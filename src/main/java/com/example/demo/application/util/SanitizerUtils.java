package com.example.demo.application.util;

public class SanitizerUtils {
    public static String sanitizar(String input){
        if (input == null) return "";
        
        return input.replaceAll("[&<>]", "")
                    .replaceAll("\\s+", " ")
                    .trim();
    }
}
