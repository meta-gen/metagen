-- 테이블명 변경 시 컬럼(column_info.table_name)에 예전 테이블명이 남아있던 데이터를
-- 연관관계(table_info_id) 기준으로 현재 테이블명과 맞춘다.
update column_info c
set table_name = (select t.table_name from table_info t where t.table_info_id = c.table_info_id)
where c.table_info_id is not null
  and exists (select 1 from table_info t
              where t.table_info_id = c.table_info_id
                and (c.table_name is null or t.table_name <> c.table_name));
