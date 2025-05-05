package com.koboolean.metagen.board.domain.entity;

import java.io.Serializable;

import com.koboolean.metagen.home.jpa.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class File extends BaseEntity implements Serializable {
    
    /* File 번호 
     * 각 파일이 가진 고유한 번호. 파일들을 식별하는 key 역할을 한다.
     */
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private long fileId;

    /* 서버에 저장되는 File 이름 */
    @Column(length = 200, nullable = false)
    private String fimeName;

    /* 실제 File 이름 
     * 다운로드 시 이 이름으로 생성된다.
     */
    @Column(length = 200, nullable = false)
    private String orginalFileName;

    /* File 확장자 
     * 서버에 File을 저장할 땐 확장자를 포함하지 않기 때문에 다운로드 시 붙여줘야한다.
     * 그렇기에 확장자를 별도로 저장한다.
     */
    @Column(length = 15, nullable = false)
    private String fileExtention;

    /* File 경로
     * 파일이 저장된 경로. 이 값으로 파일이 저장된 위치를 찾는다.
     */
    @Column(length = 300, nullable = false)
    private String filePath;

    /* 연관 관계 Mapping 
     * File(N) : Board(1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Board board;
}
