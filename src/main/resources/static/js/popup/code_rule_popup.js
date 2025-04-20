import {setupAjaxCsrf} from "../common/csrf.js";

setupAjaxCsrf();

$(document).ready(function () {
    // 저장 버튼 클릭 시
    $("#save-button").on("click", function (e) {
        e.preventDefault(); // 폼 기본 제출 막기
        submitForm();
    });

    // 페이지 로드 시 파라미터로 데이터 조회
    const params = new URLSearchParams(window.location.search);
    const projectId = params.get("projectId");
    const type = params.get("type");

    if (type === "add" && projectId) {
        $.ajax({
            url: `/api/selectCodeRuleManage/template/${projectId}`,
            type: "GET",
            success: function (response) {
                if (response.result) {
                    setCodeRuleTemplates(response.templates || [], type);
                    $("#projectId").val(projectId);
                }
            }
        });
    }
});

function setCodeRuleTemplates(templateList, type) {
    const $select = $("#templateSelect");
    $select.empty().append(`<option value="">템플릿을 선택하세요</option>`);

    templateList.forEach(template => {
        const $option = $("<option></option>")
            .val(template.id)
            .text(template.templateName);
        $select.append($option);
    });

    $("#type").val(type);
}

function submitForm() {
    const data = {
        projectId: $("#projectId").val(),
        templateId: $("#templateSelect").val(),
        codeRuleName: $("#codeRuleName").val(),
        codeRuleDescription: $("#codeRuleDescription").val(),
        prefix: $("#prefix").val(),
        suffix: $("#suffix").val(),
        methodForm: $("#methodForm").val()
    };

    // 필수 항목 유효성 검사
    for (const [key, value] of Object.entries(data)) {

        if(key === "prefix" || key === "suffix"){
            continue;
        }

        if (!value || value.trim() === "") {
            let label = {
                templateId: "템플릿",
                codeRuleName: "코드규칙 명",
                codeRuleDescription: "코드규칙 내용",
                methodForm: "메소드 형식"
            }[key];

            alert(`"${label}" 항목을 입력해주세요.`);
            $(`#${key}`).focus();
            return;
        }
    }

    let type = "PUT";
    if($("#type").val() === "add"){
        type = "POST";
    }

    $.ajax({
        url: "/api/saveCodeRuleManage",
        type: type,
        data: JSON.stringify(data),
        success: (response) => {
            if(response.result){
                // 부모로 데이터 전달
                if (window.opener && typeof window.opener.popupFunction?.codeRuleManageSuccess === 'function') {
                    window.opener.popupFunction.codeRuleManageSuccess();
                } else {
                    alert("부모 창과의 통신이 불가능합니다.");
                }

                window.close();
            }
        }
    });
}
