package com.koboolean.metagen.board.domain.entity;

import java.io.Serializable;

import com.koboolean.metagen.home.jpa.BaseEntity;

import groovy.transform.builder.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Builder
@Getter
@AllArgsConstructor
public class Board extends BaseEntity implements Serializable {

    /* 게시글 번호 */
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private long id;

    /* 회원 아이디 */
    @Column(length = 16, nullable = false)
    private String username;

    /* 게시글 제목 */
    @Column(length = 100, nullable = false)
    private String title;

    /* 게시글 내용 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /* 조회수 */
    @Column(name = "hit_count", nullable = false)
    private int hitCount;

    /* 삭제 여부 */
    @Column(name = "delete_yn", length = 1, nullable = false)
    private char deleteYn;

    /* 게시판 카테고리 아이디 - 연관관계에서 매핑됨 */
    // @Column(name = "category_id", length = 6, nullable = false)
    // private String categoryId;

    /* 연관 관계 Mapping */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private BoardCategory boardCategory;
}
