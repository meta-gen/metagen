import {getCsrfToken, setupAjaxCsrf} from "../../common/csrf.js";
import {downloadFile} from "../../common/common.js";
import {applyAll, registerRowDecorator, setApproval, setMono, setYN} from "../../common/grid_cells.js";
import {openColumnTableDetail} from "../../popup/column_table_detail.js";

/**
 * 컬럼 행 꾸미기 : 테이블/컬럼명·데이터 타입은 코드 글꼴, Y/N 은 라벨, 승인여부는 상태 라벨
 */
registerRowDecorator("columnManageGrid", (row, data, cellOf) => {
    applyAll(cellOf, data, ["tableName", "columnName", "dataType", "refTableName"], setMono);
    setYN(cellOf("isPk"), data.isPk, "PK");
    applyAll(cellOf, data, ["isNullable", "isMasterData", "isRequired", "isSensitive", "isUnique", "isIndex", "isEncrypted"], setYN);
    setApproval(cellOf("isApproval"), data.isApproval);
});

const tableId = "columnManageGrid";

$(document).ready(() => {
    setupAjaxCsrf();

    /**
     * 템플릿 다운로드 버튼 클릭 이벤트
     */
    $("#download-template").on("click", function () {
        $.ajax({
            url: '/api/downloadTemplate/columnTemplate',
            type: 'GET',
            xhrFields: {
                responseType: 'blob'
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "컬럼 템플릿.xlsx");
                closeDialog("div");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });

    $("#grd-add-columnManageGrid").on("click", () => {
       const type = "C";
       const form = createForm({}, type);

        window.openDialog('div', { title: type === 'C' ? '컬럼 등록' : '컬럼 수정', content: form });

        /**
         * 저장버튼 클릭
         */
        $("#btn-save-column").on("click", function(e){
            e.preventDefault();
            saveColumn(type);
        });

        // 테이블 / 표준용어(컬럼명) 실시간 조회
        bindColumnLookup();
        $("#tableSearchInput").trigger("focus");
    });

    /**
     * 승인버튼 클릭 시
     */
    $("#grd-active-columnManageGrid").on("click", () => {
        updateActive(true);
    });

    /**
     * 승인취소버튼 클릭 시
     */
    $("#grd-unactive-columnManageGrid").on("click", () => {
        updateActive(false);
    });

    $("#grd-delete-columnManageGrid").on("click", () => {
       deleteColumns();
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
                url: "/api/uploadColumnExcelFile",
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
                    window.openAlert(xhr.responseJSON.message);
                }
            });
        });
    });

});

/**
 * 폼을 생성한다.
 * @param rowData
 * @param type
 * @returns {string}
 */
