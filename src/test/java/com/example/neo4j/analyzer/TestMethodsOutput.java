package com.example.neo4j.analyzer;

import java.util.List;

/**
 * Simple test program to demonstrate the description() and stopwords() methods.
 */
public class TestMethodsOutput {
    public static void main(String[] args) {
        JapaneseAnalyzerProvider provider = new JapaneseAnalyzerProvider();
        
        System.out.println("=== Testing description() method ===");
        String description = provider.description();
        System.out.println("Description: " + description);
        
        System.out.println("\n=== Testing stopwords() method ===");
        List<String> stopwords = provider.stopwords();
        System.out.println("Number of stopwords: " + stopwords.size());
        System.out.println("Stopwords list: " + stopwords);
        
        System.out.println("\n=== Method calls completed successfully ===");
    }
}
