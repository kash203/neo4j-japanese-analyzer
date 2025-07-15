package com.example.neo4j.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.core.UpperCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.ja.JapaneseTokenizerFactory;
import org.neo4j.graphdb.schema.AnalyzerProvider;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Japanese Analyzer Provider for Neo4j using Kuromoji tokenizer.
 * 
 * This analyzer provides Japanese text analysis capabilities including:
 * - Morphological analysis using Kuromoji
 * - Uppercase filtering
 * - Japanese stop words filtering
 */
public class JapaneseAnalyzerProvider extends AnalyzerProvider {

    /**
     * Constructor that registers this analyzer with the name "japanese".
     */
    public JapaneseAnalyzerProvider() {
        super("japanese");
    }

    /**
     * Creates and returns a Lucene Analyzer instance configured for Japanese text analysis.
     * 
     * The analyzer pipeline consists of:
     * 1. JapaneseTokenizer - Kuromoji-based morphological analysis
     * 2. UpperCaseFilter - Converts tokens to uppercase
     * 3. StopFilter - Removes Japanese stop words
     * 
     * @return Configured Analyzer instance for Japanese text
     * @throws UncheckedIOException if analyzer creation fails
     */
    @Override
    public Analyzer createAnalyzer() {
        try {
            return CustomAnalyzer.builder()
                    // Use Kuromoji tokenizer for Japanese morphological analysis
                    .withTokenizer(JapaneseTokenizerFactory.class)
                    
                    // Convert all tokens to uppercase for consistent searching
                    .addTokenFilter(UpperCaseFilterFactory.class)
                    
                    // Remove Japanese stop words
                    .addTokenFilter(StopFilterFactory.class, 
                        "ignoreCase", "true",
                        "words", "japanese-stopwords.txt", 
                        "format", "wordset")
                    
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create Japanese analyzer", e);
        }
    }
}
