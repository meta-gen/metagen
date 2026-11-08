import {setupAjaxCsrf} from "./common/csrf.js";
import {openNoticeDetail} from "./popup/notice_popup.js";

setupAjaxCsrf();

/**
 * 날짜를 "3분 전", "2일 전" 같은 상대 시간으로 변환한다. (7일이 넘으면 원래 날짜 유지)
 */
function toRelativeTime(date) {
    const diffSec = Math.floor((Date.now() - date.getTime()) / 1000);

    if (diffSec < 60) return "방금 전";
    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) return `${diffMin}분 전`;
    const diffHour = Math.floor(diffMin / 60);
    if (diffHour < 24) return `${diffHour}시간 전`;
    const diffDay = Math.floor(diffHour / 24);
    if (diffDay < 7) return `${diffDay}일 전`;
    return null;
}

function openNoticePopup(noticeId) {
    openNoticeDetail(noticeId);
}

$(document).ready(() => {
    // 상대 시간 표시 (마우스를 올리면 title 로 원래 일시 노출)
    $(".js-relative-time").each(function () {
        const date = new Date($(this).attr("datetime"));
        if (isNaN(date.getTime())) return;

        const relative = toRelativeTime(date);
        if (relative) $(this).text(relative);
    });

    // 공지사항 상세 팝업
    $("#noticeList").on("click", ".notice-item[data-id]", function () {
        openNoticePopup($(this).data("id"));
    }).on("keydown", ".notice-item[data-id]", function (e) {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            openNoticePopup($(this).data("id"));
        }
    });
});
