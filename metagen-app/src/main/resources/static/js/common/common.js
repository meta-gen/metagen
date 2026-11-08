
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

// 공통 다이얼로그(#mainConfirm) 크기 클래스 (primer.css)
const MAIN_DIALOG_SIZES = ["dialog-xl", "dialog-table-detail"];

/**
 * 공통 다이얼로그(#mainConfirm)를 지정한 크기로 연 뒤, 닫히면 기본 크기로 되돌린다. window.openDialog("div", ...) 다음에 부른다.
 *
 * 다이얼로그의 close 이벤트는 close() 직후가 아니라 잠시 뒤에 발생한다. 그래서 A 를 닫고 곧바로 같은 다이얼로그로 B 를 열면
 * A 의 close 이벤트가 B 가 열린 뒤에 도착한다. 열려 있는 동안 도착한 close 이벤트는 무시해야 B 의 크기가 지워지지 않는다.
 *
 * @param sizeClass dialog-xl(컬럼 편집) | dialog-table-detail(테이블 상세)
 */
export function setMainDialogSize(sizeClass) {
    const dialog = document.getElementById("mainConfirm");
    if (!dialog) return;

    dialog.classList.remove(...MAIN_DIALOG_SIZES); // 이전 화면의 크기가 남아 있으면 지운다
    dialog.classList.add(sizeClass);

    const onClose = () => {
        if (dialog.open) return; // 이전 화면의 늦게 도착한 close 이벤트
        dialog.classList.remove(sizeClass);
        dialog.removeEventListener("close", onClose);
    };
    dialog.addEventListener("close", onClose);
}