function createForm(rowData, type) {

    const dataTypes = ["VARCHAR", "CHAR", "NUMBER", "DATE", "TIMESTAMP", "BOOLEAN", "CLOB", "BLOB"];

    const dataTypeDatalist = `
        <datalist id="dataTypeOptions">
            ${dataTypes.map(dt => `<option value="${dt}"></option>`).join("")}
        </datalist>
    `;

    return `<form id="editTableForm" class="edit-form">
        <input type="hidden" name="id" value="${rowData.id ?? 0}"/>
        <input type="hidden" name="type" value="${type}" />
        <input type="hidden" name="tableInfoId" id="tableInfoId" value=""/>
        <input type="hidden" name="termId" id="termId" value=""/>
        <input type="hidden" name="tableName" id="tableName" value=""/>

        <!-- 필수 정보 : 테이블 (승인된 테이블만) -->
        <div class="form-group">
            <label for="tableSearchInput">테이블</label>
            <input type="text" class="form-control" id="tableSearchInput" autocomplete="off"
                   placeholder="테이블명 또는 일부를 입력하세요 (예: USE_HIST)"/>
            <select class="form-control lookup-select" id="tableSelect" size="4" hidden aria-label="테이블 선택"></select>
            <div id="tableLookupResult" class="table-name-words" aria-live="polite"></div>
        </div>

        <!-- 필수 정보 : 컬럼명 (표준용어에서 선택) -->
        <div class="form-group">
            <label for="termSearchInput">컬럼명 (표준용어)</label>
            <div class="lookup-search-row">
                <select class="form-control" id="termSearchType" aria-label="표준용어 검색 기준">
                    <option value="termName">표준용어명</option>
                    <option value="termAbbreviation">영문약어명</option>
                </select>
                <input type="text" class="form-control" id="termSearchInput" autocomplete="off"
                       placeholder="예: 사용자 이름 / USER_NM"/>
            </div>
            <select class="form-control lookup-select" id="termSelect" size="5" hidden aria-label="표준용어 선택"></select>
            <div id="termLookupResult" class="table-name-words" aria-live="polite"></div>
            <input type="text" class="form-control gh-mono" name="columnName" id="columnName"
                   placeholder="표준용어를 선택하면 자동으로 입력됩니다." readonly />
            <div id="columnDuplicateResult" class="table-name-words" aria-live="polite"></div>
        </div>

        <div class="form-group">
            <label for="dataType">데이터 타입</label>
            <input type="text" class="form-control" name="dataType" id="dataType"
                   list="dataTypeOptions" value="${rowData.dataType ?? ''}" />
        </div>
        ${dataTypeDatalist}

        <div class="form-group">
            <label for="columnDesc">컬럼 설명</label>
            <input type="text" class="form-control" name="columnDesc" id="columnDesc"
                   value="${rowData.columnDesc ?? ''}" />
        </div>

        <div class="form-check">
            <input type="checkbox" class="form-check-input" name="isNullable" id="isNullable"
                   ${rowData.isNullable === 'Y' ? 'checked' : ''} />
            <label class="form-check-label" for="isNullable">NULL 허용 여부</label>
        </div>

        <div class="form-check">
            <input type="checkbox" class="form-check-input" name="isNullable" id="isPk"
                   ${rowData.isPk === 'Y' ? 'checked' : ''} />
            <label class="form-check-label" for="isPk">PK 여부</label>
        </div>

        <div class="form-group">
            <label for="maxLength">최대 길이</label>
            <input type="number" class="form-control" name="maxLength" id="maxLength"
                   value="${rowData.maxLength ?? ''}" />
        </div>

        <!-- 부가 정보 펼치기 -->
        <div class="form-group mt-3">
            <button type="button" class="btn btn-sm" onclick="toggleExtraInfo()">부가 정보 보기 ▼</button>
        </div>

        <div id="extra-info-section" style="display: none; margin-top: 12px; border-top: 1px solid #d1d9e0b3; padding-top: 16px;">

            <div class="form-group">
                <label for="precision">정밀도</label>
                <input type="number" class="form-control" name="precision" id="precision"
                       value="${rowData.precision ?? ''}" />
            </div>

            <div class="form-group">
                <label for="scale">소수 자릿수</label>
                <input type="number" class="form-control" name="scale" id="scale"
                       value="${rowData.scale ?? ''}" />
            </div>

            <div class="form-group">
                <label for="defaultValue">기본값</label>
                <input type="text" class="form-control" name="defaultValue" id="defaultValue"
                       value="${rowData.defaultValue ?? ''}" />
            </div>

            <div class="form-group">
                <label for="sortOrder">정렬 순서</label>
                <input type="number" class="form-control" name="sortOrder" id="sortOrder"
                       value="${rowData.sortOrder ?? ''}" />
            </div>

            <div class="form-group">
                <label for="refTableName">참조 테이블명</label>
                <input type="text" class="form-control" name="refTableName" id="refTableName"
                       value="${rowData.refTableName ?? ''}" />
            </div>

            <div class="form-group">
                <label for="example">데이터 예시</label>
                <input type="text" class="form-control" name="example" id="example"
                       value="${rowData.example ?? ''}" />
            </div>

            ${[
        { id: 'isMasterData', label: '마스터 데이터 여부' },
        { id: 'isRequired', label: '필수 입력 여부' },
        { id: 'isSensitive', label: '민감정보 여부' },
        { id: 'isUnique', label: '고유값 여부' },
        { id: 'isIndex', label: '인덱스 생성 여부' },
        { id: 'isEncrypted', label: '암호화 필요 여부' }
    ].map(info => `
                <div class="form-check">
                    <input type="checkbox" class="form-check-input" name="${info.id}" id="${info.id}"
                        ${rowData[info.id] === 'Y' ? 'checked' : ''} />
                    <label class="form-check-label" for="${info.id}">${info.label}</label>
                </div>
            `).join("")}
        </div>

        <button type="submit" class="btn btn-primary" id="btn-save-column">저장</button>
    </form>`;
}

/**
 * 컬럼 저장을 수행한다.
 * @param type
 */
