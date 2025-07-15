package com.example.neo4j.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.core.UpperCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.ja.JapaneseTokenizerFactory;
import org.neo4j.graphdb.schema.AnalyzerProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * @return A description of this analyzer.
     */
    @Override
    public String description() {
        return "Japanese text analyzer using Kuromoji tokenizer with stop words filtering and uppercase normalization";
    }

    @Override
    public List<String> stopwords() {
        Analyzer analyzer = createAnalyzer();
        try {
            // Access tokenFilters field from CustomAnalyzer
            var tokenFiltersField = analyzer.getClass().getDeclaredField("tokenFilters");
            tokenFiltersField.setAccessible(true);
            var tokenFilters = tokenFiltersField.get(analyzer);
            
            if (tokenFilters instanceof Object[] filterArray) {
                for (Object filter : filterArray) {
                    // Look for StopFilterFactory
                    if (filter.getClass().getSimpleName().contains("StopFilter")) {
                        try {
                            // Get stopwords from the parent class AbstractWordsFileFilterFactory
                            var wordsField = filter.getClass().getSuperclass().getDeclaredField("words");
                            wordsField.setAccessible(true);
                            var words = wordsField.get(filter);
                            
                            if (words != null) {
                                // Convert CharArraySet to List<String>
                                List<String> result = new java.util.ArrayList<>();
                                if (words instanceof Iterable<?> iterableWords) {
                                    for (Object obj : iterableWords) {
                                        if (obj instanceof char[]) {
                                            result.add(new String((char[]) obj));
                                        } else {
                                            result.add(obj.toString());
                                        }
                                    }
                                }
                                return result;
                            }
                        } catch (Exception e) {
                            // Continue to next filter if this fails
                        }
                    }
                }
            }
        } catch (Exception e) {
            // If reflection fails, return empty list
        } finally {
            analyzer.close();
        }
        return List.of();
    }
}
