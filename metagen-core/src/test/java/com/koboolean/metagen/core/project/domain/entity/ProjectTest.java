package com.koboolean.metagen.core.project.domain.entity;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
    @CsvSource({"TB_, TB_", "tb, TB_", "' tb_ ', TB_", "T_MST, T_MST_", "TB2, TB2_"})
    @DisplayName("테이블명 접두어는 대문자로 바꾸고 끝에 _ 를 붙인다")
    void normalize(String input, String expected) {
        assertThat(Project.normalizeTablePrefix(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("빈 접두어는 사용하지 않는다(null)")
    void blank() {
        assertThat(Project.normalizeTablePrefix(null)).isNull();
        assertThat(Project.normalizeTablePrefix("  ")).isNull();
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"1TB", "_TB", "TB-", "테이블", "TB TB", "ABCDEFGHIJKLMNOPQRSTU"})
    @DisplayName("영문으로 시작하지 않거나 영문/숫자/_ 외 문자가 있거나 20자를 넘으면 INVALID_TABLE_PREFIX")
    void invalid(String input) {
        assertThatThrownBy(() -> Project.normalizeTablePrefix(input))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TABLE_PREFIX);
    }
}