function saveColumn(type){
    const form = document.getElementById("editTableForm");

    if (!form) {
        openAlert("폼을 찾을 수 없습니다.");
        return;
    }

    // 등록 : 조회가 끝나고 테이블/표준용어를 선택했으며 컬럼명이 중복되지 않아야 한다.
    if (type === "C") {
        if (columnLookup.pending) {
            openAlert("조회 중입니다. 잠시 후 다시 시도해주세요.");
            return;
        }
        if (!form.tableInfoId?.value) {
            openAlert("테이블을 선택해주세요.");
            return;
        }
        if (!form.termId?.value) {
            openAlert("컬럼명으로 사용할 표준용어를 선택해주세요.");
            return;
        }
        if (columnLookup.duplicated) {
            openAlert("선택한 테이블에 이미 같은 컬럼명이 등록되어 있습니다.");
            return;
        }
    }

    const requiredFields = {
        tableName: "테이블명을 선택해주세요.",
        columnName: "컬럼명을 입력해주세요.",
        dataType: "데이터 타입을 선택해주세요.",
        columnDesc: "컬럼 설명을 입력해주세요."
    };

    for (const [field, message] of Object.entries(requiredFields)) {
        const value = form[field]?.value?.trim();
        if (!value) {
            openAlert(message);
            return;
        }
    }

    // 데이터 수집
    const data = {
        id: Number(form.id?.value || 0),
        tableInfoId: Number(form.tableInfoId?.value),
        termId: Number(form.termId?.value),
        tableName: form.tableName.value.trim(),
        columnName: form.columnName.value.trim(),
        dataType: form.dataType.value.trim(),
        columnDesc: form.columnDesc.value.trim(),
        maxLength: form.maxLength?.value ? Number(form.maxLength.value) : null,
        precision: form.precision?.value ? Number(form.precision.value) : null,
        scale: form.scale?.value ? Number(form.scale.value) : null,
        defaultValue: form.defaultValue?.value?.trim() || "",
        sortOrder: form.sortOrder?.value ? Number(form.sortOrder.value) : null,
        refTableName: form.refTableName?.value?.trim() || "",
        example: form.example?.value?.trim() || "",
        isPk: form.isPk?.checked ? "Y" : "N",
        isNullable: form.isNullable?.checked ? "Y" : "N",
        isMasterData: form.isMasterData?.checked ? "Y" : "N",
        isRequired: form.isRequired?.checked ? "Y" : "N",
        isSensitive: form.isSensitive?.checked ? "Y" : "N",
        isUnique: form.isUnique?.checked ? "Y" : "N",
        isIndex: form.isIndex?.checked ? "Y" : "N",
        isEncrypted: form.isEncrypted?.checked ? "Y" : "N"
    };

    const msg = type === "C"? "추가" : "저장";

    const restType = type === "C" ? "POST" : "PUT";

    openConfirm(`${msg}하시겠습니까?`, () => {
        $.ajax({
            url: "/api/saveColumn",
            type: restType,
            data: JSON.stringify(data),
            success: (response) => {
                if(response.result){
                    openAlert(`정상적으로 ${msg}되었습니다.`, () => {
                        window.closeDialog("div");
                        window.searchGrid(tableId);
                    });
                }
            }
        });
    });
}

/**
 * 컬럼 등록 시 테이블/표준용어 조회 상태 (등록 다이얼로그 하나만 열리므로 모듈 변수로 관리)
 */
const columnLookup = {
    tables: [],      // 조회된 승인 테이블
    terms: [],       // 조회된 표준용어
    tableSeq: 0,     // 늦게 도착한 이전 응답 무시용
    termSeq: 0,
    dupSeq: 0,
    pending: false,  // 조회 중 여부 (테이블/용어/중복)
    duplicated: false
};

/**
 * 테이블명 / 표준용어 입력 즉시 조회한다.
 */
function bindColumnLookup() {
    Object.assign(columnLookup, { tables: [], terms: [], pending: false, duplicated: false });

    let tableTimer = null;
    let termTimer = null;

    $("#tableSearchInput").on("input", function () {
        const keyword = $(this).val().trim();
        clearTimeout(tableTimer);
        clearSelectedTable();

        if (!keyword) {
            $("#tableSelect").attr("hidden", true).empty();
            columnLookup.pending = false;
            return;
        }

        columnLookup.pending = true;
        tableTimer = setTimeout(() => lookupTables(keyword), 300);
    });

    $("#tableSelect").on("change", function () {
        const table = columnLookup.tables.find(t => String(t.id) === $(this).val());
        selectTable(table);
    });

    $("#termSearchInput, #termSearchType").on("input change", function () {
        const keyword = $("#termSearchInput").val().trim();
        clearTimeout(termTimer);
        clearSelectedTerm();

        if (!keyword) {
            $("#termSelect").attr("hidden", true).empty();
            columnLookup.pending = false;
            return;
        }

        columnLookup.pending = true;
        termTimer = setTimeout(() => lookupTerms($("#termSearchType").val(), keyword), 300);
    });

    $("#termSelect").on("change", function () {
        const term = columnLookup.terms.find(t => String(t.id) === $(this).val());
        selectTerm(term);
    });
}

