package com.koboolean.metagen.security.exception.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 알수없는 오류가 발생하였습니다.
     */
    UNKNOWN_EXCEPTION(HttpStatus.BAD_REQUEST, "UNKNOWN-EXCEPTION", "알수없는 오류가 발생하였습니다."),

    /**
     * 패스워드가 일치하지 않습니다.
     */
    PASSWORD_NOT_MATCHED(HttpStatus.NOT_FOUND, "PASSWORD-NOT-MATCHED", "패스워드가 일치하지 않습니다."),


    /**
     * 엑셀 생성이 실패하였습니다.
     */
    CREATE_EXCEL_FAIL_EXCEPTION(HttpStatus.NOT_FOUND, "CREATE_EXCEL_FAIL_EXCEPTION", "엑셀 생성이 실패하였습니다."),

    /**
     * 등록된 아이디가 존재합니다.
     */
    USERNAME_IS_DUPLICATION(HttpStatus.BAD_REQUEST, "USERNAME_IS_DUPLICATION", "등록된 아이디가 존재합니다."),

    /**
     * 비밀번호는 8자 이상, 대문자 및 특수문자를 각각 하나 이상 포함해야 합니다.
     */
    PASSWORD_IS_NON_VALIDATOR(HttpStatus.BAD_REQUEST, "PASSWORD_IS_NON_VALIDATOR", "비밀번호는 8자 이상, 대문자 및 특수문자를 각각 하나 이상 포함해야 합니다."),

    /**
     * 아이디는 5~20자 사이의 소문자, 숫자, 밑줄(_)로 구성할 수 있습니다. 단, 밑줄은 처음과 끝에 사용할 수 없으며 연속된 밑줄도 허용되지 않습니다.
     */
    USERNAME_IS_NON_VALIDATOR(HttpStatus.BAD_REQUEST, "USERNAME_IS_NON_VALIDATOR", "아이디는 5~20자 사이의 소문자, 숫자, 밑줄(_)로 구성할 수 있습니다. 단, 밑줄은 처음과 끝에 사용할 수 없으며 연속된 밑줄도 허용되지 않습니다."),

    /**
     * 동일한 형식의 표준 용어와 표준 도메인이 존재합니다. 중복된 용어는 등록이 불가능합니다.
     */
    DUPLICATE_TERM_DATA(HttpStatus.BAD_REQUEST, "DUPLICATE_TERM_DATA", "동일한 형식의 표준 용어와 표준 도메인이 존재합니다. 중복된 용어는 등록이 불가능합니다."),


    /**
     * 해당 용어를 찾을 수 없습니다.
     */
    NOT_FOUND_TERM_DATA(HttpStatus.BAD_REQUEST, "NOT_FOUND_TERM_DATA", "해당 용어를 찾을 수 없습니다."),

    /**
     * 등록 용어중 매핑되지 않는 단어가 존재합니다.
     */
    NOT_FOUND_WORD_DATA(HttpStatus.BAD_REQUEST, "NOT_FOUND_WORD_DATA", "등록 용어중 매핑되지 않는 단어가 존재합니다."),

    /**
     * 승인된 정보는 삭제할 수 없습니다.
     */
    APPROVED_DATA_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "APPROVED_DATA_CANNOT_BE_DELETED", "승인된 정보는 삭제할 수 없습니다."),

    /**
     * 사용 중인 정보는 삭제할 수 없습니다.
     */
    RELATION_EXISTS(HttpStatus.BAD_REQUEST, "RELATION_EXISTS", "사용 중인 정보는 삭제할 수 없습니다."),

    /**
     * 해당 도메인을 찾을 수 없습니다.
     */
    NOT_FOUND_DOMAIN_DATA(HttpStatus.BAD_REQUEST, "NOT_FOUND_DOMAIN_DATA", "해당 도메인을 찾을 수 없습니다."),

    /**
     * 권한이 없어 작업을 수행할 수 없습니다.
     */
    DATA_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "DATA_CANNOT_BE_DELETED", "권한이 없어 작업을 수행할 수 없습니다."),

    /**
     * 저장된 데이터가 존재합니다.
     */
    SAVED_DATA_EXISTS(HttpStatus.BAD_REQUEST, "SAVED_DATA_EXISTS", "저장된 데이터가 존재합니다."),

    /**
     * 컬럼으로 등록된 정보가 존재하기 때문에 삭제가 불가능합니다.
     */
    COLUMN_DEPENDENCY_EXISTS(HttpStatus.BAD_REQUEST, "COLUMN_DEPENDENCY_EXISTS", "컬럼으로 등록된 정보가 존재하기 때문에 삭제가 불가능합니다."),

    /**
     * 등록된 테이블 정보가 존재하지 않습니다.
     */
    TABLE_IS_NOT_DEFINED(HttpStatus.BAD_REQUEST, "TABLE_IS_NOT_DEFINED", "등록된 테이블 정보가 존재하지 않습니다."),

    /**
     * NULL 허용여부, 필수 입력 여부, 민감정보 여부, PK 여부, 고유값 여부, 인덱스 생성 여부, 암호화 필요 여부는 Y 또는 N 값이어야합니다.
     */
    COLUMN_BOOLEAN_TYPE_DEF(HttpStatus.BAD_REQUEST, "COLUMN_BOOLEAN_TYPE_DEF", "NULL 허용여부, 필수 입력 여부, 민감정보 여부, PK 여부, 고유값 여부, 인덱스 생성 여부, 암호화 필요 여부는 Y 또는 N 값이어야합니다."),

    /**
     * 등록된 테이블 정보가 존재하지 않습니다.
     */
    TERM_IS_NOT_DEFINED(HttpStatus.BAD_REQUEST, "TERM_IS_NOT_DEFINED", "등록된 표준용어 정보가 존재하지 않습니다."),

    /**
     * 승인된 정보는 수정이 불가능합니다.
     */
    APPROVED_DATA_CANNOT_BE_UPDATE(HttpStatus.BAD_REQUEST, "APPROVED_DATA_CANNOT_BE_UPDATE", "승인된 정보는 수정이 불가능합니다."),

    /**
     * 입력하신 표준도메인분류명을 찾을 수 없습니다.
     */
    DOMAIN_IS_NOT_DEFINED(HttpStatus.BAD_REQUEST, "DOMAIN_IS_NOT_DEFINED", "입력하신 표준도메인분류명을 찾을 수 없습니다."),

    /**
     * 해당 프로젝트의 템플릿 내 동일한 코드규칙명을 가진 코드규칙이 존재합니다.
     */
    TEMPLATE_CODE_RULE_NAME_IN_PROJECT_DUPLICATE(HttpStatus.BAD_REQUEST, "TEMPLATE_CODE_RULE_NAME_IN_PROJECT_DUPLICATE", "해당 프로젝트의 템플릿 내 동일한 코드규칙명을 가진 코드규칙이 존재합니다."),

    /**
     * 해당 프로젝트에 동일한 템플릿명을 가진 템플릿이 존재합니다.
     */
    TEMPLATE_NAME_IN_PROJECT_DUPLICATE(HttpStatus.BAD_REQUEST, "TEMPLATE_NAME_IN_PROJECT_DUPLICATE", "해당 프로젝트에 동일한 템플릿명을 가진 템플릿이 존재합니다."),

    /**
     * 프로젝트 관리자는 삭제가 불가능합니다.
     */
    MANAGER_NON_DELETED(HttpStatus.BAD_REQUEST, "MANAGER_NON_DELETED", "관리자는 변경이 불가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
