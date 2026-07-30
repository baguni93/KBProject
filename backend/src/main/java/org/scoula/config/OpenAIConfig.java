package org.scoula.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAIClient openAIClient() {
        String apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "환경변수 OPENAI_API_KEY가 등록되지 않았습니다."
            );
        }

        // OPENAI_API_KEY 환경변수를 자동으로 읽음
        return OpenAIOkHttpClient.fromEnv();
    }
}