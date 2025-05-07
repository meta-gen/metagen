import {setupAjaxCsrf} from "../../common/csrf.js";
import {downloadFile} from "../../common/common.js";

setupAjaxCsrf();

$(document).ready(() => {

    const tableId = 'codeRuleGrid';

    $("#grd-print-codeRuleGrid").on("click", () => {
        const checkedData = getCheckedData(tableId);

        const dialogContent = $("<div></div>");
        const form = $("<form></form>").attr("id", `edit-${tableId}`);

        const message = checkedData.length === 0 ? "검색조건의 <b>전체 데이터</b>가 출력됩니다." : "<b>체크된 데이터</b>가 출력됩니다.";

        const p = $("<p></p>").html(`${message}<br/>아래의 출력 형식을 선택해주세요.`);
        form.append(p);

        [
            { id: "unitTest", text: "단위테스트 시나리오" }
            /*, { id: "integrationTest", text: "통합테스트 시나리오" }*/
        ].forEach(option => {
            const btn = $("<button></button>")
                .attr({
                    type: "button",
                    id: option.id
                }).css("margin-top", "20px")
                .addClass("btn btn-primary")
                .text(option.text)
                .on("click", () => {
                    // 여기에 각 버튼 클릭 시 동작 처리
                    console.log("선택된 출력 형식:", option.id);
                    handlePrintFormat(option.id, option.text, checkedData);
                });

            form.append(btn);
        });


        dialogContent.append(form);


        openDialog("div", {title: "테스트 시나리오 출력", content: dialogContent});
    });

});

function handlePrintFormat(formatType, formatText, checkedData) {
    let message = "";
    let type = "all";

    let data = {};

    // null일 경우 조회조건에 따른 출력
    if(checkedData.length === 0){
        const searchColumn = $("#search-column-codeRuleGrid").val();
        const searchValue = $("#search-input-text-codeRuleGrid").val();

        // 검색어가 입력되어있지 않으면 부하 발생할 수 있다는 부분 추가 필요
        if(searchValue === "") message = "검색어가 입력되어있지 않습니다. 전체 내용이 출력되므로 시간이 다소 소요될 수 있습니다.";

        message += `${formatText}을 출력하시겠습니까?`;

        data.column = searchColumn;
        data.searchValue = searchValue;

    }else {
        message = `체크된 ${formatText}을 출력하시겠습니까?`;
        type = "unit";

        data.rules = checkedData;
    }

    data.type = type;

    openConfirm(message, () => {
        $.ajax({
            url : "/api/printTest",
            type : "POST",
            data : JSON.stringify(data),
            xhrFields: {
                responseType: 'blob'
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, `${formatText}.xlsx`);
                closeDialog("div");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        })
    });
}
