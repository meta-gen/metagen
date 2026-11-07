import {openDictionaryDetail} from "./dictionary_detail.js";
import {registerRowDecorator, setApproval, setMono} from "../../common/grid_cells.js";

/**
 * 표준 용어 행 꾸미기 : 영문약어는 코드 글꼴, 승인여부는 상태 라벨
 */
registerRowDecorator("standardTerms", (row, data, cellOf) => {
    setMono(cellOf("commonStandardTermAbbreviation"), data.commonStandardTermAbbreviation);
    setApproval(cellOf("isApprovalYn"), data.isApprovalYn);
});

/**
 * 표준 용어 승인 버튼
 *
 */
$("#grd-active-standardTerms").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardTerms");
    if(!checkedData) return;

    window.openConfirm("체크된 표준 용어를 승인하시겠습니까?", () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApprovalYn === 'N'){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert("승인이 필요한 표준 용어가 선택되지 않았습니다.");
            return;
        }

        $.ajax({

            url: "/api/approvalStandardTerms/true",
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 승인처리 되었습니다.", () => {
                        window.searchGrid("standardTerms");
                    });
                }
            }
        });
    });
});

/**
 * 표준 용어 승인취소 버튼
 *
 */
$("#grd-unactive-standardTerms").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardTerms");
    if(!checkedData) return;

    window.openConfirm("체크된 표준 용어를 승인하시겠습니까?", () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApprovalYn === 'Y'){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert("승인취소가 필요한 표준 용어가 선택되지 않았습니다.");
            return;
        }

        $.ajax({
            url: "/api/approvalStandardTerms/false",
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 승인취소처리 되었습니다.", () => {
                        window.searchGrid("standardTerms");
                    });
                }
            }
        });
    });
});

/**
 * 표준용어 추가 버튼
 */
$("#grd-add-standardTerms").on("click", () => {
    addStandardTerms();
});

/**
 * 표준용어 삭제 버튼
 */
$("#grd-delete-standardTerms").on("click", () => {
    const tableId = "standardTerms";
    const checkedData = getCheckedDataIsNonNull(tableId);
    if(!checkedData) return;

    let isApproval = true;
    checkedData.forEach((e) => {
        if(e.isApprovalYn === 'Y'){
            isApproval = false;
        }
    });

    if(!isApproval){
        window.openAlert("체크된 데이터에 승인된 정보도 포함되어있습니다. 승인이 완료된 경우, 삭제가 불가능합니다.");
        return;
    }

    window.openConfirm("체크된 데이터를 삭제하시겠습니까?", () => {
        $.ajax({
            url : `/api/deleteDataDictionary/${tableId}`,
            type : 'DELETE',
            data : JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 삭제처리 되었습니다.", () => {
                        window.searchGrid(tableId);
                    });
                }
            }
        });
    });
});

/**
 * 표준용어를 등록한다.
 */
