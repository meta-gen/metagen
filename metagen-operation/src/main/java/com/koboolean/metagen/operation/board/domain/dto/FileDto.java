package com.koboolean.metagen.operation.board.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class FileDto {
    
    /* File 번호 */
    private long fileId;

    /* 서버에 저장되는 File 이름 */
    private String fimeName;

    /* 실제 File 이름 */
    private String orginalFileName;

    /* File 확장자 */
    private String fileExtention;

    /* File 경로*/
    private String filePath;
}
