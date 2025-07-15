package com.example.neo4j.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Debug test to check if the analyzer is properly filtering stopwords.
 */
public class AnalyzerDebugTest {
    public static void main(String[] args) throws IOException {
        JapaneseAnalyzerProvider provider = new JapaneseAnalyzerProvider();
        Analyzer analyzer = provider.createAnalyzer();
        
        // Test text with known stopwords
        String testText = "これは重要な情報です";
        
        System.out.println("Original text: " + testText);
        System.out.println("Tokens after analysis:");
        
        try (TokenStream tokenStream = analyzer.tokenStream("content", testText)) {
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            
            List<String> tokens = new ArrayList<>();
            while (tokenStream.incrementToken()) {
                String token = termAttribute.toString();
                tokens.add(token);
                System.out.println("  - " + token);
            }
            
            System.out.println("\nTotal tokens: " + tokens.size());
            
            // Check if stopwords are being filtered
            if (tokens.contains("これ") || tokens.contains("は")) {
                System.out.println("WARNING: Stopwords are NOT being filtered!");
            } else {
                System.out.println("SUCCESS: Stopwords appear to be filtered.");
            }
        }
        
        analyzer.close();
    }
}
