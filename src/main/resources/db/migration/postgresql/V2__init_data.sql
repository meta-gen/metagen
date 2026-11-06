-- =====================================================================
-- MetaGen 초기 데이터 (V2) : 권한, 관리자 계정(admin/admin), 기본 프로젝트, 게시판 카테고리
-- 기존 DB(baseline) 에도 적용되므로 이미 존재하는 데이터는 건너뛴다.
-- =====================================================================

/* ROLE 생성 */
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(0, null, '관리자', 'ROLE_ADMIN', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(1, null, '매니저', 'ROLE_MANAGER', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(2, null, 'DBA', 'ROLE_DBA', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(3, null, '사용자', 'ROLE_USER', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(4, null, '뷰어 사용자', 'ROLE_VIEWER', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(5, null, '권한 미승인 사용자', 'ROLE_NOT_APPROVE', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(6, null, '익명 사용자', 'ROLE_ANONYMOUS', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;

/* 유저 정보 생성 */
insert into account(id, password, username, name, is_password_check,created_by, updated_by, is_active) values(0, '{bcrypt}$2a$10$GN1YfMyJLcWhDuslP6P/UuqRwIfJk2VF5tl9mXsRjLJ18ivQfIAoW', 'admin', '관리자', false, 'SYSTEM', 'SYSTEM', true) ON CONFLICT (id) DO NOTHING;
insert into account_roles(account_id, role_id) values(0,0) on CONFLICT (account_id, role_id) DO NOTHING;

insert into project(is_active, project_id, created_by, project_name, updated_by, is_auto_active, account_id) values(true, 0, 'SYSTEM', 'MAIN', 'SYSTEM', true, 0) ON CONFLICT (project_id) DO NOTHING;

insert into project_member(account_id, id, project_id, created_by, updated_by, is_active) values (0, 0, 0, 'SYSTEM', 'SYSTEM', true) on CONFLICT (ID) do nothing;

/* 게시판 카테고리 */
INSERT INTO board_category(category_id, category_name, order_number, use_yn) VALUES ('NOTICE', '공지사항', 0, 'Y') ON CONFLICT (category_id) do nothing;
