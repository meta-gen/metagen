import {setupAjaxCsrf} from "./common/csrf.js";

setupAjaxCsrf();

$(document).ready(() => {
    $("#noticeTable tbody tr[data-id]").each(function () {
        $(this).css("cursor", "pointer").on("click", function () {
            const noticeId = $(this).data("id");

            if (noticeId) {
                const popup = window.open(
                    `/popup/noticePopupDetail/${noticeId}`,
                    "공지사항 상세보기",
                    "width=700,height=800,resizable=yes,scrollbars=yes"
                );
                popup.focus();
            }
        });
    });
});