function addStandardTerms(){
    const tableId = "standardTerms";

    const columnList = window.tableInstances["standardTerms"]
        .settings()
        .init()
        .columns
        .filter(col => col.data !== "id" && col.data !== "isApprovalYn")
        .map(col => {
            return {
                column: col.data,
                columnName: col.title || col.columnName || col.data
            };
        });

    const dialog = $("#mainConfirm")[0];
    const dialogTitle = $("#mainDialogTitle");
    const dialogContent = $("#mainDialogContent");

    dialogTitle.text("표준용어 추가");
    dialogContent.empty(); // 초기화

    const form = $("<form></form>").attr("id", `edit-${tableId}`);

    const inputType = $("<input>").attr("id", "type")
        .attr("name", "type")
        .attr("type", "hidden")
        .attr("value", "C");

    form.append(inputType);

    columnList.forEach(col => {
        const key = col.column;
        const labelText = col.columnName || key;

        if (key === "commonStandardTermName") {
            // 표준용어명 : 입력 즉시 표준 단어를 조회해 영문약어/도메인을 채운다 (별도 검색 창 없이)
            form.append(createTermWordInput(labelText));
        }else{

            const label = $("<label></label>")
                .text(labelText)
                .attr("for", key)
                .css("display", "block")
                .css("margin-top", "10px");

            let input;

            if (["commonStandardDomainDescription", "allowedValues"].includes(key)) {
                input = $("<textarea></textarea>")
                    .attr("id", key)
                    .attr("name", key)
                    .css("width", "100%")
                    .css("height", "60px");
            }else if(key === "revisionNumber"){
                input = $("<input>")
                    .attr("type", "number")
                    .attr("id", key)
                    .attr("name", key)
                    .css("width", "100%")
                    .on("keydown", function(event){
                        if (event.key === "e" || event.key === "E" || event.key === "+" || event.key === "-") {
                            event.preventDefault();
                        }
                    });
            } else {
                input = $("<input>")
                    .attr("type", "text")
                    .attr("id", key)
                    .attr("name", key)
                    .css("width", "100%");
            }

            // id, projectId는 제외하거나 readonly
            if (key === "id" || key === "projectId" || key === "commonStandardDomainName" || key === "allowedValues" || key === "storageFormat" || key === "displayFormat" || key === "synonyms" || key === "commonStandardTermName" || key === "commonStandardTermAbbreviation") {
                input.prop("readonly", true).css({
                        flex: "1",
                        backgroundColor: "#f0f0f0",
                        height: "30px",
                        paddingLeft: "10px",
                        border: "1px solid #ccc",
                        borderRadius: "4px"
                    });
            }

            form.append(label, input);
        }
    });

    const saveBtn = $("<button></button>")
        .attr("type", "button")
        .text("추가")
        .attr("id", "approvalSubmit")
        .attr("name", "approvalSubmit")
        .addClass("btn btn-primary")
        .css("margin-top", "20px")
        .on("click", approvalSubmitClick);

    form.append(saveBtn);
    dialogContent.append(form);

    resetTermState();
    dialog.showModal();
    $("#termWordInput").trigger("focus");
}

/**
 * 표준용어 등록 시 단어 조회 상태 (등록 팝업 하나만 열리므로 모듈 변수로 관리)
 */
const termState = {
    termName: "",    // 마지막으로 조회한 용어명 (공백 구분)
    valid: false,    // 모든 단어가 등록되어 있고 마지막 단어가 도메인 분류 단어인지
    pending: false,  // 조회 중 여부
    requestSeq: 0,   // 늦게 도착한 이전 응답 무시용
    domains: [],     // 마지막 단어의 도메인 분류에 속한 도메인 목록
    duplicate: { key: "", checking: false, duplicated: false, seq: 0 } // 영문약어명 + 도메인 중복 확인 결과
};

const TERM_WORD_STATUS = {
    APPROVED: { variant: "success" },
    NOT_APPROVED: { variant: "attention", text: "미승인" },
    NOT_FOUND: { variant: "danger", text: "미등록" },
    NOT_DOMAIN: { variant: "danger", text: "도메인 분류 없음" }
};

// 표준용어명이 바뀌면 다시 채워야 하는 항목 (조회/도메인 선택으로 자동 입력)
const TERM_AUTO_FIELDS = ["commonStandardTermAbbreviation", "commonStandardDomainName", "allowedValues", "storageFormat", "displayFormat", "synonyms"];

function resetTermState() {
    Object.assign(termState, { termName: "", valid: false, pending: false, domains: [] });
    resetDuplicate();
}

function resetDuplicate() {
    Object.assign(termState.duplicate, { key: "", checking: false, duplicated: false });
    termState.duplicate.seq++; // 진행 중인 확인 응답 무시
    $("#termDuplicate").empty();
}

/**
 * 표준용어명 입력 + 단어별 결과 + 도메인 선택 영역
 */
