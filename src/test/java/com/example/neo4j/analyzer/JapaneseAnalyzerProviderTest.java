package com.example.neo4j.analyzer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JapaneseAnalyzerProvider.
 * 
 * Tests the Japanese analyzer functionality including:
 * - Analyzer registration and availability
 * - Full-text index creation with Japanese analyzer
 * - Japanese text tokenization and search
 * - Stop words filtering
 */
public class JapaneseAnalyzerProviderTest {

    private Neo4j embeddedDatabaseServer;
    private Driver driver;

    @BeforeEach
    void initializeNeo4j() {
        // Start embedded Neo4j - the analyzer provider will be loaded via ServiceLoader
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .build();
        
        driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
    }

    @AfterEach
    void closeNeo4j() {
        if (driver != null) {
            driver.close();
        }
        if (embeddedDatabaseServer != null) {
            embeddedDatabaseServer.close();
        }
    }

    @Test
    void testJapaneseAnalyzerIsAvailable() {
        try (Session session = driver.session()) {
            // Check if our Japanese analyzer is available
            Result result = session.run("CALL db.index.fulltext.listAvailableAnalyzers()");
            
            List<String> analyzerNames = result.stream()
                    .map(record -> record.get("analyzer").asString())
                    .collect(Collectors.toList());
            
            assertTrue(analyzerNames.contains("japanese"), 
                "Japanese analyzer should be available in the list of analyzers");
        }
    }

    @Test
    void testCreateFullTextIndexWithJapaneseAnalyzer() {
        try (Session session = driver.session()) {
            // Create a full-text index using the Japanese analyzer
            session.run("CREATE FULLTEXT INDEX japanese_text_index FOR (n:Document) ON EACH [n.content] " +
                       "OPTIONS {indexConfig: {`fulltext.analyzer`: 'japanese'}}");
            
            // Wait for index to be online
            session.run("CALL db.awaitIndexes()");
            
            // Verify the index was created
            Result result = session.run("SHOW INDEXES YIELD name, type, entityType, labelsOrTypes, properties, options " +
                                      "WHERE name = 'japanese_text_index'");
            
            assertTrue(result.hasNext(), "Japanese full-text index should be created");
            
            Record indexRecord = result.next();
            assertEquals("FULLTEXT", indexRecord.get("type").asString());
            assertEquals("NODE", indexRecord.get("entityType").asString());
        }
    }

    @Test
    void testJapaneseTextTokenizationAndSearch() {
        try (Session session = driver.session()) {
            // Create index
            session.run("CREATE FULLTEXT INDEX japanese_content_index FOR (n:Article) ON EACH [n.title, n.content] " +
                       "OPTIONS {indexConfig: {`fulltext.analyzer`: 'japanese'}}");
            
            // Wait for index to be online
            session.run("CALL db.awaitIndexes()");
            
            // Create test data with Japanese text
            session.run("CREATE (a1:Article {title: '日本の技術', content: '日本は先進的な技術を持っています。'}), " +
                       "(a2:Article {title: 'コンピュータサイエンス', content: 'プログラミングは面白い分野です。'}), " +
                       "(a3:Article {title: '人工知能', content: '機械学習とディープラーニングが注目されています。'})");
            
            // Test search for "技術" (technology)
            Result result = session.run(
                "CALL db.index.fulltext.queryNodes('japanese_content_index', '技術') " +
                "YIELD node, score RETURN node.title AS title, score ORDER BY score DESC"
            );
            
            assertTrue(result.hasNext(), "Should find articles containing '技術'");
            Record firstResult = result.next();
            assertEquals("日本の技術", firstResult.get("title").asString());
            
            // Test search for "プログラミング" (programming)
            result = session.run(
                "CALL db.index.fulltext.queryNodes('japanese_content_index', 'プログラミング') " +
                "YIELD node, score RETURN node.title AS title, score ORDER BY score DESC"
            );
            
            assertTrue(result.hasNext(), "Should find articles containing 'プログラミング'");
            firstResult = result.next();
            assertEquals("コンピュータサイエンス", firstResult.get("title").asString());
        }
    }

    @Test
    void testStopWordsFiltering() {
        try (Session session = driver.session()) {
            // Create index
            session.run("CREATE FULLTEXT INDEX stop_words_test_index FOR (n:TestDoc) ON EACH [n.text] " +
                       "OPTIONS {indexConfig: {`fulltext.analyzer`: 'japanese'}}");
            
            // Wait for index to be online
            session.run("CALL db.awaitIndexes()");
            
            // Create test data with stop words
            session.run("CREATE (d1:TestDoc {text: 'これは重要な情報です。'}), " +
                       "(d2:TestDoc {text: 'あれは古い情報でした。'})");
            
            // Search for stop word "これ" should not return meaningful results
            // but search for "重要" should work
            Result result = session.run(
                "CALL db.index.fulltext.queryNodes('stop_words_test_index', '重要') " +
                "YIELD node, score RETURN node.text AS text, score ORDER BY score DESC"
            );
            
            assertTrue(result.hasNext(), "Should find documents containing '重要'");
            Record firstResult = result.next();
            assertEquals("これは重要な情報です。", firstResult.get("text").asString());
        }
    }

    @Test
    void testMixedJapaneseAndEnglishText() {
        try (Session session = driver.session()) {
            // Create index
            session.run("CREATE FULLTEXT INDEX mixed_text_index FOR (n:MixedDoc) ON EACH [n.content] " +
                       "OPTIONS {indexConfig: {`fulltext.analyzer`: 'japanese'}}");
            
            // Wait for index to be online
            session.run("CALL db.awaitIndexes()");
            
            // Create test data with mixed Japanese and English
            session.run("CREATE (d1:MixedDoc {content: 'Neo4jは素晴らしいgraph databaseです。'}), " +
                       "(d2:MixedDoc {content: 'JavaとPythonでプログラミングしています。'})");
            
            // Test search for English word
            Result result = session.run(
                "CALL db.index.fulltext.queryNodes('mixed_text_index', 'neo4j') " +
                "YIELD node, score RETURN node.content AS content, score ORDER BY score DESC"
            );
            
            assertTrue(result.hasNext(), "Should find documents containing 'neo4j'");
            
            // Test search for Japanese word
            result = session.run(
                "CALL db.index.fulltext.queryNodes('mixed_text_index', 'プログラミング') " +
                "YIELD node, score RETURN node.content AS content, score ORDER BY score DESC"
            );
            
            assertTrue(result.hasNext(), "Should find documents containing 'プログラミング'");
        }
    }
}