function lookupTables(keyword) {
    const seq = ++columnLookup.tableSeq;

    $.ajax({
        url: `/api/selectColumn/table/${encodeURIComponent(keyword)}`,
        type: "GET",
        success: (response) => {
            if (seq !== columnLookup.tableSeq) return;
            columnLookup.pending = false;

            const tables = response.tableInfos || [];
            columnLookup.tables = tables;

            const $select = $("#tableSelect").empty();
            tables.forEach(table => {
                const label = table.tableKorName ? `${table.tableName} · ${table.tableKorName}` : table.tableName;
                $select.append($("<option></option>").val(table.id).text(label));
            });
            $select.attr("hidden", tables.length === 0);

            if (tables.length === 0) {
                showLookupMessage("#tableLookupResult", "table-name-error", "조건에 맞는 승인된 테이블이 없습니다.");
                return;
            }

            // 입력과 정확히 같은 테이블이 있거나 결과가 하나뿐이면 바로 선택
            const exact = tables.find(t => t.tableName === keyword.toUpperCase());
            const auto = exact ?? (tables.length === 1 ? tables[0] : null);
            if (auto) {
                $select.val(String(auto.id));
                selectTable(auto);
            } else {
                showLookupMessage("#tableLookupResult", "form-help", `승인된 테이블 ${tables.length}건 · 목록에서 선택하세요.`);
            }
        },
        error: () => {
            if (seq === columnLookup.tableSeq) columnLookup.pending = false;
        }
    });
}

function clearSelectedTable() {
    $("#tableName").val("");
    $("#tableInfoId").val("");
    $("#tableLookupResult").empty();
    refreshColumnDuplicate(); // 중복은 테이블 기준이므로 다시 확인
}

/**
 * 테이블 선택 : 테이블에 이미 등록된 컬럼을 함께 보여준다.
 */
function selectTable(table) {
    clearSelectedTable();
    if (!table) return;

    $("#tableName").val(table.tableName);
    $("#tableInfoId").val(table.id);

    const $result = $("#tableLookupResult");
    $result.append($("<span></span>").addClass("gh-label gh-label-success")
        .append($("<span></span>").addClass("table-name-abbr").text(table.tableName))
        .append($("<span></span>").text("선택됨")));

    refreshColumnDuplicate();

    $.ajax({
        url: `/api/selectColumn/tableColumn/${encodeURIComponent(table.id)}`,
        type: "GET",
        success: (response) => {
            if (String($("#tableInfoId").val()) !== String(table.id)) return; // 그 사이 다른 테이블 선택

            const columns = response.columnInfos || [];
            const text = columns.length === 0
                ? "등록된 컬럼이 없습니다."
                : `등록된 컬럼 ${columns.length}개 : ${columns.map(c => c.columnName).join(", ")}`;
            $result.append($("<p></p>").addClass("form-help lookup-columns").text(text));
        }
    });
}

function lookupTerms(type, keyword) {
    const seq = ++columnLookup.termSeq;

    $.ajax({
        url: `/api/selectColumn/term/${encodeURIComponent(type)}/${encodeURIComponent(keyword)}`,
        type: "GET",
        success: (response) => {
            if (seq !== columnLookup.termSeq) return;
            columnLookup.pending = false;

            const terms = response.termDtos || [];
            columnLookup.terms = terms;

            const $select = $("#termSelect").empty();
            terms.forEach(term => {
                const approval = term.isApprovalYn === "Y" ? "" : " (미승인)";
                $select.append($("<option></option>").val(term.id)
                    .text(`${term.commonStandardTermAbbreviation} · ${term.commonStandardTermName} · ${term.dataType ?? "-"}${approval}`));
            });
            $select.attr("hidden", terms.length === 0);

            if (terms.length === 0) {
                showLookupMessage("#termLookupResult", "table-name-error", "조건에 맞는 표준용어가 없습니다. 데이터 사전에서 먼저 등록해주세요.");
                return;
            }

            if (terms.length === 1) {
                $select.val(String(terms[0].id));
                selectTerm(terms[0]);
            } else {
                showLookupMessage("#termLookupResult", "form-help", `표준용어 ${terms.length}건 · 목록에서 선택하세요.`);
            }
        },
        error: () => {
            if (seq === columnLookup.termSeq) columnLookup.pending = false;
        }
    });
}