function createTermWordInput(labelText) {
    const $group = $("<div></div>").addClass("term-word-group");
    let timer = null;

    const $input = $("<input>")
        .attr({ type: "text", id: "termWordInput", autocomplete: "off", placeholder: "예) 사용자_이름 또는 사용자 이름" })
        .addClass("form-control")
        .on("input", function () {
            const words = splitTermWords($(this).val());
            clearTimeout(timer);
            clearTermResult();

            if (words.length === 0) return;

            termState.pending = true;
            timer = setTimeout(() => lookupTermWords(words), 300);
        });

    // 서버로 보내는 표준용어명 (단어를 공백으로 이은 값)
    const $hidden = $("<input>").attr({ type: "hidden", id: "commonStandardTermName", name: "commonStandardTermName" });

    const $domainSelect = $("<select></select>")
        .attr({ id: "termDomainSelect" })
        .addClass("form-control")
        .prop("disabled", true)
        .append($("<option></option>").val("").text("표준용어명을 먼저 입력하세요"))
        .on("change", function () {
            const domain = termState.domains.find(d => d.commonStandardDomainName === $(this).val());
            fillDomainFields(domain);
        });

    $group.append(
        $("<label></label>").attr("for", "termWordInput").text(labelText),
        $input,
        $("<p></p>").addClass("form-help")
            .text("표준 단어를 _ 또는 공백으로 구분해 입력하면 바로 조회합니다. 마지막 단어는 도메인 분류가 있는 단어(예: 명, 일자)여야 합니다."),
        $("<div></div>").attr({ id: "termWords", "aria-live": "polite" }).addClass("table-name-words"),
        $hidden,
        $("<label></label>").attr("for", "termDomainSelect").text("도메인 선택"),
        $domainSelect,
        $("<div></div>").attr({ id: "termDuplicate", "aria-live": "polite" }).addClass("table-name-words")
    );

    return $group;
}

function splitTermWords(value) {
    return String(value ?? "").trim().split(/[_\s]+/).filter(Boolean);
}

/**
 * 입력이 바뀌면 이전 조회 결과와 자동 입력 값을 비운다.
 */
function clearTermResult() {
    Object.assign(termState, { termName: "", valid: false, pending: false, domains: [] });
    resetDuplicate();
    $("#termWords").empty();
    $("#commonStandardTermName").val("");
    TERM_AUTO_FIELDS.forEach(field => $(`#${field}`).val(""));
    $("#termDomainSelect").prop("disabled", true).empty()
        .append($("<option></option>").val("").text("표준용어명을 먼저 입력하세요"));
}

function lookupTermWords(words) {
    const seq = ++termState.requestSeq;
    const termName = words.join(" ");

    $.ajax({
        url: `/api/getStandardTerms/popup/${encodeURIComponent(termName)}`,
        type: "GET",
        success: (response) => {
            if (seq !== termState.requestSeq) return; // 이전 입력에 대한 응답은 무시

            const standardWords = response.list || [];
            const domains = response.standardDomains || [];

            const results = words.map((word, index) => {
                const matched = standardWords.find(w => w.commonStandardWordName === word);
                const isLast = index === words.length - 1;
                const isDomainWord = !!matched && !!matched.commonStandardDomainCategory && matched.commonStandardDomainCategory !== "-";

                let status = "APPROVED";
                if (!matched) status = "NOT_FOUND";
                else if (isLast && !isDomainWord) status = "NOT_DOMAIN";
                else if (matched.isApprovalYn === "N") status = "NOT_APPROVED";

                return { word, matched, isLast, isDomainWord, status };
            });

            // 미승인 단어는 등록 가능(승인 대기)하므로 경고만 하고, 미등록/도메인 분류 없음은 등록할 수 없다.
            const valid = results.every(r => r.status === "APPROVED" || r.status === "NOT_APPROVED") && domains.length > 0;

            Object.assign(termState, { termName, valid, pending: false, domains });
            $("#commonStandardTermName").val(termName);

            renderTermWords(results, valid, domains);

            if (!valid) return;

            $("#commonStandardTermAbbreviation").val(results.map(r => r.matched.commonStandardWordAbbreviation).join("_"));
            $("#synonyms").val(buildSynonyms(results));
            populateTermDomains(domains);
        },
        error: () => {
            if (seq === termState.requestSeq) termState.pending = false;
        }
    });
}

/**
 * 단어별 결과 라벨 : "사용자 USER" / "이름 NM · 도메인" / "나이 미등록"
 */
