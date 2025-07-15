package com.example.neo4j.analyzer;

import org.apache.lucene.analysis.Analyzer;
import java.lang.reflect.Field;

/**
 * StopFilterFactoryの内部構造を詳しく調べるクラス
 */
public class StopFilterInspector {
    public static void main(String[] args) {
        JapaneseAnalyzerProvider provider = new JapaneseAnalyzerProvider();
        Analyzer analyzer = provider.createAnalyzer();
        
        try {
            var tokenFiltersField = analyzer.getClass().getDeclaredField("tokenFilters");
            tokenFiltersField.setAccessible(true);
            var tokenFilters = tokenFiltersField.get(analyzer);
            
            if (tokenFilters instanceof Object[] filterArray) {
                System.out.println("Found " + filterArray.length + " token filters:");
                
                for (int i = 0; i < filterArray.length; i++) {
                    Object filter = filterArray[i];
                    System.out.println("\nFilter " + i + ": " + filter.getClass().getName());
                    
                    // フィールドを全て調べる
                    Field[] fields = filter.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        try {
                            Object value = field.get(filter);
                            System.out.println("  " + field.getName() + " (" + field.getType().getSimpleName() + "): " + value);
                        } catch (Exception e) {
                            System.out.println("  " + field.getName() + " (" + field.getType().getSimpleName() + "): <access failed>");
                        }
                    }
                    
                    // 親クラスのフィールドも調べる
                    Class<?> superClass = filter.getClass().getSuperclass();
                    if (superClass != null && !superClass.equals(Object.class)) {
                        System.out.println("  Superclass fields from " + superClass.getSimpleName() + ":");
                        Field[] superFields = superClass.getDeclaredFields();
                        for (Field field : superFields) {
                            field.setAccessible(true);
                            try {
                                Object value = field.get(filter);
                                System.out.println("    " + field.getName() + " (" + field.getType().getSimpleName() + "): " + value);
                            } catch (Exception e) {
                                System.out.println("    " + field.getName() + " (" + field.getType().getSimpleName() + "): <access failed>");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            analyzer.close();
        }
    }
}
