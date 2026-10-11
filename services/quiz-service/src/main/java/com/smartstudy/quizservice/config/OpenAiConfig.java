//package com.smartstudy.quizservice.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.openai.client.OpenAIClient;
//import com.openai.client.okhttp.OpenAIOkHttpClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Lazy;
//
//@Configuration
//public class OpenAiConfig {
//
//    @Bean
//    @Lazy
//    public OpenAIClient openAIClient() {
//        return OpenAIOkHttpClient.builder()
//                .apiKey(System.getenv("OPENAI_API_KEY"))
//                .baseUrl(System.getenv("OPENAI_BASE_URL"))
//                .build();
//    }
//
//    @Bean
//    public ObjectMapper objectMapper() {
//        return new ObjectMapper();
//    }
//}


package com.smartstudy.quizservice.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Bean
    public OpenAIClient openAIClient() {
        String apiKey = System.getenv("AZURE_OPENAI_KEY");
        String baseUrl = System.getenv("OPENAI_BASE_URL");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "AZURE_OPENAI_KEY environment variable is missing"
            );
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_BASE_URL environment variable is missing"
            );
        }

        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }
}