function renderTermWords(results, valid, domains) {
    const $words = $("#termWords").empty();

    results.forEach(r => {
        const status = TERM_WORD_STATUS[r.status];
        const $label = $("<span></span>").addClass(`gh-label gh-label-${status.variant}`)
            .append($("<span></span>").text(r.word));

        if (r.matched) {
            $label.append($("<span></span>").addClass("table-name-abbr").text(r.matched.commonStandardWordAbbreviation ?? ""));
            if (r.matched.commonStandardWordEnglishName) $label.attr("title", r.matched.commonStandardWordEnglishName);
        }
        if (status.text) $label.append($("<span></span>").text(status.text));
        if (r.isLast && r.isDomainWord) $label.append($("<span></span>").text(`· ${r.matched.commonStandardDomainCategory}`));

        $words.append($label);
    });

    const messages = [];
    if (results.some(r => r.status === "NOT_FOUND")) {
        messages.push(["table-name-error", "미등록 단어는 표준 단어 탭에서 먼저 등록해야 합니다."]);
    }
    if (results.some(r => r.status === "NOT_DOMAIN")) {
        messages.push(["table-name-error", "마지막 단어는 도메인 분류가 있는 표준 단어여야 합니다."]);
    }
    if (results.every(r => r.status !== "NOT_FOUND" && r.status !== "NOT_DOMAIN") && domains.length === 0) {
        messages.push(["table-name-error", "마지막 단어의 도메인 분류에 등록된 표준 도메인이 없습니다."]);
    }
    if (valid && results.some(r => r.status === "NOT_APPROVED")) {
        messages.push(["table-name-warning", "미승인 단어가 포함되어 있습니다. 등록은 가능하지만 단어 승인이 필요합니다."]);
    }

    messages.forEach(([cls, text]) => $words.append($("<p></p>").addClass(cls).text(text)));
}

/**
 * 마지막(도메인) 단어의 이음동의어로 용어 이음동의어를 만든다. (예: 사용자 명 → 사용자 이름, 사용자 성명)
 */
function buildSynonyms(results) {
    const last = results[results.length - 1].matched;
    const synonyms = String(last.synonyms ?? "").trim();
    if (!synonyms || synonyms === "-") return "";

    const prefix = results.slice(0, -1).map(r => r.word).join(" ");
    return synonyms.split(",").map(s => s.trim()).filter(Boolean)
        .map(s => (prefix ? `${prefix} ${s}` : s)).join(",");
}

function populateTermDomains(domains) {
    const $select = $("#termDomainSelect").empty().prop("disabled", false)
        .append($("<option></option>").val("").text("도메인을 선택하세요"));

    domains.forEach(domain => {
        $select.append($("<option></option>")
            .val(domain.commonStandardDomainName)
            .text(`${domain.commonStandardDomainName} (${domain.storageFormat || "-"})`));
    });

    // 선택지가 하나뿐이면 바로 선택
    if (domains.length === 1) {
        $select.val(domains[0].commonStandardDomainName).trigger("change");
    }
}

function fillDomainFields(domain) {
    $("#commonStandardDomainName").val(domain?.commonStandardDomainName ?? "");
    $("#allowedValues").val(domain?.allowedValues ?? "");
    $("#storageFormat").val(domain?.storageFormat ?? "");
    $("#displayFormat").val(domain?.displayFormat ?? "");

    resetDuplicate();
    if (domain) {
        checkTermDuplicate($("#commonStandardTermAbbreviation").val(), domain.commonStandardDomainName);
    }
}

/**
 * 같은 영문약어명 + 도메인의 용어가 이미 있는지 바로 확인한다. (저장 시 서버 중복 기준과 동일)
 */
