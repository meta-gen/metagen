-- 엑셀 업로드 속도 개선 : JDBC 배치 INSERT 를 쓰기 위해 IDENTITY → SEQUENCE(pooled, 50개씩 할당) 로 바꾼다.
-- (Hibernate 는 IDENTITY 키 엔티티의 INSERT 를 배치로 묶지 않는다)
-- 시퀀스 다음 값 = 기존 최대 ID + 50 : pooled 방식은 (다음 값 - 49) ~ 다음 값 범위를 쓰므로 기존 ID 와 겹치지 않는다.

create sequence table_info_seq start with 1 increment by 50;
select setval('table_info_seq', (select coalesce(max(table_info_id), 0) from table_info) + 50, false);
alter table table_info alter column table_info_id drop identity if exists;

create sequence column_info_seq start with 1 increment by 50;
select setval('column_info_seq', (select coalesce(max(column_info_id), 0) from column_info) + 50, false);
alter table column_info alter column column_info_id drop identity if exists;

create sequence standard_domains_seq start with 1 increment by 50;
select setval('standard_domains_seq', (select coalesce(max(standard_domain_id), 0) from standard_domains) + 50, false);
alter table standard_domains alter column standard_domain_id drop identity if exists;

create sequence standard_terms_seq start with 1 increment by 50;
select setval('standard_terms_seq', (select coalesce(max(standard_term_id), 0) from standard_terms) + 50, false);
alter table standard_terms alter column standard_term_id drop identity if exists;

create sequence standard_words_seq start with 1 increment by 50;
select setval('standard_words_seq', (select coalesce(max(id), 0) from standard_words) + 50, false);
alter table standard_words alter column id drop identity if exists;

-- 용어 ↔ 컬럼 관계는 column_info.standard_terms_standard_term_id(FK) 로 일원화한다.
-- 조인 테이블에만 남아있는 관계를 FK 로 옮긴 뒤 조인 테이블을 제거한다. (컬럼 추가마다 조인 테이블 전체 삭제/재등록 방지)
update column_info c
set standard_terms_standard_term_id = (
    select max(j.standard_term_standard_term_id)
    from standard_terms_column_infos j
    where j.column_infos_column_info_id = c.column_info_id)
where c.standard_terms_standard_term_id is null
  and exists (select 1 from standard_terms_column_infos j where j.column_infos_column_info_id = c.column_info_id);

drop table standard_terms_column_infos;
