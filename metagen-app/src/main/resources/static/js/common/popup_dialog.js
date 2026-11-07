/**
 * 팝업 다이얼로그 (window.open 팝업 대체)
 * 서버에서 그린 팝업 화면 조각(/popup/... → th:fragment="content")을 받아 공통 다이얼로그(#popupDialog)에 띄운다.
 *
 * - 등록/수정 다이얼로그(#mainConfirm) 위에서도 열 수 있다. (예: 컬럼 등록 중 테이블 조회)
 * - 팝업 안의 [data-popup-close] 버튼은 다이얼로그를 닫는다.
 * - 팝업 스크립트는 반드시 전달받은 $root 안에서만 요소를 찾는다. (메인 화면과 id 가 겹칠 수 있음)
 */

const DIALOG_ID = "popupDialog";

let onCloseCallback = null;

function getDialog() {
    return document.getElementById(DIALOG_ID);
}

/**
 * @param url     팝업 화면 조각 URL (예: /popup/noticePopupDetail/1)
 * @param title   다이얼로그 제목
 * @param size    "wide"(기본) | "large"
 * @param onLoad  ($root) => void : 화면 조각을 넣은 뒤 호출 (이벤트 연결, 데이터 조회)
 * @param onClose () => void : 닫힐 때 호출
 */
export function openPopupDialog({url, title, size = "wide", onLoad, onClose}) {
    const dialog = getDialog();
    if (!dialog) {
        console.error("popupDialog 를 찾을 수 없습니다.");
        return;
    }

    $.ajax({
        url,
        type: "GET",
        dataType: "html",
        success: (html) => {
            // 이미 열려 있으면 이전 내용을 정리하고 새 내용으로 바꾼다.
            if (dialog.open) closePopupDialog();

            const $root = $(dialog).find(".popup-dialog-body");
            $root.html(html);

            setPopupDialogTitle(title);
            dialog.classList.toggle("popup-dialog-large", size === "large");
            onCloseCallback = typeof onClose === "function" ? onClose : null;

            dialog.showModal();
            dialog.querySelector(".popup-dialog-body").scrollTop = 0;

            if (typeof onLoad === "function") onLoad($root);
        }
    });
}

export function setPopupDialogTitle(title) {
    $(getDialog()).find(".popup-dialog-title").text(title ?? "");
}

export function closePopupDialog() {
    const dialog = getDialog();
    if (dialog?.open) dialog.close(); // close 이벤트에서 내용 정리
}

/**
 * 닫힐 때(닫기 버튼, X, ESC) 내용을 비워 이벤트/요소가 남지 않게 한다.
 */
$(function () {
    const dialog = getDialog();
    if (!dialog) return;

    dialog.addEventListener("close", () => {
        $(dialog).find(".popup-dialog-body").empty();

        const callback = onCloseCallback;
        onCloseCallback = null;
        if (callback) callback();
    });

    $(dialog).on("click", "[data-popup-close]", (e) => {
        e.preventDefault();
        closePopupDialog();
    });
});
