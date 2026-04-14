package com.jinju.jinjuwiki.domain.document.dto.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

public record DocumentUpdateRequest(
        @NotBlank(message = "제목은 비어 있을 수 없습니다.")
        @Size(max = 150, message = "제목은 150자 이하로 입력해야 합니다.")
        String title,

        @NotBlank(message = "요약은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "요약은 255자 이하로 입력해야 합니다.")
        String summary,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        Integer eventYear,

        @NotNull(message = "본문 JSON은 비어 있을 수 없습니다.")
        @JsonDeserialize(using = ContentJsonNodeDeserializer.class)
        JsonNode contentJson
) {

    // Jackson 3 요청 본문을 기존 Jackson 2 JsonNode로 변환하는 역직렬화 클래스
    static class ContentJsonNodeDeserializer extends StdDeserializer<JsonNode> {

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        ContentJsonNodeDeserializer() {
            super(JsonNode.class);
        }

        // 요청 본문 contentJson 변환 메서드
        @Override
        public JsonNode deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            tools.jackson.databind.JsonNode runtimeJsonNode = context.readTree(parser);
            try {
                return OBJECT_MAPPER.readTree(runtimeJsonNode.toString());
            } catch (JsonProcessingException exception) {
                throw new IllegalArgumentException("본문 JSON 변환 실패", exception);
            }
        }
    }
}
