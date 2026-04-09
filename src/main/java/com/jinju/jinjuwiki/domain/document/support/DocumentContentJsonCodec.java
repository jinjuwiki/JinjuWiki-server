package com.jinju.jinjuwiki.domain.document.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

// 문서 본문 JSON 직렬화 유틸
public final class DocumentContentJsonCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DocumentContentJsonCodec() {
    }

    public static String writeValue(JsonNode contentJson) {
        try {
            return OBJECT_MAPPER.writeValueAsString(contentJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("문서 본문 JSON 직렬화 실패", exception);
        }
    }

    public static JsonNode readTree(String contentJson) {
        try {
            if (contentJson == null || contentJson.isBlank()) {
                return createEmptyDocument();
            }
            return OBJECT_MAPPER.readTree(contentJson);
        } catch (JsonProcessingException exception) {
            return createLegacyDocument(contentJson);
        }
    }

    private static JsonNode createEmptyDocument() {
        ObjectNode documentNode = OBJECT_MAPPER.createObjectNode();
        documentNode.put("type", "doc");
        documentNode.putArray("content");
        return documentNode;
    }

    private static JsonNode createLegacyDocument(String legacyContent) {
        if (legacyContent == null || legacyContent.isBlank()) {
            return createEmptyDocument();
        }

        ObjectNode documentNode = OBJECT_MAPPER.createObjectNode();
        documentNode.put("type", "doc");

        ArrayNode contentNodes = documentNode.putArray("content");
        ObjectNode paragraphNode = contentNodes.addObject();
        paragraphNode.put("type", "paragraph");

        ArrayNode textNodes = paragraphNode.putArray("content");
        textNodes.addObject()
                .put("type", "text")
                .put("text", legacyContent);

        return documentNode;
    }
}
