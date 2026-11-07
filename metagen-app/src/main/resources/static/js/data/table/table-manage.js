import {getCsrfToken, setupAjaxCsrf} from "../../common/csrf.js";
import {downloadFile} from "../../common/common.js";
import {registerRowDecorator, setApproval, setMono, setYN} from "../../common/grid_cells.js";

/**
 * 테이블 행 꾸미기 : 테이블명은 코드 글꼴, 마스터 여부/승인 여부는 라벨
 */
registerRowDecorator("tableManageGrid", (row, data, cellOf) => {
    setMono(cellOf("tableName"), data.tableName);
    setYN(cellOf("isMasterTable"), data.isMasterTable, "마스터");
    setApproval(cellOf("isApproval"), data.isApproval);
});

setupAjaxCsrf();
const tableId = "tableManageGrid";

$(document).ready(() => {

    /**
     * 템플릿 다운로드 버튼 클릭 이벤트
     */
    $("#download-template").on("click", function () {
        $.ajax({
            url: '/api/downloadTemplate/tableTemplate',
            type: 'GET',
            xhrFields: {
                responseType: 'blob'
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "테이블 템플릿.xlsx");
                closeDialog("div");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });

    /**
     * 추가버튼 선택 시 테이블을 생성한다.
     */
    $("#grd-add-tableManageGrid").on("click", function(){
        const type = "C";
        const form = createForm({}, type);

        window.openDialog('div', { title: type === 'C' ? '테이블 등록' : '테이블 수정', content: form });
        bindTableNameLookup({}, type);

        /**
         * 저장버튼 클릭
         */
        $("#btn-save-table").on("click", function(e){
            e.preventDefault();
            saveTable(type);
        });
    });

    /**
     * 삭제버튼 선택 시 테이블을 삭제처리한다.
     */
    $("#grd-delete-tableManageGrid").on("click", function(){
        const checkedData = getCheckedDataIsNonNull(tableId);

        if(!checkedData) return;

        let isApproval = true;
        checkedData.forEach((e) => {
            if(e.isApproval === 'Y'){
                isApproval = false;
            }
        });

        if(!isApproval){
            window.openAlert("체크된 데이터에 승인된 정보도 포함되어있습니다. 승인이 완료된 경우, 삭제가 불가능합니다.");
            return;
        }

        window.openConfirm("체크된 데이터를 삭제하시겠습니까?", () => {
            $.ajax({
                url: "/api/deleteTable",
                type: "DELETE",
                data: JSON.stringify(checkedData),
                success: (response) => {
                    if(response.result){
                        openAlert("정상적으로 삭제되었습니다.", () => {
                            window.searchGrid(tableId);
                        });
                    }
                }
            })
        });

    });


    $("#grd-active-tableManageGrid").on("click", () => {
        updateActive(true);
    });


    $("#grd-unactive-tableManageGrid").on("click", () => {
        updateActive(false);
    });

    $("#excel-upload").on("click", () => {
        $("#upload-file").click();
    });

    /**
     * 엑셀 파일이 업로드 된 이후 서버에 전송 및 파싱처리
     */
    $("#upload-file").on("change", () => {
        window.openConfirm("업로드 하시겠습니까?", () => {
            const file = $("#upload-file")[0].files[0]; // 선택한 파일 가져오기
            if (!file) {
                alert("업로드할 파일을 선택해주세요.");
                return;
            }

            // FormData 생성 후 파일 추가
            const formData = new FormData();
            formData.append("file", file);

            for (let pair of formData.entries()) {
                console.log(pair[0] + ', ' + pair[1]);
            }

            $.ajax({
                url: "/api/uploadTableExcelFile",
                type: "POST",
                data: formData,
                processData: false,  // FormData를 쿼리 스트링으로 변환하지 않음
                contentType: false,  // `multipart/form-data`가 자동 설정되도록 false로 설정
                beforeSend: function (xhr) {  // setupAjaxCsrf() 영향 방지
                    xhr.setRequestHeader('X-XSRF-TOKEN', getCsrfToken()); // CSRF 토큰만 추가
                },
                success: function (response) {
                    if(response.result === "success"){
                        window.openAlert("업로드가 완료되었습니다.");
                        window.searchGrid(tableId);
                        $("#upload-file").val('');
                    }
                },
                error: function (xhr, status, error) {
                    alert("파일 업로드 중 오류가 발생했습니다.");
                    console.error("업로드 실패:", status, error);
                }
            });
        });
    });

});

function updateActive(type){
    const checkedData = getCheckedDataIsNonNull(tableId);
    if(!checkedData) return;

    const msg = type ? "승인" : "승인취소";

    window.openConfirm(`체크된 테이블 정보를 ${msg}하시겠습니까?`, () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApproval === (type ? 'N' : 'Y')){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert(`${msg} 필요한 정보가 선택되지 않았습니다.`);
            return;
        }

        $.ajax({
            url: `/api/saveTable/${type}`,
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert(`정상적으로 ${msg}처리 되었습니다.`, () => {
                        window.searchGrid(tableId);
                    });
                }
            }
        });
    });
}

