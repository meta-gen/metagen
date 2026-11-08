import {closePopupDialog, openPopupDialog} from "../common/popup_dialog.js";

/**
 * 코드규칙 관리 등록/수정 다이얼로그 (설계기반 관리 > 코드규칙 관리)
 * @param type      "add" | "modified"
 * @param id        수정할 코드규칙 관리 ID
 * @param onSuccess 저장 성공 시 호출 (다이얼로그는 닫힌 뒤 호출)
 */
export function openCodeRuleManagePopup({type, id, onSuccess}) {
    openPopupDialog({
        url: "/popup/codeRulePopup",
        title: type === "modified" ? "코드규칙 관리 수정" : "코드규칙 관리 등록",
        onLoad: ($root) => initCodeRuleManage($root, type, id, onSuccess)
    });
}

function initCodeRuleManage($root, type, id, onSuccess) {
    const q = (selector) => $root.find(selector);

    q("#type").val(type);

    q("#save-button").on("click", (e) => {
        e.preventDefault();
        submitForm($root, onSuccess);
    });

    $.ajax({
        url: `/api/selectCodeRuleManage/template`,
        type: "GET",
        success: (response) => {
            if (!response.result) return;

            setCodeRuleTemplates($root, response.templates || []);

            if (type !== "modified") return;

            $.ajax({
                url: `/api/selectCodeRuleManage/detail/${encodeURIComponent(id)}`,
                type: "GET",
                success: (response) => {
                    if (!response.result) return;

                    const codeRule = response.codeRuleDto;
                    q("#id").val(codeRule.id);
                    q("#codeRuleName").val(codeRule.codeRuleName);
                    q("#codeRuleDescription").val(codeRule.codeRuleDescription);
                    q("#prefix").val(codeRule.prefix);
                    q("#suffix").val(codeRule.suffix);
                    q("#methodForm").val(codeRule.methodForm);
                    q("#input").val(codeRule.input);
                    q("#output").val(codeRule.output);
                    q("#exception").val(codeRule.exception);
                    q("#templateSelect").val(String(codeRule.templateId)).prop("disabled", true);
                }
            });
        }
    });

    // 메소드 형식 입력 시 Tab 키로 들여쓰기
    q("#methodForm").on("keydown", function (e) {
        if (e.key !== "Tab") return;
        e.preventDefault();

        const start = this.selectionStart;
        const end = this.selectionEnd;
        this.value = this.value.substring(0, start) + "\t" + this.value.substring(end);
        this.selectionStart = this.selectionEnd = start + 1;
    });
}

function setCodeRuleTemplates($root, templateList) {
    const $select = $root.find("#templateSelect");
    $select.empty().append(`<option value="">템플릿을 선택하세요</option>`);

    templateList.forEach(template => {
        $select.append($("<option></option>").val(template.id).text(template.templateName));
    });
}

function submitForm($root, onSuccess) {
    const q = (selector) => $root.find(selector);

    const data = {
        templateId: q("#templateSelect").val(),
        codeRuleName: q("#codeRuleName").val(),
        codeRuleDescription: q("#codeRuleDescription").val(),
        prefix: q("#prefix").val(),
        suffix: q("#suffix").val(),
        input: q("#input").val(),
        output: q("#output").val(),
        exception: q("#exception").val(),
        methodForm: q("#methodForm").val()
    };

    const requiredLabels = {
        templateId: "템플릿",
        codeRuleName: "코드규칙 명",
        codeRuleDescription: "코드규칙 내용",
        exception: "기본 예외처리값",
        methodForm: "메소드 형식"
    };

    // 필수 항목 유효성 검사 (접두사/접미사/입력값/출력값은 선택)
    for (const [key, label] of Object.entries(requiredLabels)) {
        const value = data[key];
        if (!value || value.trim() === "") {
            window.openAlert(`"${label}" 항목을 입력해주세요.`);
            q(`#${key === "templateId" ? "templateSelect" : key}`).trigger("focus");
            return;
        }
    }

    const isAdd = q("#type").val() === "add";
    if (!isAdd) data.id = q("#id").val();

    $.ajax({
        url: "/api/saveCodeRuleManage",
        type: isAdd ? "POST" : "PUT",
        data: JSON.stringify(data),
        success: (response) => {
            if (response.result) {
                closePopupDialog();
                if (typeof onSuccess === "function") onSuccess();
            }
        }
    });
}
