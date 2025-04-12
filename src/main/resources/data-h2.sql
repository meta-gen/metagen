-- Autogenerated: do not edit this file

CREATE TABLE IF NOT EXISTS BATCH_JOB_INSTANCE  (
                                                   JOB_INSTANCE_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                                   VERSION BIGINT,
                                                   JOB_NAME VARCHAR(100) NOT NULL,
                                                   JOB_KEY VARCHAR(32) NOT NULL,
                                                   CONSTRAINT JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY)
);

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION  (
                                                    JOB_EXECUTION_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                                    VERSION BIGINT,
                                                    JOB_INSTANCE_ID BIGINT NOT NULL,
                                                    CREATE_TIME TIMESTAMP(9) NOT NULL,
                                                    START_TIME TIMESTAMP(9) DEFAULT NULL,
                                                    END_TIME TIMESTAMP(9) DEFAULT NULL,
                                                    STATUS VARCHAR(10),
                                                    EXIT_CODE VARCHAR(2500),
                                                    EXIT_MESSAGE VARCHAR(2500),
                                                    LAST_UPDATED TIMESTAMP(9),
                                                    CONSTRAINT JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID)
                                                        REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
);

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS  (
                                                           JOB_EXECUTION_ID BIGINT NOT NULL,
                                                           PARAMETER_NAME VARCHAR(100) NOT NULL,
                                                           PARAMETER_TYPE VARCHAR(100) NOT NULL,
                                                           PARAMETER_VALUE VARCHAR(2500),
                                                           IDENTIFYING CHAR(1) NOT NULL,
                                                           CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID)
                                                               REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION  (
                                                     STEP_EXECUTION_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                                     VERSION BIGINT NOT NULL,
                                                     STEP_NAME VARCHAR(100) NOT NULL,
                                                     JOB_EXECUTION_ID BIGINT NOT NULL,
                                                     CREATE_TIME TIMESTAMP(9) NOT NULL,
                                                     START_TIME TIMESTAMP(9) DEFAULT NULL,
                                                     END_TIME TIMESTAMP(9) DEFAULT NULL,
                                                     STATUS VARCHAR(10),
                                                     COMMIT_COUNT BIGINT,
                                                     READ_COUNT BIGINT,
                                                     FILTER_COUNT BIGINT,
                                                     WRITE_COUNT BIGINT,
                                                     READ_SKIP_COUNT BIGINT,
                                                     WRITE_SKIP_COUNT BIGINT,
                                                     PROCESS_SKIP_COUNT BIGINT,
                                                     ROLLBACK_COUNT BIGINT,
                                                     EXIT_CODE VARCHAR(2500),
                                                     EXIT_MESSAGE VARCHAR(2500),
                                                     LAST_UPDATED TIMESTAMP(9),
                                                     CONSTRAINT JOB_EXEC_STEP_FK FOREIGN KEY (JOB_EXECUTION_ID)
                                                         REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_CONTEXT  (
                                                             STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                                             SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                                             SERIALIZED_CONTEXT LONGVARCHAR,
                                                             CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID)
                                                                 REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
);

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_CONTEXT  (
                                                            JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                                            SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                                            SERIALIZED_CONTEXT LONGVARCHAR,
                                                            CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID)
                                                                REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE SEQUENCE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ;
CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ;
CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_SEQ;

