package com.likelion.vlog.config;

import com.likelion.vlog.enums.SearchFiled;
import com.likelion.vlog.enums.SortField;
import com.likelion.vlog.enums.TagMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class appConfig implements WebMvcConfigurer {

    @Override //enum 설정
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, SortField.class, SortField::from);
        registry.addConverter(String.class, SearchFiled.class, SearchFiled::from);
        registry.addConverter(String.class, TagMode.class, TagMode::from);
    }
}
