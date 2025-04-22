/* 권한 계층 적용 */
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (1,'ROLE_ADMIN',null, 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (2,'ROLE_MANAGER','1', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (3,'ROLE_DBA','2', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (4,'ROLE_USER','2', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (5,'ROLE_USER','3', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (6,'ROLE_VIEWER','4', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (7,'ROLE_VIEWER','5', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (8,'ROLE_NOT_APPROVE','6', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (9,'ROLE_NOT_APPROVE','7', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (10,'ROLE_ANONYMOUS','8', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;
insert into role_hierarchy (id, role_name, parent_id, created_by, updated_by) values (11,'ROLE_ANONYMOUS','9', 'SYSTEM', 'SYSTEM') ON CONFLICT (id) DO NOTHING;

/* ROLE 생성 */
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(0, null, '관리자', 'ROLE_ADMIN', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(1, null, '매니저', 'ROLE_MANAGER', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(2, null, 'DBA', 'ROLE_DBA', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(3, null, '사용자', 'ROLE_USER', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(4, null, '뷰어 사용자', 'ROLE_VIEWER', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(5, null, '권한 미승인 사용자', 'ROLE_NOT_APPROVE', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;
insert into role(role_id, is_expression, role_desc, role_name, created_by, updated_by) values(6, null, '익명 사용자', 'ROLE_ANONYMOUS', 'SYSTEM', 'SYSTEM') ON CONFLICT (ROLE_ID) DO NOTHING;

/* 인가 */
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (1, '*', 0, '/logout','url', '로그아웃', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (2, '*', 0, '/account','url', '내정보화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (3, '*', 0, '/api/updatePwd','url', '패스워드수정', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (4, '*', 0, '/columnManage','url', '컬럼관리화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (5, '*', 0, '/api/updateName','url', '사용자명수정', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (6, '*', 0, '/codeRuleManage','url', '코드규칙관리 화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (7, '*', 0, '/designManage','url', '설계서화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (8, '*', 0, '/userManage','url', '사용자관리화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (9, '*', 0, '/systemLog','url', '시스템로그화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (10, '*', 0, '/dataDictionary','url', '데이터사전화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (11, '*', 0, '/accessManage','url', '인가관리화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (12, '*', 0, '/testManage','url', '테스트시나리오화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (13, '*', 0, '/notice','url', '공지사항화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (14, '*', 0, '/api/updateRole','url', '권한수정', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (15, '*', 0, '/api/downloadTemplate/**','url', '템플릿다운로드', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (16, '*', 0, '/projectManage','url', '프로젝트관리화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (17, '*', 0, '/api/checkProjectList/**','url', '접근가능프로젝트 조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (18, '*', 0, '/api/getSystemLog/**','url', '시스템로그조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (19, '*', 0, '/api/getStandardTerms/**','url', '표준용어조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (20, '*', 0, '/api/getStandardWords/**','url', '표준단어조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (21, '*', 0, '/api/getStandardDomains/**','url', '표준도메인조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (22, '*', 0, '/api/uploadDataDictionaryExcelFile/**','url', '데이터사전 엑셀파일업로드', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (23, '*', 0, '/popup/standardTermSearch','url', '표준용어세부조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (24, '*', 0, '/api/approvalStandardDomains/**','url', '표준도메인승인/승인취소', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (25, '*', 0, '/api/approvalStandardWords/**','url', '표준단어승인/승인취소', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (26, '*', 0, '/api/approvalStandardTerms/**','url', '표준용어승인/승인취소', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (27, '*', 0, '/api/insertDataDictionary/**','url', '데이터사전등록', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (28, '*', 0, '/api/deleteDataDictionary/**','url', '데이터사전삭제', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (29, '*', 0, '/api/updateDataDictionary/**','url', '데이터사전수정', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (30, '*', 0, '/api/getProject/**','url', '프로젝트조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (31, '*', 0, '/api/saveProject/**','url', '프로젝트저장', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (32, '*', 0, '/api/deleteProject/**','url', '프로젝트삭제', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (33, '*', 0, '/api/selectUser/**','url', '사용자조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (34, '*', 0, '/api/saveUser/**','url', '사용자저장', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (35, '*', 0, '/api/deleteUser/**','url', '사용자삭제', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (36, '*', 0, '/api/selectAccess/**','url', '인가조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (37, '*', 0, '/api/updateAccess/**','url', '인가수정', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (38, '*', 0, '/api/selectTable/**','url', '테이블 조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (39, '*', 0, '/api/saveTable/**','url', '테이블 저장', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (40, '*', 0, '/api/deleteTable/**','url', '테이블 삭제', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (41, '*', 0, '/api/selectColumn/**','url', '테이블컬럼 조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (42, '*', 0, '/api/saveColumn/**','url', '테이블컬럼 저장', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (43, '*', 0, '/api/deleteColumn/**','url', '테이블컬럼 삭제', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (44, '*', 0, '/api/uploadColumnExcelFile/**','url', '컬럼 업로드', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (45, '*', 0, '/tableManage','url', '테이블관리화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (46, '*', 0, '/api/uploadTableExcelFile/**','url', '테이블 업로드', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (47, '*', 0, '/api/saveNotice/**','url', '공지사항 등록/수정', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (48, '*', 0, '/api/selectNotice/**','url', '공지사항 조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (49, '*', 0, '/api/deleteNotice/**','url', '공지사항 삭제', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (50, '*', 0, '/popup/columTableSearch/**','url', '테이블조회 팝업화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (51, '*', 0, '/codeRule','url', '코드규칙 화면', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (52, '*', 0, '/api/selectCodeRuleManage/**','url', '코드규칙관리 조회', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (53, '*', 0, '/api/saveCodeRuleManage/**','url', '코드규칙관리 저장', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (54, '*', 0, '/api/deleteCodeRuleManage/**','url', '코드규칙관리 삭제', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;
INSERT INTO RESOURCES(resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by)
VALUES (55, '*', 0, '/popup/codeRulePopup','url', '코드규칙관리 등록/수정 팝업', 'SYSTEM', 'SYSTEM')
ON CONFLICT (resource_id) DO NOTHING;


-- ROLE_NOT_APPROVE
insert into role_resources(resource_id, role_id) values (1,5) on CONFLICT DO NOTHING; -- 로그아웃
insert into role_resources(resource_id, role_id) values (2,5) on CONFLICT DO NOTHING; -- 내정보화면
insert into role_resources(resource_id, role_id) values (3,5) on CONFLICT DO NOTHING; -- 패스워드수정
insert into role_resources(resource_id, role_id) values (5,5) on CONFLICT DO NOTHING; -- 사용자명수정
insert into role_resources(resource_id, role_id) values (13,5) on CONFLICT DO NOTHING; -- 공지사항화면
insert into role_resources(resource_id, role_id) values (48,5) on CONFLICT DO NOTHING; -- 공지사항조회

-- ROLE_VIEWER
insert into role_resources(resource_id, role_id) values (4,4) on CONFLICT DO NOTHING; -- 컬럼관리화면
insert into role_resources(resource_id, role_id) values (6,4) on CONFLICT DO NOTHING; -- 코드규칙관리 화면
insert into role_resources(resource_id, role_id) values (7,4) on CONFLICT DO NOTHING; -- 설계서화면
insert into role_resources(resource_id, role_id) values (8,4) on CONFLICT DO NOTHING; -- 사용자관리화면
insert into role_resources(resource_id, role_id) values (10,4) on CONFLICT DO NOTHING; -- 데이터사전화면
insert into role_resources(resource_id, role_id) values (11,4) on CONFLICT DO NOTHING; -- 인가관리화면
insert into role_resources(resource_id, role_id) values (12,4) on CONFLICT DO NOTHING; -- 테스트시나리오화면
insert into role_resources(resource_id, role_id) values (16,4) on CONFLICT DO NOTHING; -- 프로젝트관리화면
insert into role_resources(resource_id, role_id) values (19,4) on CONFLICT DO NOTHING; -- 표준용어조회
insert into role_resources(resource_id, role_id) values (20,4) on CONFLICT DO NOTHING; -- 표준단어조회
insert into role_resources(resource_id, role_id) values (21,4) on CONFLICT DO NOTHING; -- 표준도메인조회
insert into role_resources(resource_id, role_id) values (23,4) on CONFLICT DO NOTHING; -- 표준용어세부조회
insert into role_resources(resource_id, role_id) values (33,4) on CONFLICT DO NOTHING; -- 사용자조회
insert into role_resources(resource_id, role_id) values (36,4) on CONFLICT DO NOTHING; -- 인가조회
insert into role_resources(resource_id, role_id) values (30,4) on CONFLICT DO NOTHING; -- 프로젝트조회
insert into role_resources(resource_id, role_id) values (38,4) on CONFLICT DO NOTHING; -- 테이블설계 조회
insert into role_resources(resource_id, role_id) values (45,4) on CONFLICT DO NOTHING; -- 테이블관리화면
insert into role_resources(resource_id, role_id) values (50,4) on CONFLICT DO NOTHING; -- 테이블조회 팝업화면
insert into role_resources(resource_id, role_id) values (51,4) on CONFLICT DO NOTHING; -- 코드규칙 화면
insert into role_resources(resource_id, role_id) values (52,4) on CONFLICT DO NOTHING; -- 코드규칙관리 조회

-- ROLE_USER
insert into role_resources(resource_id, role_id) values (15,3) on CONFLICT DO NOTHING; -- 템플릿다운로드
insert into role_resources(resource_id, role_id) values (22,3) on CONFLICT DO NOTHING; -- 데이터사전 엑셀파일업로드
insert into role_resources(resource_id, role_id) values (27,3) on CONFLICT DO NOTHING; -- 데이터사전등록
insert into role_resources(resource_id, role_id) values (28,3) on CONFLICT DO NOTHING; -- 데이터사전삭제
insert into role_resources(resource_id, role_id) values (29,3) on CONFLICT DO NOTHING; -- 데이터사전수정
insert into role_resources(resource_id, role_id) values (39,3) on CONFLICT DO NOTHING; -- 테이블설계 수정
insert into role_resources(resource_id, role_id) values (40,3) on CONFLICT DO NOTHING; -- 테이블설계 삭제
insert into role_resources(resource_id, role_id) values (41,3) on CONFLICT DO NOTHING; -- 테이블컬럼 조회
insert into role_resources(resource_id, role_id) values (42,3) on CONFLICT DO NOTHING; -- 테이블컬럼 수정
insert into role_resources(resource_id, role_id) values (43,3) on CONFLICT DO NOTHING; -- 테이블컬럼 삭제
insert into role_resources(resource_id, role_id) values (44,3) on CONFLICT DO NOTHING; -- 컬럼 업로드
insert into role_resources(resource_id, role_id) values (46,3) on CONFLICT DO NOTHING; -- 테이블 업로드

-- ROLE_DBA
insert into role_resources(resource_id, role_id) values (31,2) on CONFLICT DO NOTHING; -- 프로젝트저장
insert into role_resources(resource_id, role_id) values (32,2) on CONFLICT DO NOTHING; -- 프로젝트삭제
insert into role_resources(resource_id, role_id) values (9,2) on CONFLICT DO NOTHING; -- 시스템로그화면
insert into role_resources(resource_id, role_id) values (18,2) on CONFLICT DO NOTHING; -- 시스템로그조회
insert into role_resources(resource_id, role_id) values (24,2) on CONFLICT DO NOTHING; -- 표준도메인승인/승인취소
insert into role_resources(resource_id, role_id) values (25,2) on CONFLICT DO NOTHING; -- 표준단어승인/승인취소
insert into role_resources(resource_id, role_id) values (26,2) on CONFLICT DO NOTHING; -- 표준용어승인/승인취소

-- ROLE_ADMIN
insert into role_resources(resource_id, role_id) values (14,0) on CONFLICT DO NOTHING; -- 권한수정
insert into role_resources(resource_id, role_id) values (34,0) on CONFLICT DO NOTHING; -- 사용자저장
insert into role_resources(resource_id, role_id) values (35,0) on CONFLICT DO NOTHING; -- 사용자삭제
insert into role_resources(resource_id, role_id) values (37,0) on CONFLICT DO NOTHING; -- 인가수정

-- ROLE_MANAGER
insert into role_resources(resource_id, role_id) values (47,1) on CONFLICT DO NOTHING; -- 공지사항 등록/수정
insert into role_resources(resource_id, role_id) values (49,1) on CONFLICT DO NOTHING; -- 공지사항 삭제
insert into role_resources(resource_id, role_id) values (53,1) on CONFLICT DO NOTHING; -- 코드규칙관리 저장
insert into role_resources(resource_id, role_id) values (54,1) on CONFLICT DO NOTHING; -- 코드규칙관리 삭제
insert into role_resources(resource_id, role_id) values (55,1) on CONFLICT DO NOTHING; -- 코드규칙관리 등록/수정 팝업


-- ROLE_ANONYMOUS
insert into role_resources(resource_id, role_id) values (17,6) on CONFLICT DO NOTHING; -- 접근가능프로젝트 조회

/* 유저 정보 생성 */
insert into account(id, password, username, name, is_password_check,created_by, updated_by, is_active) values(0, '{bcrypt}$2a$10$GN1YfMyJLcWhDuslP6P/UuqRwIfJk2VF5tl9mXsRjLJ18ivQfIAoW', 'admin', '관리자', false, 'SYSTEM', 'SYSTEM', true) ON CONFLICT (id) DO NOTHING;
insert into account_roles(account_id, role_id) values(0,0) on CONFLICT (account_id, role_id) DO NOTHING;

insert into project(is_active, project_id, created_by, project_name, updated_by, is_auto_active, account_id) values(true, 0, 'SYSTEM', 'MAIN', 'SYSTEM', true, 0) ON CONFLICT (project_id) DO NOTHING;

insert into project_member(account_id, id, project_id, created_by, updated_by, is_active) values (0, 0, 0, 'SYSTEM', 'SYSTEM', true) on CONFLICT (ID) do nothing;

/* 게시판 카테고리 */
INSERT INTO board_category(category_id, category_name, order_number, use_yn)
VALUES ('NOTICE', '공지사항', 0, 'Y') ON CONFLICT (category_id) do nothing;
;
