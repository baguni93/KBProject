package org.scoula.analysis.service;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenAITestService {

    private final OpenAIClient openAIClient;

    public String testConnection() {

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model("gpt-4o-mini")
                .input("다음 문장만 정확히 출력하세요: OpenAI 연결 성공")
                .build();

        Response response =
                openAIClient.responses().create(params);

        // 응답 결과 저장 부분.
        String result = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .collect(Collectors.joining());

        if (result.isBlank()) {
            throw new IllegalStateException(
                    "OpenAI가 텍스트 응답을 반환하지 않았습니다."
            );
        }

        return result;
    }
}