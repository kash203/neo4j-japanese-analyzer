package com.example.neo4j.analyzer;

import org.apache.lucene.analysis.Analyzer;
import java.lang.reflect.Field;

/**
 * Analyzer内部構造を調べるためのデバッグクラス
 */
public class AnalyzerInspector {
    public static void main(String[] args) {
        JapaneseAnalyzerProvider provider = new JapaneseAnalyzerProvider();
        Analyzer analyzer = provider.createAnalyzer();
        
        System.out.println("=== Analyzer Class Information ===");
        System.out.println("Class: " + analyzer.getClass().getName());
        System.out.println("Superclass: " + analyzer.getClass().getSuperclass().getName());
        
        System.out.println("\n=== Fields ===");
        Field[] fields = analyzer.getClass().getDeclaredFields();
        for (Field field : fields) {
            System.out.println("Field: " + field.getName() + " (" + field.getType().getName() + ")");
        }
        
        System.out.println("\n=== Trying to access components ===");
        try {
            Field componentsField = analyzer.getClass().getDeclaredField("components");
            componentsField.setAccessible(true);
            Object components = componentsField.get(analyzer);
            
            if (components instanceof java.util.List<?> componentsList) {
                System.out.println("Components found: " + componentsList.size());
                for (int i = 0; i < componentsList.size(); i++) {
                    Object component = componentsList.get(i);
                    System.out.println("Component " + i + ": " + component.getClass().getName());
                    
                    // 各コンポーネントのフィールドを調べる
                    Field[] componentFields = component.getClass().getDeclaredFields();
                    for (Field field : componentFields) {
                        System.out.println("  Field: " + field.getName() + " (" + field.getType().getName() + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error accessing components: " + e.getMessage());
        }
        
        analyzer.close();
    }
}
