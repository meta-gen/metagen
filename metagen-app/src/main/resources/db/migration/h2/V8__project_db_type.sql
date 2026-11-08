-- 프로젝트 설정 : 대상 DB (POSTGRESQL / ORACLE / MYSQL / MARIADB) - 테이블 관리에서 DDL 을 이 DB 기준으로 생성한다
alter table project add column db_type varchar(20) default 'POSTGRESQL' not null;
