package com.koboolean.metagen.core.project.domain.entity;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.common.jpa.BaseEntity;
import com.koboolean.metagen.core.project.domain.enums.DbType;
import com.koboolean.metagen.core.security.domain.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity implements Serializable {

    @Id @GeneratedValue
    @Column(name = "project_id")
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "project")
    private List<ProjectMember> projectMember = new ArrayList<>();

    @Column(name = "is_auto_active")
    private Boolean isAutoActive; // 자동 승인여부

    @Column(name = "is_dic_abbr_used")
    private Boolean isDicAbbrUsed; // 데이터 사전 약어 사용 여부

    @Column(name = "table_prefix", length = 20)
    private String tablePrefix; // 테이블명 접두어 (예: TB_) - 표준 단어 기준 테이블명 생성 시 사용

    @Enumerated(EnumType.STRING)
    @Column(name = "db_type", length = 20, nullable = false)
    private DbType dbType; // 대상 DB - 테이블 DDL 생성 기준 (없으면 DbType.DEFAULT)

    /**
     * 대상 DB 를 지정하지 않고 저장하면 기본값(PostgreSQL)으로 저장한다. (Builder 는 필드 초기값을 쓰지 않는다)
     */
    @PrePersist
    @PreUpdate
    void applyDefaults() {
        if (dbType == null) dbType = DbType.DEFAULT;
    }

    private static final Pattern TABLE_PREFIX_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    /**
     * 테이블명 접두어를 정규화한다 : 공백 제거, 대문자, 끝에 '_' 보장 (tb → TB_). 빈 값이면 접두어를 사용하지 않는다(null).
     * 영문으로 시작하고 영문/숫자/'_'만 허용하며 '_' 포함 20자까지 가능하다.
     */
    public static String normalizeTablePrefix(String tablePrefix) {
        if (tablePrefix == null || tablePrefix.isBlank()) {
            return null;
        }

        String prefix = tablePrefix.trim().toUpperCase();
        if (!prefix.endsWith("_")) {
            prefix = prefix + "_";
        }

        if (!TABLE_PREFIX_PATTERN.matcher(prefix).matches() || prefix.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_TABLE_PREFIX, tablePrefix);
        }

        return prefix;
    }
}