/**
 * 폼을 생성한다.
 * @param rowData
 * @param type
 * @returns {string}
 */
function createForm(rowData, type) {
    const korNameHelp = type === "C"
        ? "표준 단어를 _ 또는 공백으로 구분해 입력하면 프로젝트 접두어 + 단어의 영문약어로 테이블명을 만듭니다."
        : "한글명을 바꾸면 표준 단어 기준으로 테이블명을 다시 만듭니다. 그대로 두면 현재 테이블명을 유지합니다.";

    return `<form id="editTableForm" class="edit-form">
        <input type="hidden" name="id" value="${rowData.id ?? 0}"/>
        <input type="hidden" name="type" value="${type}" />

        <div class="form-group">
            <label for="tableKorName">테이블 한글명</label>
            <input type="text" class="form-control" name="tableKorName" id="tableKorName"
                   value="${escapeHtml(rowData.tableKorName)}"
                   placeholder="예) 사용_내역_관리" autocomplete="off" ${type === "C" ? "required" : ""}/>
            <p class="table-name-help">${korNameHelp}</p>
            <div id="tableNameWords" class="table-name-words" aria-live="polite"></div>
        </div>

        <div class="form-group">
            <label for="tableName">테이블명</label>
            <input type="text" class="form-control gh-mono" name="tableName" id="tableName"
                   value="${escapeHtml(rowData.tableName)}" placeholder="한글명을 입력하면 자동으로 생성됩니다." readonly/>
        </div>

        <div class="form-group">
            <label for="tableDescription">테이블 설명</label>
            <input type="text" class="form-control" name="tableDescription" id="tableDescription"
                   value="${escapeHtml(rowData.tableDescription)}" placeholder="비워두면 한글명으로 채웁니다."/>
        </div>

        <div class="form-check" style="margin: 10px 0;">
            <input type="checkbox" class="form-check-input" name="isMasterTable" id="isMasterTable"
                   ${rowData.isMasterTable === 'Y' ? 'checked' : ''} />
            <label class="form-check-label" for="isMasterTable">마스터 테이블 여부</label>
        </div>

        <div class="form-group" style="margin-bottom: 10px;">
            <label for="sortOrder">정렬 순서</label>
            <input type="number" class="form-control" name="sortOrder" id="sortOrder"
                   value="${rowData.sortOrder ?? ''}"
                   onkeydown="if (event.key === 'e' || event.key === 'E' || event.key === '+' || event.key === '-') event.preventDefault();" />
        </div>

        <button type="submit" class="btn btn-primary" id="btn-save-table">저장</button>
    </form>`
}


/**
 * 표준 단어 기준 테이블명 조회 상태 (팝업 하나만 열리므로 모듈 변수로 관리)
 */
const tableNameState = {
    originalKorName: "", // 수정 팝업을 열 때 저장되어 있던 한글명 (그대로면 테이블명을 다시 만들지 않는다)
    korName: "",      // 마지막으로 조회한 한글명
    result: null,     // 마지막 조회 결과 (TableNameDto)
    pending: false,   // 조회 중 여부
    requestSeq: 0,    // 늦게 도착한 이전 응답 무시용
    autoDescription: "" // 한글명으로 자동 입력한 설명 (사용자가 직접 바꾸면 덮어쓰지 않는다)
};

const WORD_STATUS = {
    APPROVED: { variant: "success", text: "" },
    NOT_APPROVED: { variant: "attention", text: "미승인" },
    NOT_FOUND: { variant: "danger", text: "미등록" }
};

/**
 * 테이블 한글명 입력 시 표준 단어를 조회해 테이블명을 만든다.
 */
function bindTableNameLookup(rowData, type) {
    const originalTableName = rowData.tableName ?? "";
    let timer = null;

    Object.assign(tableNameState, {
        originalKorName: normalizeKorName(rowData.tableKorName),
        korName: "", result: null, pending: false, autoDescription: ""
    });

    $("#tableKorName").on("input", function () {
        const korName = $(this).val().trim();
        clearTimeout(timer);

        // 비웠거나 (수정 시) 저장된 한글명과 같으면 현재 테이블명을 유지한다.
        if (!korName || isKorNameUnchanged(korName)) {
            Object.assign(tableNameState, { korName: "", result: null, pending: false });
            $("#tableNameWords").empty();
            $("#tableName").val(type === "C" ? "" : originalTableName);
            return;
        }

        tableNameState.pending = true;
        timer = setTimeout(() => lookupTableName(korName, type, originalTableName), 300);
    });
}

function lookupTableName(korName, type, originalTableName) {
    const seq = ++tableNameState.requestSeq;

    $.ajax({
        url: `/api/selectTable/tableName?tableKorName=${encodeURIComponent(korName)}`,
        type: "GET",
        success: (result) => {
            if (seq !== tableNameState.requestSeq) return; // 이전 입력에 대한 응답은 무시

            Object.assign(tableNameState, { korName, result, pending: false });
            renderTableNameWords(result);

            $("#tableName").val(result.valid ? result.tableName : (type === "C" ? "" : originalTableName));

            // 설명이 비어있거나 이전에 자동 입력한 값이면 한글명(공백 구분)으로 채운다.
            const $description = $("#tableDescription");
            const current = $description.val().trim();
            if (result.valid && (!current || current === tableNameState.autoDescription)) {
                $description.val(result.tableKorName);
                tableNameState.autoDescription = result.tableKorName;
            }
        },
        error: () => {
            if (seq === tableNameState.requestSeq) tableNameState.pending = false;
        }
    });
}

