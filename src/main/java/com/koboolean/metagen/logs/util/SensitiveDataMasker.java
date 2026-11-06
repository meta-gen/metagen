package com.koboolean.metagen.logs.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 시스템 로그에 저장되는 요청/응답 본문에서 비밀번호 등 민감정보를 마스킹한다.
 * (password, currentPassword, changedPasswd, pwd 등 key 이름에 passw / pwd 가 포함된 값)
 */
public final class SensitiveDataMasker {

    public static final String MASK = "****";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern SENSITIVE_KEY = Pattern.compile("(?i).*(passw|pwd).*");

    // form-urlencoded (key=value&key=value) 형식
    private static final Pattern SENSITIVE_FORM_PARAM = Pattern.compile("(?i)((?:^|&)[^&=]*(?:passw|pwd)[^&=]*=)[^&]*");

    private SensitiveDataMasker() {
    }

    public static String mask(String body) {
        if (body == null || body.isBlank()) {
            return body;
        }

        String trimmed = body.trim();

        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(trimmed);
                maskJson(root);
                return OBJECT_MAPPER.writeValueAsString(root);
            } catch (Exception e) {
                // JSON 형식이 아니면 아래 form 형식 마스킹으로 처리
            }
        }

        return SENSITIVE_FORM_PARAM.matcher(body).replaceAll("$1" + MASK);
    }

    private static void maskJson(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldNames = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);

            for (String fieldName : fieldNames) {
                JsonNode child = objectNode.get(fieldName);

                if (SENSITIVE_KEY.matcher(fieldName).matches() && child.isValueNode() && !child.isNull()) {
                    objectNode.put(fieldName, MASK);
                } else {
                    maskJson(child);
                }
            }
        } else if (node.isArray()) {
            node.forEach(SensitiveDataMasker::maskJson);
        }
    }
}
