package com.example.onlineCodeSubmitter.factory;

import com.example.onlineCodeSubmitter.enums.LanguageProcessorEnum;
import com.example.onlineCodeSubmitter.interfaces.LanguageProcessor;
import org.springframework.context.ApplicationContext;

public class LanguageProcessorFactory {

    private final ApplicationContext context;

    public LanguageProcessorFactory(ApplicationContext context) {
        this.context = context;
    }

    public LanguageProcessor getProcessor(String language) {
        LanguageProcessorEnum languageProcessor = LanguageProcessorEnum.getProcessorByLanguage(language);
        return languageProcessor == null || languageProcessor.getProcessor().isEmpty() ? null : getBean(languageProcessor.getProcessor());
    }

    private <T> T getBean(String beanName) {
        return (T) context.getBean(beanName);
    }

    private <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    private <T> T getBeanFromClass(String clazz) {
        Class<T> clazzName;
        try {
            clazzName = (Class<T>) Class.forName(clazz);
            return context.getBean(clazzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