function checkTermDuplicate(abbreviation, domainName) {
    if (!abbreviation || !domainName) return;

    const duplicate = termState.duplicate;
    const seq = ++duplicate.seq;
    Object.assign(duplicate, { key: `${abbreviation}|${domainName}`, checking: true, duplicated: false });

    $.ajax({
        url: `/api/getStandardTerms/duplicate?abbreviation=${encodeURIComponent(abbreviation)}&domainName=${encodeURIComponent(domainName)}`,
        type: "GET",
        success: (response) => {
            if (seq !== duplicate.seq) return; // 이전 선택에 대한 응답은 무시

            Object.assign(duplicate, { checking: false, duplicated: response.duplicated === true });

            const $box = $("#termDuplicate").empty();
            if (duplicate.duplicated) {
                const names = (response.terms || [])
                    .map(t => `${t.commonStandardTermName}${t.isApprovalYn === "Y" ? "" : " (승인 대기)"}`)
                    .join(", ");
                $box.append($("<p></p>").addClass("table-name-error")
                    .text(`이미 같은 영문약어명(${abbreviation})과 도메인(${domainName})으로 등록된 용어가 있습니다: ${names}`));
            } else {
                $box.append($("<span></span>").addClass("gh-label gh-label-success").text("등록 가능 · 중복 없음"));
            }
        },
        error: () => {
            if (seq === duplicate.seq) duplicate.checking = false;
        }
    });
}

/**
 * 그리드 선택 callback Function
 */
export function selectRow(rowData, columnList, isManager, tableId) {
    // 그리드에서 숨긴 항목까지 상세보기로 보여주고, 승인 전 항목만 수정 팝업으로 이동한다.
    openDictionaryDetail({
        title: "표준용어 상세",
        rowData,
        columnList,
        onEdit: rowData.isApprovalYn === 'Y' ? null : () => openEditDialog(rowData, columnList, isManager, tableId)
    });
}

/**
 * 표준용어 수정 팝업
 */
function openEditDialog(rowData, columnList, isManager, tableId) {

    const dialogContent = $("<div></div>");
    const form = $("<form></form>").attr("id", `edit-${tableId}`);

    const inputType = $("<input>").attr("id", "type")
        .attr("name", "type")
        .attr("type", "hidden")
        .attr("value", "U");

    form.append(inputType);

    // key와 columnName을 매핑할 Map 생성
    const columnMap = new Map();
    columnList.forEach(col => {
        columnMap.set(col.column, col.columnName);
    });

    for (const [key, value] of Object.entries(rowData)) {
        const labelText = columnMap.get(key) || key; // 매칭되는 columnName 없으면 key 그대로 사용

        if (key === "commonStandardTermName") {
            const label = $("<label></label>")
                .attr("for", key)
                .text(labelText)
                .css({
                    display: "block",
                    marginTop: "10px",
                    fontWeight: "bold"
                });

            const wrapper = $("<div></div>").css({
                display: "flex",
                alignItems: "center",
                gap: "10px",
                marginBottom: "10px"
            });

            const input = $("<input>")
                .attr("type", "text")
                .attr("id", key)
                .attr("name", key)
                .val(value || "") // 기본값 처리
                .prop("readonly", true)
                .css({
                    flex: "1",
                    backgroundColor: "#f0f0f0",
                    height: "38px",
                    paddingLeft: "10px",
                    border: "1px solid #ccc",
                    borderRadius: "4px"
                });
            // 수정 시 표준용어명은 바꿀 수 없다 (읽기 전용)
            wrapper.append(input);
            form.append(label, wrapper); // label 따로, wrapper 따로 append
        }else{

            const label = $("<label></label>")
                .text(labelText)
                .attr("for", key)
                .css("display", "block")
                .css("margin-top", "10px");

            let input;

            if (["commonStandardTermDescription", "synonyms"].includes(key)) {
                input = $("<textarea></textarea>")
                    .attr("id", key)
                    .attr("name", key)
                    .val(value)
                    .css("width", "100%")
                    .css("height", "60px");
            }else if (key === "isApprovalYn") {
                continue;
            }else if(key === "revisionNumber"){
                input = $("<input>")
                    .attr("type", "number")
                    .attr("id", key)
                    .attr("name", key)
                    .val(value)
                    .css("width", "100%")
                    .on("keydown", function(event){
                        if (event.key === "e" || event.key === "E" || event.key === "+" || event.key === "-") {
                            event.preventDefault();
                        }
                    });
            } else {
                input = $("<input>")
                    .attr("type", "text")
                    .attr("id", key)
                    .attr("name", key)
                    .val(value)
                    .css("width", "100%");
            }

            // 수정 불가 필드 처리
            if (key === "id" || key === "projectId" || key === "commonStandardDomainName" || key === "allowedValues" || key === "storageFormat" || key === "displayFormat" || key === "synonyms" || key === "commonStandardTermName" || key === "commonStandardTermAbbreviation") {
                input.prop("readonly", true);
                input.css("background-color", "#f0f0f0");
            }
            form.append(label, input);
        }
    }

    if(isManager || rowData['isApprovalYn'] === 'N'){
        const saveBtn = $("<button></button>")
            .attr("type", "button")
            .text("저장")
            .attr("id", "approvalSubmit")
            .attr("name", "approvalSubmit")
            .addClass("btn btn-primary")
            .css("margin-top", "20px")
            .on("click", approvalSubmitClick);

        form.append(saveBtn);
    }else{
        $(`#edit-${tableId} input`).each((index, data) => {
            $(data).prop("readonly",true);
            $(data).css("background-color", "#f0f0f0");
        });
    }

    dialogContent.append(form);

    openDialog("div", {
        title: "표준용어 수정",
        content: dialogContent
    });
}

