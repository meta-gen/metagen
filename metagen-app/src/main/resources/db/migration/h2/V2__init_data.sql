-- =====================================================================
-- MetaGen 초기 데이터 (V2) : 권한, 관리자 계정(admin/admin), 기본 프로젝트, 게시판 카테고리
-- 기존 DB(baseline) 에도 적용되므로 이미 존재하는 데이터는 건너뛴다.
-- =====================================================================

/* ROLE 생성 */
INSERT INTO role (role_id, is_expression, role_desc, role_name, created_by, updated_by)
SELECT 0, null, '관리자', 'ROLE_ADMIN', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE ROLE_ID = 0);
INSERT INTO role (role_id, is_expression, role_desc, role_name, created_by, updated_by)
SELECT 1, null, '매니저', 'ROLE_MANAGER', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE ROLE_ID = 1);
INSERT INTO role (role_id, is_expression, role_desc, role_name, created_by, updated_by)
SELECT 2, null, 'DBA', 'ROLE_DBA', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE ROLE_ID = 2);
INSERT INTO role (role_id, is_expression, role_desc, role_name, created_by, updated_by)
SELECT 3, null, '사용자', 'ROLE_USER', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE ROLE_ID = 3);
INSERT INTO role (role_id, is_expression, role_desc, role_name, created_by, updated_by)
SELECT 4, null, '뷰어 사용자', 'ROLE_VIEWER', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE ROLE_ID = 4);
INSERT INTO role (role_id, is_expression, role_desc, role_name, created_by, updated_by)
SELECT 5, null, '권한 미승인 사용자', 'ROLE_NOT_APPROVE', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE ROLE_ID = 5);
INSERT INTO role (role_id, is_expression, role_desc, role_name, created_by, updated_by)
SELECT 6, null, '익명 사용자', 'ROLE_ANONYMOUS', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE ROLE_ID = 6);

/* 유저 정보 생성 */
INSERT into account (id, password, username, name, is_password_check,created_by, updated_by, is_active)
SELECT 0, '{bcrypt}$2a$10$GN1YfMyJLcWhDuslP6P/UuqRwIfJk2VF5tl9mXsRjLJ18ivQfIAoW', 'admin', '관리자', false, 'SYSTEM', 'SYSTEM', true
WHERE NOT EXISTS (SELECT 1 FROM account WHERE id = 0);
INSERT INTO account_roles (account_id, role_id)
SELECT 0,0
WHERE NOT EXISTS (SELECT 1 FROM account_roles WHERE account_id = 0 AND role_id = 0);

INSERT INTO project (is_active, project_id, created_by, project_name, updated_by, is_auto_active, account_id)
SELECT true, 0, 'SYSTEM', 'MAIN', 'SYSTEM', true, 0
WHERE NOT EXISTS (SELECT 1 FROM project WHERE project_id = 0);

INSERT INTO project_member (account_id, id, project_id, created_by, updated_by, is_active)
SELECT 0, 0, 0, 'SYSTEM', 'SYSTEM', true
WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE ID = 0);

/* 게시판 카테고리 */
INSERT INTO board_category (category_id, category_name, order_number, use_yn)
SELECT 'NOTICE', '공지사항', 0, 'Y'
WHERE NOT EXISTS (SELECT 1 FROM board_category WHERE category_id = 'NOTICE');
