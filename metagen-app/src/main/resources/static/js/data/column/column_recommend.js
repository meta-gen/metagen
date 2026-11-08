/**
 * 컬럼 속성 자동 추천 (컬럼 일괄 편집에서 표준용어로 새 컬럼을 추가할 때)
 *
 * 데이터 타입/길이는 표준 도메인에서 오므로 바꾸지 않고(도메인에 타입이 없을 때만 채운다), NOT NULL / 기본값 / PK 만 추천한다.
 * 추천은 미리 채워 두는 값일 뿐이라 사용자가 표에서 바로 바꿀 수 있고, 행에 "추천" 라벨과 근거를 보여준다.
 *
 * 규칙
 * - 여부 (…_여부 / …_YN)          : NOT NULL, 기본값 N  (타입이 없으면 CHAR(1))
 * - 등록·생성 일시/일자            : NOT NULL, 타입이 날짜/시간이면 기본값 CURRENT_TIMESTAMP
 * - 등록자·생성자 (…자 / …자_ID)   : NOT NULL
 * - 테이블 첫 컬럼 + 식별자 (…_ID / …_NO / …_SN / …_SEQ, 아이디/번호/일련번호/순번) + PK 없음 : PK
 */

const CREATE_WORDS = ["등록", "생성", "최초등록", "입력"];
const DATE_WORDS = ["일시", "일자", "일"];
const USER_WORDS = ["자", "자ID", "자아이디", "ID", "아이디"];
const KEY_WORDS = ["아이디", "번호", "일련번호", "순번", "ID"];
const DATE_TYPES = ["DATE", "DATETIME", "TIMESTAMP"];

/**
 * @param term    표준용어 (commonStandardTermName, commonStandardTermAbbreviation)
 * @param fields  새 컬럼 행 값 (fromTerm 결과 : dataType/maxLength 등이 채워진 상태) - 이 객체를 직접 바꾼다
 * @param context {isFirst: 테이블에 남은 컬럼이 없는지, hasPk: 이미 PK 컬럼이 있는지}
 * @returns 적용한 추천 근거 목록 (없으면 빈 배열)
 */
export function applyRecommendation(term, fields, {isFirst = false, hasPk = false} = {}) {
    const words = String(term.commonStandardTermName ?? "").split("_").filter(Boolean);
    const abbr = String(term.commonStandardTermAbbreviation ?? "").toUpperCase();
    const first = words[0] ?? "";
    const last = words[words.length - 1] ?? "";
    const reasons = [];

    const notNull = () => {
        if (fields.isNullable !== "N") fields.isNullable = "N";
    };

    // 여부
    if (last === "여부" || abbr.endsWith("_YN") || abbr === "YN") {
        if (!fields.dataType) {
            fields.dataType = "CHAR";
            fields.maxLength = "1";
        }
        notNull();
        if (!fields.defaultValue) fields.defaultValue = "N";
        reasons.push("여부 컬럼 : NOT NULL, 기본값 N");
        return reasons;
    }

    // 등록·생성 일시/일자
    if (CREATE_WORDS.includes(first) && DATE_WORDS.includes(last)) {
        notNull();
        if (DATE_TYPES.includes(String(fields.dataType ?? "").toUpperCase()) && !fields.defaultValue) {
            fields.defaultValue = "CURRENT_TIMESTAMP";
            reasons.push("등록 일시 : NOT NULL, 기본값 CURRENT_TIMESTAMP");
        } else {
            reasons.push("등록 일시 : NOT NULL");
        }
        return reasons;
    }

    // 등록자·생성자
    if (CREATE_WORDS.some(w => first === w || first === w + "자") && (USER_WORDS.includes(last) || last.endsWith("자"))) {
        notNull();
        reasons.push("등록자 : NOT NULL");
        return reasons;
    }

    // 첫 컬럼 식별자 → PK
    if (isFirst && !hasPk && (/_(ID|NO|SN|SEQ)$/.test(abbr) || KEY_WORDS.includes(last))) {
        fields.isPk = "Y";
        fields.isNullable = "N";
        fields.isRequired = "Y";
        fields.isUnique = "Y";
        reasons.push("테이블 첫 식별자 컬럼 : PK");
    }

    return reasons;
}