window.gridCallbacks["standardTerms_selectRow"] = selectRow;


/**
 * 저장/추가버튼을 눌려 저장을 수행한다.
 * @param e
 */
function approvalSubmitClick(e){
    e.preventDefault();

    const tableId = "standardTerms";

    const requiredFields = [
        "commonStandardDomainName",
        "allowedValues",
        "storageFormat",
        "displayFormat",
        "commonStandardTermName",
        "commonStandardTermDescription"
    ];

    const form = $(e.target).closest("form");

    // 등록 시 : 표준 단어 조회가 끝나고 등록 가능한 상태여야 한다.
    if (form.find("#type").val() === "C") {
        if (termState.pending) {
            window.openAlert("표준 단어를 조회하고 있습니다. 잠시 후 다시 시도해주세요.");
            return;
        }
        if (!termState.termName) {
            window.openAlert("표준용어명을 입력해주세요.");
            return;
        }
        if (!termState.valid) {
            window.openAlert("표준용어명에 등록할 수 없는 단어가 있습니다. 단어별 안내를 확인해주세요.");
            return;
        }
        if (!$("#commonStandardDomainName").val()) {
            window.openAlert("도메인을 선택해주세요.");
            return;
        }
        if (termState.duplicate.checking) {
            window.openAlert("중복 여부를 확인하고 있습니다. 잠시 후 다시 시도해주세요.");
            return;
        }
        if (termState.duplicate.duplicated) {
            window.openAlert("이미 같은 영문약어명과 도메인으로 등록된 용어가 있습니다.");
            return;
        }
    }

    // 단어 입력칸(termWordInput)은 화면용이므로 name 이 없는 항목은 보내지 않는다.
    const inputs = form.find("input[name], textarea[name]");

    const formData = {};
    let isValid = true;

    let type = "C";

    inputs.each(function () {
        const $input = $(this);
        const name = $input.attr("name");
        const value = $input.val();

        // 값 저장
        if(name !== "type"){
            formData[name] = value;
        }else{
            type = value;
        }

        // 필수값 체크
        if (requiredFields.includes(name)) {
            if (!value || value.trim() === "") {
                const labelText = form.find(`label[for='${name}']`).text() || name;
                alert(`필수값 누락: ${labelText}`);
                $input.focus();
                isValid = false;
                return false; // .each 중단
            }
        }
    });

    if (!isValid) return;

    const url = "/api/" + (type === "C" ? `insertDataDictionary/${tableId}` : `updateDataDictionary/${tableId}`);
    const ajaxType = type === "C" ? "POST" : "PUT"

    $.ajax({
        url: url,
        type : ajaxType,
        data : JSON.stringify(formData),
        success : (response) => {

            if(response.result){
                window.openAlert(`정상적으로 ${type === "C" ? "등록" : "수정"}되었습니다.`, () => {
                    window.closeDialog("div");
                    window.searchGrid(tableId);
                });
            }
        }
    })


}
