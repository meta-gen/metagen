-- 프로젝트 공통 컬럼 세트 : 거의 모든 테이블에 들어가는 컬럼(등록일시, 등록자, 수정일시, 수정자, 사용여부 등)을
-- 표준용어로 정해 두고, 컬럼 일괄 편집에서 한 번에 추가한다. (용어가 지워질 수 있으므로 term_id 는 FK 를 걸지 않는다)
create table common_column (
    common_column_id bigint not null,
    project_id bigint not null,
    term_id bigint not null,
    sort_order integer,
    is_nullable boolean,
    is_index boolean,
    default_value varchar(255),
    created timestamp(6),
    updated timestamp(6),
    created_by varchar(255),
    updated_by varchar(255),
    primary key (common_column_id)
);

create sequence common_column_seq start with 1 increment by 50;

create index ix_common_column_project on common_column (project_id);
