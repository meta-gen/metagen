import {registerRowDecorator, setLabel, setMono} from "../../common/grid_cells.js";

/**
 * 코드규칙 그리드(codeRuleGrid) 행 꾸미기 : 코드 규칙 / 설계서 / 테스트 시나리오 화면 공통
 * - 변환된 메소드 명 · 입출력값은 코드 글꼴, 템플릿 명은 라벨, 그룹명은 보라 라벨
 */
export function registerCodeRuleDecorator(tableId = "codeRuleGrid") {
    registerRowDecorator(tableId, (row, data, cellOf) => {
        if (data.functionGroup) setLabel(cellOf("functionGroup"), data.functionGroup, "done");
        if (data.templateName) setLabel(cellOf("templateName"), data.templateName);
        ["methodName", "input", "output", "exception"].forEach(field => {
            if (data[field]) setMono(cellOf(field), data[field]);
        });
    });
}

/**
 * 출력 형식 선택 목록 (설계서 / 테스트 시나리오 출력 다이얼로그)
 * @param options [{id, text, desc}]
 * @param onSelect (option) => {}
 */
export function createPrintOptions(message, options, onSelect) {
    const icon = '<svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M2 1.75C2 .784 2.784 0 3.75 0h6.586c.464 0 .909.184 1.237.513l2.914 2.914c.329.328.513.773.513 1.237v9.586A1.75 1.75 0 0 1 13.25 16h-9.5A1.75 1.75 0 0 1 2 14.25Zm1.75-.25a.25.25 0 0 0-.25.25v12.5c0 .138.112.25.25.25h9.5a.25.25 0 0 0 .25-.25V6h-2.75A1.75 1.75 0 0 1 9 4.25V1.5Zm6.75.062V4.25c0 .138.112.25.25.25h2.688l-.011-.013-2.914-2.914-.013-.011Z"/></svg>';

    const $content = $("<div></div>");
    $content.append($('<p class="gh-dialog-text"></p>').html(message));

    const $list = $('<div class="gh-action-list gh-action-list-boxed"></div>');
    options.forEach(option => {
        const $btn = $('<button type="button"></button>').attr("id", option.id)
            .html(`${icon}<div><strong></strong><span></span></div>`);
        $btn.find("strong").text(option.text);
        $btn.find("span").text(option.desc ?? "엑셀 파일로 내려받습니다.");
        $btn.on("click", () => onSelect(option));
        $list.append($btn);
    });

    return $content.append($list);
}
