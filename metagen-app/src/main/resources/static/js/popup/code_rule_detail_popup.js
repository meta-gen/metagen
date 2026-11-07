import {closePopupDialog, openPopupDialog} from "../common/popup_dialog.js";

/**
 * 코드규칙 등록/수정 다이얼로그 (설계기반 관리 > 코드 규칙)
 * 입력값으로 [변환]을 눌러 메소드명/양식을 만든 뒤 저장한다.
 * @param type      "add" | "modified"
 * @param id        수정할 코드규칙 ID
 * @param onSuccess 저장 성공 시 호출 (다이얼로그는 닫힌 뒤 호출)
 */
export function openCodeRuleDetailPopup({type, id, onSuccess}) {
    openPopupDialog({
        url: "/popup/codeRuleDetailPopup",
        title: type === "modified" ? "코드규칙 수정" : "코드규칙 등록",
        onLoad: ($root) => initCodeRuleDetail($root, type, id, onSuccess)
    });
}

function initCodeRuleDetail($root, type, id, onSuccess) {
    const q = (selector) => $root.find(selector);
    let codeRuleCache = []; // 선택한 템플릿의 코드규칙 목록

    q("#type").val(type);
    if (type === "modified") q("#id").val(id);

    $.ajax({
        url: `/api/selectCodeRuleManage/template`,
        type: "GET",
        success: (response) => {
            if (!response.result) return;

            setCodeRuleTemplates($root, response.templates || []);

            if (type !== "modified") return;

            $.ajax({
                url: `/api/selectCodeRule/detail/${encodeURIComponent(id)}`,
                type: "GET",
                success: (response) => {
                    if (!response.result) return;

                    const data = response.codeRule;
                    q("#id").val(data.id);
                    q("#dummyData").val(String(data.codeRuleId)); // 템플릿 선택 후 코드규칙 선택용
                    q("#functionGroup").val(data.functionGroup);
                    q("#methodKeyword").val(data.methodKeyword);
                    q("#methodPurpose").val(data.methodPurpose);
                    q("#description").val(data.description);
                    q("#methodName").val(data.methodName);
                    q("#isDicAbbrUsed").val(data.isDicAbbrUsed);
                    q("#input").val(data.input);
                    q("#output").val(data.output);
                    q("#exception").val(data.exception);
                    q("#useSwagger").prop("checked", data.useSwagger);
                    q("#methodForm").val(data.methodForm);

                    q("#templateSelect").val(String(data.templateId)).trigger("change");
                }
            });
        }
    });

    // 템플릿 선택 → 코드규칙 목록 조회
    q("#templateSelect").on("change", function () {
        const templateId = $(this).val();

        const $codeRuleSelect = q("#codeRule");
        $codeRuleSelect.empty().append(`<option value="">코드규칙을 선택하세요</option>`);

        if (type === "add") $codeRuleSelect.trigger("change");
        if (templateId === "") return;

        $.ajax({
            url: `/api/selectCodeRule/codeRule/${encodeURIComponent(templateId)}`,
            type: "GET",
            success: (response) => {
                if (!response.result) return;

                codeRuleCache = response.codeRules || [];
                codeRuleCache.forEach(rule => {
                    $codeRuleSelect.append($("<option></option>").val(rule.id).text(rule.codeRuleName));
                });

                const $dummy = q("#dummyData");
                if ($dummy.val() !== "") $codeRuleSelect.val($dummy.val());
                $dummy.val("");
            }
        });
    });

    // 코드규칙 선택 시 (등록) 기본 입력/출력/예외처리값 반영
    q("#codeRule").on("change", function () {
        if (type !== "add") return;

        const selectedRule = codeRuleCache.find(rule => String(rule.id) === $(this).val());
        q("#input").val(selectedRule?.input || "");
        q("#output").val(selectedRule?.output || "");
        q("#exception").val(selectedRule?.exception || "");
    });

    // 입력값이 바뀌면 다시 변환해야 저장할 수 있다.
    q(".popup-form").on("input change", "input, select, textarea", () => {
        q("#save-button").css("display", "none");
    });

    q("#set-button").on("click", () => convert($root));
    q("#save-button").on("click", () => save($root, onSuccess));
}

function setCodeRuleTemplates($root, templateList) {
    const $select = $root.find("#templateSelect");
    $select.empty().append(`<option value="">템플릿을 선택하세요</option>`);

    templateList.forEach(template => {
        $select.append($("<option></option>").val(template.id).text(template.templateName));
    });
}

/**
 * 첫 번째로 비어있는 필수값의 안내 문구를 반환한다. (모두 입력되었으면 null)
 */
function findMissing(data, requiredFields) {
    return requiredFields.find(field => !data[field.key])?.label ?? null;
}

function readForm($root) {
    const q = (selector) => $root.find(selector);
    return {
        templateId: q("#templateSelect").val(),
        codeRuleId: q("#codeRule").val(),
        functionGroup: q("#functionGroup").val().trim(),
        methodKeyword: q("#methodKeyword").val().trim(),
        methodPurpose: q("#methodPurpose").val().trim(),
        description: q("#description").val().trim(),
        input: q("#input").val().trim(),
        output: q("#output").val().trim(),
        exception: q("#exception").val().trim(),
        useSwagger: q("#useSwagger").is(":checked")
    };
}

const REQUIRED_FIELDS = [
    {key: "templateId", label: "템플릿을 선택해주세요."},
    {key: "codeRuleId", label: "코드규칙을 선택해주세요."},
    {key: "functionGroup", label: "그룹명을 입력해주세요."},
    {key: "methodKeyword", label: "명칭 키워드를 입력해주세요."}
];

/**
 * [변환] : 입력값으로 메소드명/양식을 만든다.
 */
function convert($root) {
    const q = (selector) => $root.find(selector);
    const data = readForm($root);

    const missing = findMissing(data, REQUIRED_FIELDS);
    if (missing) {
        window.openAlert(missing);
        return;
    }

    $.ajax({
        url: "/api/selectCodeRule/detail",
        type: "POST",
        data: JSON.stringify(data),
        success: (response) => {
            if (!response.result) return;

            q("#methodPurpose").val(response.codeRule.methodPurpose);
            q("#methodName").val(response.codeRule.methodName);
            q("#methodForm").val(response.codeRule.methodForm);
            q("#isDicAbbrUsed").val(response.codeRule.isDicAbbrUsed);

            q("#save-button").css("display", "inline-block");
        }
    });
}

function save($root, onSuccess) {
    const q = (selector) => $root.find(selector);

    const data = {
        ...readForm($root),
        id: q("#id").val(),
        isDicAbbrUsed: q("#isDicAbbrUsed").val(),
        methodName: q("#methodName").val()
    };

    const missing = findMissing(data, [...REQUIRED_FIELDS, {key: "methodName", label: "변환 버튼을 눌러 메소드명을 만들어주세요."}]);
    if (missing) {
        window.openAlert(missing);
        return;
    }

    $.ajax({
        url: "/api/saveCodeRule",
        type: q("#type").val() === "add" ? "POST" : "PUT",
        data: JSON.stringify(data),
        success: (response) => {
            if (response.result) {
                closePopupDialog();
                if (typeof onSuccess === "function") onSuccess();
            }
        }
    });
}
