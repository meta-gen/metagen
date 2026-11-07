-- 프로젝트 설정 : 표준 단어 기준 테이블명 생성 시 앞에 붙일 접두어 (예: TB_)
alter table project add column table_prefix varchar(20);

-- 테이블 한글명 (예: 사용 내역 관리) : 테이블명은 이 한글명을 표준 단어 영문약어로 변환해 만든다
alter table table_info add column table_kor_name varchar(255);