/**
 * 단어별 변환 결과 : 승인된 단어는 "사용 USE", 아니면 "통계 미등록" 형태의 라벨
 */
function renderTableNameWords(result) {
    const $words = $("#tableNameWords").empty();

    // 프로젝트 설정의 접두어 (시스템 관리 > 프로젝트 관리)
    if (result.tablePrefix && result.words.length > 0) {
        $words.append($("<span></span>").addClass("gh-label")
            .attr("title", "프로젝트 설정의 테이블명 접두어")
            .append($("<span></span>").text("접두어"))
            .append($("<span></span>").addClass("table-name-abbr").text(result.tablePrefix)));
    }

    result.words.forEach(word => {
        const status = WORD_STATUS[word.status] ?? WORD_STATUS.NOT_FOUND;
        const $label = $("<span></span>").addClass(`gh-label gh-label-${status.variant}`);

        $label.append($("<span></span>").text(word.word));
        $label.append($("<span></span>").addClass("table-name-abbr")
            .text(word.status === "APPROVED" ? word.abbreviation : status.text));

        if (word.englishName) $label.attr("title", word.englishName);

        $words.append($label);
    });

    if (!result.valid && result.words.length > 0) {
        $words.append($("<p></p>").addClass("table-name-error")
            .text("미등록·미승인 단어는 데이터 사전에서 표준 단어로 등록·승인한 뒤 사용할 수 있습니다."));
    }
}

/**
 * 한글명 비교용 정규화 : "사용_내역_관리" / " 사용 내역  관리 " → "사용 내역 관리"
 */
function normalizeKorName(value) {
    return String(value ?? "").trim().split(/[_\s]+/).filter(Boolean).join(" ");
}

function isKorNameUnchanged(korName) {
    return tableNameState.originalKorName !== "" && normalizeKorName(korName) === tableNameState.originalKorName;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}

function saveTable(type){
    const $form = $("#editTableForm");

    const msg = type === "C" ? "저장" : "수정";
    const inputKorName = $form.find("[name='tableKorName']").val().trim();

    // 수정 시 저장된 한글명을 그대로 두면 서버에 보내지 않는다 (기존 테이블명/한글명 유지)
    const korName = type === "U" && isKorNameUnchanged(inputKorName) ? "" : inputKorName;

    // 한글명을 입력했다면 (등록은 필수) 표준 단어 조회가 끝나고 모든 단어가 승인 단어여야 한다.
    if (type === "C" && !korName) {
        openAlert("테이블 한글명은(는) 필수 입력 항목입니다.");
        return;
    }

    if (korName) {
        if (tableNameState.pending || tableNameState.korName !== korName) {
            openAlert("표준 단어를 조회하고 있습니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        if (!tableNameState.result?.valid) {
            openAlert("테이블 한글명에 승인된 표준 단어가 아닌 단어가 있습니다.");
            return;
        }
    }

    // 한글명 없이 수정하는 경우에는 설명을 직접 입력해야 한다. (한글명이 있으면 서버에서 채운다)
    if (!korName && !$form.find("[name='tableDescription']").val().trim()) {
        openAlert("테이블 설명은(는) 필수 입력 항목입니다.");
        return;
    }

    const tableData = {
        id: $form.find("[name='id']").val(),
        tableKorName: korName,
        tableName: $form.find("[name='tableName']").val(),
        tableDescription: $form.find("[name='tableDescription']").val(),
        isMasterTable: $form.find("[name='isMasterTable']").is(":checked") ? "Y" : "N",
        sortOrder: parseInt($form.find("[name='sortOrder']").val()) || 0
    };

    const method = type === "C" ? "POST" : "PUT";

    openConfirm(`${msg}하시겠습니까?`, () => {
        $.ajax({
            url: "/api/saveTable", // 필요 시 type에 따라 URL 분기 가능
            type: method,
            data: JSON.stringify(tableData),
            success: (response) => {
                if (response.result) {
                    openAlert(`정상적으로 ${msg}되었습니다.`, () => {
                        window.closeDialog("div");
                        window.searchGrid(tableId);
                    });
                }
            }
        });
    });
}

export function tableSelectRow(rowData, columnList, isManager, tableId){
    if(rowData.isApproval === "Y") return;

    const type = "U";
    const form = createForm(rowData, type);

    window.openDialog('div', { title: type === 'C' ? '테이블 등록' : '테이블 수정', content: form });
    bindTableNameLookup(rowData, type);

    /**
     * 저장버튼 클릭
     */
    $("#btn-save-table").on("click", function(e){
        e.preventDefault();
        saveTable(type);
    });
}


window.gridCallbacks["tableManageGrid_selectRow"] = tableSelectRow;
