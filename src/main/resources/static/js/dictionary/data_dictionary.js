import {setupAjaxCsrf} from "../csrf.js";

$(document).ready(function () {
    setupAjaxCsrf();

    /**
     * 템플릿 다운로드 버튼 클릭 이벤트
     * */
    $("#download-template").on("click", () => {
       debugger
    });

    /**
     * 엑셀업로드 버튼 클릭 이벤트
     * */
    $("#excel-upload").on("click", () => {
        debugger
    });

});
