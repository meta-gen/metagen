package com.koboolean.metagen.core.project.domain.event;

/**
 * 프로젝트 삭제 직전에 발행되는 이벤트.
 * 프로젝트에 종속된 데이터(템플릿 등)를 가진 모듈이 수신하여 정리한다.
 * 동기 리스너로 처리되므로 프로젝트 삭제와 같은 트랜잭션에서 수행된다.
 */
public record ProjectDeletedEvent(Long projectId) {
}
