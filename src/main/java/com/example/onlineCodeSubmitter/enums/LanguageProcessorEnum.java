package com.example.onlineCodeSubmitter.enums;

import java.util.Map;
import java.util.HashMap;

public enum LanguageProcessorEnum {

    JAVA("java","javaProcessor"),
    PYTHON("python", "pythonProcessor");
    
    private String language;
    private String processor;

    private LanguageProcessorEnum(String language, String processor) {
        this.language = language;
        this.processor = processor;
    }

    public String getLanguage() {
        return language;
    }
    public String getProcessor() {
        return processor;
    }

    private static Map<String, LanguageProcessorEnum> processorMap = new HashMap<>(LanguageProcessorEnum.values().length);

    static {
        for(LanguageProcessorEnum languageProcessorEnum : LanguageProcessorEnum.values()) {
            processorMap.put(languageProcessorEnum.getLanguage(), languageProcessorEnum);
        }
    }

    public static LanguageProcessorEnum getProcessorByLanguage(String language) {
        return processorMap.get(language);
    }
}
