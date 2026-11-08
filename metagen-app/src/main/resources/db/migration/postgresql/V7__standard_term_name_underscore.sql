-- 표준용어명의 단어 구분자를 '_' 로 통일한다. (영문약어명과 단어 수를 맞춘다 : 사용자_이름 / USER_NM)

-- 1) 단어 매핑 순서대로 단어명을 '_' 로 이은 값으로 바꾼다.
--    기존 이름에서 구분자(공백/_)를 뺀 값이 단어명을 그대로 이은 값과 같을 때만 바꾼다. (예: GIS자료여부 → GIS_자료_여부)
update standard_terms t
set common_standard_term_name = x.joined
from (
    select m.standard_term_id,
           string_agg(w.common_standard_word_name, '_' order by m.order_index) as joined,
           string_agg(w.common_standard_word_name, '' order by m.order_index) as concatenated
    from standard_term_word_mappings m
    join standard_words w on w.id = m.standard_word_id
    group by m.standard_term_id
) x
where x.standard_term_id = t.standard_term_id
  and regexp_replace(t.common_standard_term_name, '[[:space:]_]+', '', 'g') = x.concatenated
  and t.common_standard_term_name <> x.joined;

-- 2) 매핑으로 확인되지 않는 이름도 공백 구분이면 공백을 '_' 로 바꾼다. (예: 사용자 이름 → 사용자_이름)
update standard_terms
set common_standard_term_name = regexp_replace(trim(common_standard_term_name), '[[:space:]]+', '_', 'g')
where common_standard_term_name ~ '[[:space:]]';

-- 3) 용어 이음동의어도 같은 형식으로
update standard_term_synonym_list
set synonym_list = regexp_replace(trim(synonym_list), '[[:space:]]+', '_', 'g')
where synonym_list ~ '[[:space:]]';
