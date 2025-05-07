import {setupAjaxCsrf} from "../common/csrf.js";

setupAjaxCsrf();

const params = new URLSearchParams(window.location.search);

$(document).ready(function () {
    const type = params.get("type");

    $.ajax({

        url: `/api/selectCodeRuleManage/template`,
        type: "GET",
        success: function (response) {
            if (response.result) {
                setCodeRuleTemplates(response.templates || [], type);

                if(type === "modified"){
                    const id = params.get("id");
                    $.ajax({
                        url: `/api/selectCodeRule/detail/${id}`,
                        type: "GET",
                        success: (response) => {
                            if(response.result){
                                const data = response.codeRule;
                                $("#id").val(data.id);
                                $("#dummyData").val(String(data.codeRuleId));
                                $("#functionGroup").val(data.functionGroup);
                                $("#methodKeyword").val(data.methodKeyword);
                                $("#methodPurpose").val(data.methodPurpose);
                                $("#description").val(data.description);
                                $("#methodName").val(data.methodName);
                                $("#isDicAbbrUsed").val(data.isDicAbbrUsed);
                                $("#input").val(data.input);
                                $("#output").val(data.output);
                                $("#exception").val(data.exception);
                                $("#useSwagger").prop("checked", data.useSwagger);
                                $("#methodForm").val(data.methodForm);

                                const select = $("#templateSelect");
                                select.val(String(data.templateId)).trigger("change");
                            }
                        }
                    });
                }
            }
        }
    });

    $("#templateSelect").on("change", function(){
        const select = $(this).find("option:selected").val();

        const $codeRuleSelect = $("#codeRule");
        $codeRuleSelect.empty(); // 기존 옵션 초기화
        $codeRuleSelect.append(`<option value="">코드규칙을 선택하세요</option>`);

        if(type === "add") $codeRuleSelect.trigger("change");

        if(select === ""){
            return;
        }

        $.ajax({
            url: `/api/selectCodeRule/codeRule/${select}`,
            type: "GET",
            success: (response) => {
                if(response.result){
                    // codeRules 전역 저장 (또는 클로저로 유지)
                    window._codeRuleCache = response.codeRules;

                    response.codeRules.forEach(rule => {
                        $codeRuleSelect.append(
                            `<option value="${rule.id}" data-method-form="${encodeURIComponent(rule.methodForm)}">${rule.codeRuleName}</option>`
                        );
                    });

                    const data = $("#dummyData");
                    if(data.val() !== ""){
                        $("#codeRule").val(data.val());
                    }

                    data.val("");
                }
            }
        });
    });

    $(".popup-form").on("input change", "input, select, textarea", function () {
        $("#save-button").css("display", "none");
    });

    $("#set-button").on("click", function () {
        setData();
    });

    $("#save-button").on("click", function(){
        saveData();
    });

    // 코드 규칙 선택 시 input/output 값 반영
    $("#codeRule").on("change", function() {
        if(type === "add"){
            const selectedId = $(this).val();
            const selectedRule = (window._codeRuleCache || []).find(rule => rule.id == selectedId);

            if (selectedRule) {
                $("#input").val(selectedRule.input || "");
                $("#output").val(selectedRule.output || "");
                $("#exception").val(selectedRule.exception || "");
            } else {
                $("#input").val("");
                $("#output").val("");
                $("#exception").val("");
            }
        }
    });
});

function setCodeRuleTemplates(templateList, type) {
    if(type === "modified"){
        const id = params.get("id");
        $("#id").val(id);
    }

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

function setData(){
    const data = {
        templateId: $("#templateSelect").val(),
        codeRuleId: $("#codeRule").val(),
        functionGroup: $("#functionGroup").val().trim(),
        methodKeyword: $("#methodKeyword").val().trim(),
        methodPurpose: $("#methodPurpose").val().trim(),
        description: $("#description").val().trim(),
        input: $("#input").val().trim(),
        output: $("#output").val().trim(),
        exception: $("#exception").val().trim(),
        useSwagger: $("#useSwagger").is(":checked")
    };

    // 필수값 리스트 정의
    const requiredFields = [
        { key: "templateId", label: "템플릿을 선택해주세요." },
        { key: "codeRuleId", label: "코드규칙을 선택해주세요." },
        { key: "functionGroup", label: "그룹명을 입력해주세요." },
        { key: "methodKeyword", label: "명칭 키워드를 입력해주세요." }
    ];

    // 필수값 체크
    let hasEmpty = false;
    requiredFields.forEach(field => {
        if (!data[field.key]) {
            alert(field.label);
            hasEmpty = true;
            return false; // forEach는 break 안 되지만 flag는 줄 수 있음
        }
    });

    if (hasEmpty) return;

    $.ajax({
        url: "/api/selectCodeRule/detail",
        type: "POST",
        data: JSON.stringify(data),
        success: (response) => {
            if(response.result){
                $("#methodPurpose").val(response.codeRule.methodPurpose);
                $("#methodName").val(response.codeRule.methodName);
                $("#methodForm").val(response.codeRule.methodForm);
                $("#isDicAbbrUsed").val(response.codeRule.isDicAbbrUsed);

                // 저장 버튼 보여주기
                $("#save-button").css("display", "inline-block");
            }
        }
    });
}

function saveData(){
    const data = {
        id: $("#id").val(),
        isDicAbbrUsed: $("#isDicAbbrUsed").val(),
        templateId: $("#templateSelect").val(),
        codeRuleId: $("#codeRule").val(),
        functionGroup: $("#functionGroup").val().trim(),
        methodKeyword: $("#methodKeyword").val().trim(),
        methodPurpose: $("#methodPurpose").val().trim(),
        description: $("#description").val().trim(),
        methodName: $("#methodName").val(),
        input: $("#input").val().trim(),
        output: $("#output").val().trim(),
        exception: $("#exception").val().trim(),
        useSwagger: $("#useSwagger").is(":checked")
    }

    // 필수값 리스트 정의
    const requiredFields = [
        { key: "templateId", label: "템플릿을 선택해주세요." },
        { key: "codeRuleId", label: "코드규칙을 선택해주세요." },
        { key: "functionGroup", label: "그룹명을 입력해주세요." },
        { key: "methodKeyword", label: "명칭 키워드를 입력해주세요." },
        { key: "methodName", label: "메소드 명" }
    ];

    // 필수값 체크
    let hasEmpty = false;
    requiredFields.forEach(field => {
        if (!data[field.key]) {
            alert(field.label);
            hasEmpty = true;
            return false; // forEach는 break 안 되지만 flag는 줄 수 있음
        }
    });

    if (hasEmpty) return;

    const type = $("#type").val();

    $.ajax({
        url: "/api/saveCodeRule",
        type: type === "add" ? "POST" : "PUT",
        data: JSON.stringify(data),
        success: (response) => {
            if(response.result){
                // 부모로 데이터 전달
                if (window.opener && typeof window.opener.popupFunction?.codeRuleSuccess === 'function') {
                    window.opener.popupFunction.codeRuleSuccess();
                } else {
                    alert("부모 창과의 통신이 불가능합니다.");
                }

                window.close();
            }
        }
    });
}
