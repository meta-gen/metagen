import {setupAjaxCsrf} from "../common/csrf.js";

setupAjaxCsrf();

$(document).ready(function () {

    // 저장 버튼 클릭 시
    $("#save-button").on("click", function (e) {

        e.preventDefault(); // 폼 기본 제출 막기

        saveNotice(type);
    });

    // 페이지 로드 시 파라미터로 데이터 조회
    const params = new URLSearchParams(window.location.search);
    const type = params.get("type");

    $('#type').val(type);

    if (type === "modify") {
        const id = params.get("id");
        $("#id").val(id);

        $("#edit-button").removeAttr("hidden");
        $("#save-button").attr("hidden", true);

        $("#notice_title").html("공지사항 수정");

        $("#notice-project input").prop("disabled", true);

        // 상세조회
        $.ajax({
            url: `/api/selectNotice/detail/${id}`,
            type: "GET",
            success: (response) => {
                if (response.result) {
                    const board = response.board;

                    $("#title").val(board.title);
                    $("#content").val(board.content);
                }
            }
        });
    } else {
        // 등록
        $("#save-button").removeAttr("hidden");
        $("#edit-button").attr("hidden", true);

        $("#title").prop("readonly", false);
        $("#content").prop("readonly", false);
        $("#noticeFile").prop("disabled", false);
    }

    $("#edit-button").on("click", function () {
        $("#title").prop("readonly", false);
        $("#content").prop("readonly", false);
        $("#noticeFile").prop("disabled", false);

        $("#save-button").removeAttr("hidden");
        $("#edit-button").attr("hidden", true);
    });

});


/**
 * 공지사항 등록
 */
function saveNotice(type) {

    const $form = $("#editNoticeForm");

    const msg = type === "add" ? "저장" : "수정";

    // 필수값 설정
    const requiredFields = [

        {name: "title", label: "공지사항 제목"}
        , {name: "content", label: "공지사항 내용"}
    ];

    // 필수값 검증
    for (const field of requiredFields) {

        const value = $form.find(`[name='${field.name}']`).val();

        if (!value || value.trim() === "") {

            alert(`${field.label}은(는) 필수 입력 항목입니다.`);

            return;
        }
    }

    const projectIds = $form.find("input[name='projectIds']:checked")
        .map(function() {
            return $(this).val();
        }).get();

    const noticeData = {
        id: $form.find("[name='id']").val()
        , title: $form.find("[name='title']").val()
        , content: $form.find("[name='content']").val()
        , projectIds : projectIds
    };

    const method = type === "add" ? "POST" : "PUT";

    $.ajax({
        url: "/api/saveNotice" // 필요 시 type에 따라 URL 분기 가능
        , type: method
        , data: JSON.stringify(noticeData)
        , success: (response) => {

            if (response.result) {

                // 부모로 데이터 전달
                if (window.opener && typeof window.opener.popupFunction?.noticeSaveSuccess === 'function') {


                    window.opener.popupFunction.noticeSaveSuccess();
                } else {

                    alert("부모 창과의 통신이 불가능합니다.");
                }

                window.close();
            }
        }
    });
}