/* 권한 계층 적용 */
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (1,'ROLE_ADMIN',null, 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (2,'ROLE_MANAGER','1', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (3,'ROLE_DBA','2', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (4,'ROLE_USER','2', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (5,'ROLE_USER','3', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (6,'ROLE_VIEWER','4', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (7,'ROLE_VIEWER','5', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (8,'ROLE_NOT_APPROVE','6', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (9,'ROLE_NOT_APPROVE','7', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (10,'ROLE_ANONYMOUS','8', 'SYSTEM', 'SYSTEM');
merge into role_hierarchy (id, role_name, parent_id, created_by, updated_by) key(id) values (11,'ROLE_ANONYMOUS','9', 'SYSTEM', 'SYSTEM');

/* ROLE 생성 */
merge into role (role_id, is_expression, role_desc, role_name, created_by, updated_by) key(ROLE_ID) values (0, null, '관리자', 'ROLE_ADMIN', 'SYSTEM', 'SYSTEM');
merge into role (role_id, is_expression, role_desc, role_name, created_by, updated_by) key(ROLE_ID) values (1, null, '매니저', 'ROLE_MANAGER', 'SYSTEM', 'SYSTEM');
merge into role (role_id, is_expression, role_desc, role_name, created_by, updated_by) key(ROLE_ID) values (2, null, 'DBA', 'ROLE_DBA', 'SYSTEM', 'SYSTEM');
merge into role (role_id, is_expression, role_desc, role_name, created_by, updated_by) key(ROLE_ID) values (3, null, '사용자', 'ROLE_USER', 'SYSTEM', 'SYSTEM');
merge into role (role_id, is_expression, role_desc, role_name, created_by, updated_by) key(ROLE_ID) values (4, null, '뷰어 사용자', 'ROLE_VIEWER', 'SYSTEM', 'SYSTEM');
merge into role (role_id, is_expression, role_desc, role_name, created_by, updated_by) key(ROLE_ID) values (5, null, '권한 미승인 사용자', 'ROLE_NOT_APPROVE', 'SYSTEM', 'SYSTEM');
merge into role (role_id, is_expression, role_desc, role_name, created_by, updated_by) key(ROLE_ID) values (6, null, '익명 사용자', 'ROLE_ANONYMOUS', 'SYSTEM', 'SYSTEM');

/* 인가 */
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (1, '*', 0, '/logout','url', '로그아웃', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (2, '*', 0, '/account','url', '내정보화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (3, '*', 0, '/api/updatePwd','url', '패스워드수정', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (4, '*', 0, '/columnManage','url', '컬럼관리화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (5, '*', 0, '/api/updateName','url', '사용자명수정', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (6, '*', 0, '/codeRule','url', '코드규칙화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (7, '*', 0, '/designManage','url', '설계서화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (8, '*', 0, '/userManage','url', '사용자관리화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (9, '*', 0, '/systemLog','url', '시스템로그화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (10, '*', 0, '/dataDictionary','url', '데이터사전화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (11, '*', 0, '/accessManage','url', '인가관리화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (12, '*', 0, '/testManage','url', '테스트시나리오화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (13, '*', 0, '/notice','url', '공지사항화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (14, '*', 0, '/api/updateRole','url', '권한수정', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (15, '*', 0, '/api/downloadTemplate/**','url', '템플릿다운로드', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (16, '*', 0, '/projectManage','url', '프로젝트관리화면', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (17, '*', 0, '/api/checkProjectList/**','url', '접근가능프로젝트 조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (18, '*', 0, '/api/getSystemLog/**','url', '시스템로그조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (19, '*', 0, '/api/getStandardTerms/**','url', '표준용어조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (20, '*', 0, '/api/getStandardWords/**','url', '표준단어조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (21, '*', 0, '/api/getStandardDomains/**','url', '표준도메인조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (22, '*', 0, '/api/uploadDataDictionaryExcelFile/**','url', '데이터사전 엑셀파일업로드', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (23, '*', 0, '/popup/standardTermSearch','url', '표준용어세부조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (24, '*', 0, '/api/approvalStandardDomains/**','url', '표준도메인승인/승인취소', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (25, '*', 0, '/api/approvalStandardWords/**','url', '표준단어승인/승인취소', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (26, '*', 0, '/api/approvalStandardTerms/**','url', '표준용어승인/승인취소', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (27, '*', 0, '/api/insertDataDictionary/**','url', '데이터사전등록', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (28, '*', 0, '/api/deleteDataDictionary/**','url', '데이터사전삭제', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (29, '*', 0, '/api/updateDataDictionary/**','url', '데이터사전수정', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (30, '*', 0, '/api/getProject/**','url', '프로젝트조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (31, '*', 0, '/api/saveProject/**','url', '프로젝트저장', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (32, '*', 0, '/api/deleteProject/**','url', '프로젝트삭제', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (33, '*', 0, '/api/selectUser/**','url', '사용자조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (34, '*', 0, '/api/saveUser/**','url', '사용자저장', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (35, '*', 0, '/api/deleteUser/**','url', '사용자삭제', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (36, '*', 0, '/api/selectAccess/**','url', '인가조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (37, '*', 0, '/api/updateAccess/**','url', '인가수정', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (38, '*', 0, '/api/selectTable/**','url', '테이블 조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (39, '*', 0, '/api/updateTable/**','url', '테이블 수정', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (40, '*', 0, '/api/deleteTable/**','url', '테이블 삭제', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (41, '*', 0, '/api/selectColumn/**','url', '테이블컬럼 조회', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (42, '*', 0, '/api/updateColumn/**','url', '테이블컬럼 수정', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (43, '*', 0, '/api/deleteColumn/**','url', '테이블컬럼 삭제', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (44, '*', 0, '/api/uploadMetadata/**','url', '메타데이터 업로드', 'SYSTEM', 'SYSTEM');
merge into RESOURCES (resource_id, http_method, order_num, resource_name, resource_type, resource_desc, created_by, updated_by) key(resource_id) values (45, '*', 0, '/tableManage','url', '테이블관리화면', 'SYSTEM', 'SYSTEM');

-- ROLE_NOT_APPROVE
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (1,5); -- 로그아웃
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (2,5); -- 내정보화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (3,5); -- 패스워드수정
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (5,5); -- 사용자명수정
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (13,5); -- 공지사항화면

-- ROLE_VIEWER
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (4,4); -- 컬럼관리화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (6,4); -- 코드규칙화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (7,4); -- 설계서화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (8,4); -- 사용자관리화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (10,4); -- 데이터사전화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (11,4); -- 인가관리화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (12,4); -- 테스트시나리오화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (16,4); -- 프로젝트관리화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (19,4); -- 표준용어조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (20,4); -- 표준단어조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (21,4); -- 표준도메인조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (23,4); -- 표준용어세부조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (33,4); -- 사용자조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (36,4); -- 인가조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (30,4); -- 프로젝트조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (38,4); -- 테이블설계 조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (45,4); -- 테이블관리화면

-- ROLE_USER
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (15,3); -- 템플릿다운로드
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (22,3); -- 데이터사전 엑셀파일업로드
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (27,3); -- 데이터사전등록
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (28,3); -- 데이터사전삭제
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (29,3); -- 데이터사전수정
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (39,3); -- 테이블설계 수정
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (40,3); -- 테이블설계 삭제
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (41,3); -- 테이블컬럼 조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (42,3); -- 테이블컬럼 수정
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (43,3); -- 테이블컬럼 삭제

-- ROLE_DBA
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (31,2) ; -- 프로젝트저장
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (32,2) ; -- 프로젝트삭제
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (9,2); -- 시스템로그화면
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (18,2) ; -- 시스템로그조회
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (24,2) ; -- 표준도메인승인/승인취소
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (25,2) ; -- 표준단어승인/승인취소
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (26,2) ; -- 표준용어승인/승인취소

-- ROLE_ADMIN
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (14,0); -- 권한수정
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (34,0); -- 사용자저장
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (35,0); -- 사용자삭제
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (37,0); -- 인가수정
merge into role_resources(resource_id, role_id) key(resource_id, role_id) values (44,0); -- 메타데이터 업로드

-- ROLE_ANONYMOUS
merge into role_resources(resource_id, role_id) values (17,6); -- 접근가능프로젝트 조회

/* 유저 정보 생성 */
merge into account (id, password, username, name, is_password_check,created_by, updated_by, is_active) key(id) values (0, '{bcrypt}$2a$10$GN1YfMyJLcWhDuslP6P/UuqRwIfJk2VF5tl9mXsRjLJ18ivQfIAoW', 'admin', '관리자', false, 'SYSTEM', 'SYSTEM', true);
merge into account_roles (account_id, role_id) key(account_id, role_id) values (0,0);

merge into project (is_active, project_id, created_by, project_name, updated_by, is_auto_active, account_id) key(project_id) values (true, 0, 'SYSTEM', 'MAIN', 'SYSTEM', true, 0);

merge into project_member (account_id, id, project_id, created_by, updated_by, is_active) key(ID) values (0, 0, 0, 'SYSTEM', 'SYSTEM', true);
