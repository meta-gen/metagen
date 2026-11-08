package com.koboolean.metagen.data.table.domain.dto;

import java.util.List;

/**
 * 테이블 한글명(예: 사용_내역_관리)을 표준 단어로 변환한 결과
 *
 * @param tableName    생성된 테이블명 (예: TB_USE_HIST_MGMT) - 변환할 수 없는 단어가 있으면 null
 * @param tableKorName 단어를 공백으로 이은 테이블 한글명 (예: 사용 내역 관리) - table_info.table_kor_name 에 저장
 * @param tablePrefix  프로젝트 설정의 테이블명 접두어 (예: TB_) - 없으면 null
 * @param words        단어별 변환 결과
 * @param valid        모든 단어가 승인된 표준 단어인지 여부
 */
public record TableNameDto(String tableName, String tableKorName, String tablePrefix, List<Word> words, boolean valid) {

    /** 단어별 상태 */
    public enum Status {
        /** 승인된 표준 단어 */
        APPROVED,
        /** 표준 단어로 등록되었으나 미승인 */
        NOT_APPROVED,
        /** 표준 단어로 등록되지 않음 */
        NOT_FOUND
    }

    /**
     * @param word         입력한 단어 (예: 내역)
     * @param abbreviation 표준단어 영문약어명 (예: HIST)
     * @param englishName  표준단어 영문명
     * @param status       단어 상태
     */
    public record Word(String word, String abbreviation, String englishName, Status status) {
    }

    /** 승인되지 않았거나 등록되지 않은 단어 목록 */
    public List<String> invalidWords() {
        return words.stream()
                .filter(word -> word.status() != Status.APPROVED)
                .map(Word::word)
                .toList();
    }
}
