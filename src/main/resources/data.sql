/* 권한 계층 적용 */
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (1,'ROLE_ADMIN',null, 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (2,'ROLE_MANAGER','1', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (3,'ROLE_DBA','1', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (4,'ROLE_USER','2', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (5,'ROLE_USER','3', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (6,'ROLE_NOT_APPROVE','4', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (7,'ROLE_NOT_APPROVE','5', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (8,'ROLE_ANONYMOUS','6', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (9,'ROLE_ANONYMOUS','7', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;

/* ROLE 생성 */
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(0, null, '관리자', 'ROLE_ADMIN', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(1, null, '매니저', 'ROLE_MANAGER', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(2, null, 'DBA', 'ROLE_DBA', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(3, null, '사용자', 'ROLE_USER', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(4, null, '권한 미승인 사용자', 'ROLE_NOT_APPROVE', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(5, null, '익명 사용자', 'ROLE_ANONYMOUS', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;

/* 인가 */
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (1, '*', 0, '/logout','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (2, '*', 0, '/account','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (3, '*', 0, '/api/updatePwd','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (4, '*', 0, '/meta','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (5, '*', 0, '/api/updateName','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (6, '*', 0, '/codeRule','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (7, '*', 0, '/designManage','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (8, '*', 0, '/manage','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (9, '*', 0, '/systemLog','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (10, '*', 0, '/dataDictionary','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (11, '*', 0, '/accessControl','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (12, '*', 0, '/testManage','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (13, '*', 0, '/notice','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (14, '*', 0, '/updateRole','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, created_by, updated_by)
VALUES (15, '*', 0, '/api/downloadTemplate/**','url', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;

insert into role_resources(resource_id, role_id) values (1,4) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (2,4) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (3,4) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (4,3) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (5,4) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (6,3) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (7,3) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (8,4) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (9,5) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (10,3) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (11,0) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (12,3) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (13,4) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (14,0) on CONFLICT (resource_id, role_id) DO NOTHING;
insert into role_resources(resource_id, role_id) values (15,4) on CONFLICT (resource_id, role_id) DO NOTHING;

/* 유저 정보 생성 */
insert into account(id, password, username, name, is_password_check,created_by, updated_by) values(0, '{bcrypt}$2a$10$GN1YfMyJLcWhDuslP6P/UuqRwIfJk2VF5tl9mXsRjLJ18ivQfIAoW', 'admin', '관리자', false, 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into account_roles(account_id, role_id) values(0,0) on CONFLICT (account_id, role_id) DO NOTHING;
