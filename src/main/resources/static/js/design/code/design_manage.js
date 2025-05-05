import {setupAjaxCsrf} from "../../common/csrf.js";

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
            { id: "functionSpec", text: "기능 정의서" },
            { id: "interfaceSpec", text: "인터페이스 설계서" },
            { id: "technicalSpec", text: "기술 설계서" }
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


        openDialog("div", {title: "설계서 출력", content: dialogContent});
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

        data.checkedData = checkedData;
    }

    openConfirm(message, () => {
        // 실제 출력 로직 호출

        console.log(data);
    });
}
