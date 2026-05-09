package com.jinju.jinjuwiki.domain.document.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

@Schema(description = "문서 생성 요청")
public record DocumentCreateRequest(
        @Schema(description = "문서 제목", example = "진주성 전투")
        @NotBlank(message = "제목은 비어 있을 수 없습니다.")
        @Size(max = 150, message = "제목은 150자 이하로 입력해야 합니다.")
        String title,

        @Schema(description = "문서 요약", example = "임진왜란 당시 진주성에서 벌어진 전투를 정리한 문서")
        @NotBlank(message = "요약은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "요약은 255자 이하로 입력해야 합니다.")
        String summary,

        @Schema(description = "문서가 속한 카테고리 ID", example = "1")
        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        @Schema(description = "학교 하위 문서가 속한 학교 문서 ID", example = "101", nullable = true)
        Long schoolDocumentId,

        @Schema(description = "사건 발생 연도, 일반 문서는 비워둘 수 있는 필드", example = "1592", nullable = true)
        Integer eventYear,

        @Schema(
                description = "에디터 본문 JSON",
                example = "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"진주성 전투 본문\"}]}]}"
        )
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
