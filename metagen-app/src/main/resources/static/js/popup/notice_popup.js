import {closePopupDialog, openPopupDialog, setPopupDialogTitle} from "../common/popup_dialog.js";

/**
 * 공지사항 상세보기 다이얼로그 (대시보드 / 공지사항 목록)
 * @param id      공지사항 ID
 * @param onClose 닫힌 뒤 호출 (예: 목록 새로고침 - 읽음 표시 갱신)
 */
export function openNoticeDetail(id, {onClose} = {}) {
    if (!id) return;

    openPopupDialog({
        url: `/popup/noticePopupDetail/${encodeURIComponent(id)}`,
        title: "공지사항",
        size: "large",
        onClose
    });
}

/**
 * 공지사항 등록/수정 다이얼로그
 * @param type      "add" | "modify"
 * @param id        수정할 공지사항 ID
 * @param onSuccess 저장 성공 시 호출 (다이얼로그는 닫힌 뒤 호출)
 */
export function openNoticeSave({type, id, onSuccess}) {
    const query = type === "modify" ? `type=modify&id=${encodeURIComponent(id)}` : "type=add";

    openPopupDialog({
        url: `/popup/noticePopupSave?${query}`,
        title: type === "modify" ? "공지사항 수정" : "공지사항 등록",
        size: "large",
        onLoad: ($root) => initNoticeSave($root, type, id, onSuccess)
    });
}

function initNoticeSave($root, type, id, onSuccess) {
    const q = (selector) => $root.find(selector);

    q("#type").val(type);

    if (type === "modify") {
        q("#id").val(id);

        q("#edit-button").removeAttr("hidden");
        q("#save-button").attr("hidden", true);
        q("#notice-project input").prop("disabled", true);

        // 상세조회
        $.ajax({
            url: `/api/selectNotice/detail/${encodeURIComponent(id)}`,
            type: "GET",
            success: (response) => {
                if (response.result) {
                    q("#title").val(response.board.title);
                    q("#content").val(response.board.content);
                }
            }
        });
    } else {
        // 등록
        q("#save-button").removeAttr("hidden");
        q("#edit-button").attr("hidden", true);
        q("#title, #content").prop("readonly", false);
    }

    q("#edit-button").on("click", () => {
        q("#title, #content").prop("readonly", false);
        q("#save-button").removeAttr("hidden");
        q("#edit-button").attr("hidden", true);
        setPopupDialogTitle("공지사항 수정");
        q("#title").trigger("focus");
    });

    q("#save-button").on("click", (e) => {
        e.preventDefault();
        saveNotice($root, type, onSuccess);
    });
}

/**
 * 공지사항 저장
 */
function saveNotice($root, type, onSuccess) {
    const $form = $root.find("#editNoticeForm");

    const requiredFields = [
        {name: "title", label: "공지사항 제목"},
        {name: "content", label: "공지사항 내용"}
    ];

    for (const field of requiredFields) {
        const value = $form.find(`[name='${field.name}']`).val();
        if (!value || value.trim() === "") {
            window.openAlert(`${field.label}은(는) 필수 입력 항목입니다.`);
            return;
        }
    }

    const projectIds = $form.find("input[name='projectIds']:checked")
        .map(function () {
            return $(this).val();
        }).get();

    const noticeData = {
        id: $form.find("[name='id']").val(),
        title: $form.find("[name='title']").val(),
        content: $form.find("[name='content']").val(),
        projectIds: projectIds
    };

    $.ajax({
        url: "/api/saveNotice",
        type: type === "add" ? "POST" : "PUT",
        data: JSON.stringify(noticeData),
        success: (response) => {
            if (response.result) {
                closePopupDialog();
                if (typeof onSuccess === "function") onSuccess();
            }
        }
    });
}
