
/**
 * 파일 다운로드
 * @param blob
 * @param status
 * @param xhr
 */
export function downloadFile(blob, status, xhr, fileName) {
    const disposition = xhr.getResponseHeader('Content-Disposition');

    // Blob을 사용하여 파일 다운로드
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);  // 메모리 해제
}
