package com.koboolean.metagen.core.logs.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskerTest {

    @Test
    @DisplayName("JSON : 중첩 객체/배열의 비밀번호 계열 key 값을 마스킹한다")
    void maskJson() {
        String masked = SensitiveDataMasker.mask(
                "{\"username\":\"admin\",\"password\":\"secret\",\"items\":[{\"currentPassword\":\"old\",\"pwd\":\"x\",\"name\":\"n\"}],\"changedPasswd\":null}");

        assertThat(masked)
                .contains("\"username\":\"admin\"")
                .contains("\"password\":\"****\"")
                .contains("\"currentPassword\":\"****\"")
                .contains("\"pwd\":\"****\"")
                .contains("\"name\":\"n\"")
                .contains("\"changedPasswd\":null")
                .doesNotContain("secret", "\"old\"");
    }

    @Test
    @DisplayName("form-urlencoded : 비밀번호 계열 파라미터 값을 마스킹한다")
    void maskForm() {
        assertThat(SensitiveDataMasker.mask("username=admin&password=secret&project_id=0"))
                .isEqualTo("username=admin&password=****&project_id=0");
    }

    @Test
    @DisplayName("JSON 형식이 깨진 경우 form 규칙으로 처리하고, 빈 값은 그대로 반환한다")
    void invalidOrEmpty() {
        assertThat(SensitiveDataMasker.mask("{broken")).isEqualTo("{broken");
        assertThat(SensitiveDataMasker.mask(null)).isNull();
        assertThat(SensitiveDataMasker.mask("")).isEmpty();
    }
}