function clearSelectedTerm() {
    $("#termId, #columnName").val("");
    $("#termLookupResult").empty();
    refreshColumnDuplicate();
}

/**
 * 표준용어 선택 : 컬럼명/설명/데이터 타입/최대 길이를 채우고 컬럼명 중복을 확인한다.
 */
function selectTerm(term) {
    clearSelectedTerm();
    if (!term) return;

    $("#termId").val(term.id);
    $("#columnName").val(term.commonStandardTermAbbreviation);
    $("#columnDesc").val(String(term.commonStandardTermName ?? "").replaceAll("_", " ")); // 설명은 읽기 쉽게 공백으로

    // 데이터 타입은 "VARCHAR(100)" 형태 : 타입과 길이를 나눠 넣는다.
    const match = String(term.dataType ?? "").match(/^\s*([^(]+?)\s*(?:\((\d+)\))?\s*$/);
    $("#dataType").val(match ? match[1] : (term.dataType ?? ""));
    if (match?.[2]) $("#maxLength").val(match[2]);

    const $result = $("#termLookupResult");
    if (term.isApprovalYn !== "Y") {
        $result.append($("<p></p>").addClass("table-name-warning")
            .text("미승인 표준용어입니다. 등록은 가능하지만 용어 승인이 필요합니다."));
    }

    refreshColumnDuplicate();
}

/**
 * 선택한 테이블에 같은 컬럼명이 있는지 확인한다. (테이블과 표준용어를 모두 골랐을 때)
 */
function refreshColumnDuplicate() {
    const tableInfoId = $("#tableInfoId").val();
    const tableName = $("#tableName").val();
    const columnName = $("#columnName").val();

    const seq = ++columnLookup.dupSeq; // 진행 중인 이전 확인 응답 무시
    columnLookup.duplicated = false;
    const $result = $("#columnDuplicateResult").empty();

    if (!columnName) return;

    if (!tableInfoId) {
        $result.append($("<p></p>").addClass("form-help").text("테이블을 선택하면 컬럼명 중복을 확인합니다."));
        return;
    }

    columnLookup.pending = true;

    $.ajax({
        url: `/api/selectColumn/duplicate?tableInfoId=${encodeURIComponent(tableInfoId)}&columnName=${encodeURIComponent(columnName)}`,
        type: "GET",
        success: (response) => {
            if (seq !== columnLookup.dupSeq) return;
            columnLookup.pending = false;
            columnLookup.duplicated = response.duplicated === true;

            if (columnLookup.duplicated) {
                $result.append($("<p></p>").addClass("table-name-error")
                    .text(`${tableName} 테이블에 이미 같은 컬럼명(${columnName})이 등록되어 있습니다.`));
            } else {
                $result.append($("<span></span>").addClass("gh-label gh-label-success").text(`${tableName} 에 등록 가능 · 중복 없음`));
            }
        },
        error: () => {
            if (seq === columnLookup.dupSeq) columnLookup.pending = false;
        }
    });
}

function showLookupMessage(target, cls, text) {
    $(target).empty().append($("<p></p>").addClass(cls).text(text));
}

window.toggleExtraInfo = function () {
    const section = document.getElementById("extra-info-section");
    if (section.style.display === "none") {
        section.style.display = "block";
    } else {
        section.style.display = "none";
    }
};



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
            url: `/api/saveColumn/approval/${type}`,
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

function deleteColumns(){
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
            url: "/api/deleteColumn",
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
}

/**
 *
 * 컬럼관리 목록 조회
 * @param rowData
 * @param columnList
 * @param isManager
 * @param tableId
 */
export function columnSelectRow(rowData, columnList, isManager, tableId){
    if(rowData.isApproval === "Y") return;

    $.ajax({
        url: `/api/selectColumn/detail/${rowData.id}`,
        type: "GET",
        success: (response) => {
            if(response.result){
                if(!(response.columnInfos?.length)){
                    openAlert("승인된 컬럼이 존재하여 수정이 불가능합니다.");
                    return;
                }

                openColumnTableDetail(response.columnInfos, {onSuccess: columnManageGridSuccess});
            }
        }
    })
}

window.gridCallbacks["columnManageGrid_selectRow"] = columnSelectRow;


function columnManageGridSuccess(){
    openAlert("정상적으로 순서가 저장되었습니다.", () => {
        window.searchGrid(tableId);
    });
}
