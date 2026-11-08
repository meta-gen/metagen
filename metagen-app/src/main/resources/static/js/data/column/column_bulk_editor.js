import {closePopupDialog, openPopupDialog} from "../../common/popup_dialog.js";
import {createTermForm} from "../dictionary/term_create_form.js";
import {applyRecommendation} from "./column_recommend.js";
import {openCommonColumnSet} from "./common_column_set.js";

const RECOMMEND_STORAGE_KEY = "metagen.columnBulk.recommend";

function loadRecommendSetting() {
    try {
        return localStorage.getItem(RECOMMEND_STORAGE_KEY) !== "N";
    } catch (e) {
        return true;
    }
}

function saveRecommendSetting(on) {
    try {
        localStorage.setItem(RECOMMEND_STORAGE_KEY, on ? "Y" : "N");
    } catch (e) {
        // 저장소를 쓸 수 없으면 이번 화면에서만 유지한다
    }
}

/**
 * 컬럼 일괄 편집 (테이블 관리 > 테이블 상세 > 컬럼 편집)
 *
 * - 표준용어를 검색해 여러 개를 체크한 뒤 한 번에 추가하고, 표 안에서 설명/타입/길이/PK/NULL/기본값 등을 바로 고친다.
 * - 승인 전 컬럼끼리 순서를 바꾸고, 승인 전 컬럼을 삭제(되돌리기 가능)한다. 승인된 컬럼은 잠근다.
 * - [일괄 저장] 은 PUT /api/saveColumn/bulk 한 번으로 보낸다. 서버 검증 오류가 있으면 아무것도 저장되지 않고 행별로 오류를 보여준다.
 * - 메인 다이얼로그(#mainConfirm)를 넓혀서 쓰고, 새 표준용어 등록은 그 위의 팝업 다이얼로그(#popupDialog)로 띄운다.
 * - 모든 값은 val / textContent 로 넣어 HTML 이 해석되지 않도록 한다.
 */

const DATA_TYPES = ["VARCHAR", "CHAR", "NUMBER", "INTEGER", "BIGINT", "DATE", "TIMESTAMP", "BOOLEAN", "CLOB", "BLOB"];

// 행 펼치기(상세)에서 고치는 Y/N 항목
const DETAIL_FLAGS = [
    ["isRequired", "필수 입력"],
    ["isUnique", "고유값"],
    ["isIndex", "인덱스"],
    ["isMasterData", "마스터 데이터"],
    ["isSensitive", "민감정보"],
    ["isEncrypted", "암호화"]
];

// 변경 여부 비교 / 저장에 쓰는 항목
const FIELD_KEYS = ["columnDesc", "dataType", "maxLength", "precision", "scale", "defaultValue", "refTableName", "example",
    "isPk", "isNullable", "isRequired", "isUnique", "isIndex", "isMasterData", "isSensitive", "isEncrypted"];

const NUMBER_KEYS = ["maxLength", "precision", "scale"];

const ICON_UP = '<svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true"><path fill="currentColor" d="M3.22 10.53a.749.749 0 0 1 0-1.06l4.25-4.25a.749.749 0 0 1 1.06 0l4.25 4.25a.749.749 0 1 1-1.06 1.06L8 6.811 4.28 10.53a.749.749 0 0 1-1.06 0Z"/></svg>';
const ICON_DOWN = '<svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true"><path fill="currentColor" d="M12.78 5.22a.749.749 0 0 1 0 1.06l-4.25 4.25a.749.749 0 0 1-1.06 0L3.22 6.28a.749.749 0 1 1 1.06-1.06L8 8.939l3.72-3.719a.749.749 0 0 1 1.06 0Z"/></svg>';

/**
 * @param table   테이블 행 데이터 (id, tableName, tableKorName, isApproval)
 * @param onSaved 저장 성공 후 호출 (다이얼로그는 닫힌 상태)
 */
export function openColumnBulkEditor(table, {onSaved} = {}) {
    $.ajax({
        url: `/api/selectColumn/tableColumn/${encodeURIComponent(table.id)}`,
        type: "GET",
        success: (response) => render(table, response.columnInfos || [], onSaved),
        error: (xhr) => {
            if (xhr.status === 403) window.openAlert("컬럼을 편집할 권한이 없습니다.");
        }
    });
}

function render(table, columnInfos, onSaved) {
    let seq = 0;
    const state = {
        rows: columnInfos
            .slice()
            .sort((a, b) => (a.sortOrder ?? Number.MAX_SAFE_INTEGER) - (b.sortOrder ?? Number.MAX_SAFE_INTEGER))
            .map(fromExisting),
        originalOrder: [],
        searchResults: [],
        searchSeq: 0,
        saving: false,
        recommend: loadRecommendSetting(), // 속성 자동 추천 사용 여부 (브라우저에 기억)
        pasteItems: []                     // 붙여넣기 결과 (줄마다 {input, status, terms, selectedId, checked})
    };
    state.originalOrder = state.rows.map(r => r.id);

    // ---------------------------------------------------------------- 화면 골격
    const $root = $("<div></div>").addClass("col-bulk");

    const $searchType = $("<select></select>").addClass("form-control").attr("aria-label", "표준용어 검색 기준")
        .append($("<option></option>").val("termName").text("표준용어명"))
        .append($("<option></option>").val("termAbbreviation").text("영문약어명"));
    const $searchInput = $("<input>").attr({type: "text", autocomplete: "off", placeholder: "예: 사용자 / USER_"}).addClass("form-control");
    const $results = $("<div></div>").addClass("col-bulk-results").attr("aria-live", "polite");
    const $addBtn = $("<button></button>").attr("type", "button").addClass("btn btn-sm btn-primary").text("선택 추가").prop("disabled", true);
    const $checkAllBtn = $("<button></button>").attr("type", "button").addClass("btn btn-sm").text("전체 선택").prop("disabled", true);
    const $createTermBtn = $("<button></button>").attr("type", "button").addClass("btn btn-sm").text("+ 새 표준용어 등록");
    const $recommendToggle = $("<label></label>").addClass("col-bulk-flag").attr("title",
        "여부 → NOT NULL·기본값 N, 등록 일시 → NOT NULL·CURRENT_TIMESTAMP, 등록자 → NOT NULL, 첫 식별자 컬럼 → PK").append(
        $("<input>").attr("type", "checkbox").addClass("form-check-input").prop("checked", state.recommend)
            .on("change", function () {
                state.recommend = this.checked;
                saveRecommendSetting(this.checked);
            }),
        $("<span></span>").text("속성 자동 추천")
    );

    // 검색 / 붙여넣기 전환
    const $modeSearch = $("<button></button>").attr({type: "button", "aria-pressed": true}).addClass("btn btn-sm col-bulk-mode is-active").text("검색해서 추가");
    const $modePaste = $("<button></button>").attr({type: "button", "aria-pressed": false}).addClass("btn btn-sm col-bulk-mode").text("붙여넣기로 추가");

    const $searchPane = $("<div></div>").addClass("col-bulk-pane").append(
        $("<div></div>").addClass("lookup-search-row").append($searchType, $searchInput),
        $results,
        $("<div></div>").addClass("col-bulk-search-actions").append($checkAllBtn, $addBtn)
    );

    const $pasteText = $("<textarea></textarea>").addClass("form-control col-bulk-paste-text").attr({
        rows: 5,
        placeholder: "설계서/엑셀에서 컬럼(용어명 또는 영문약어명)을 한 줄에 하나씩 붙여넣으세요.\n예)\n사용자 아이디\n사용자_이름\nUSE_YN\n(엑셀에서 여러 칸을 복사하면 첫 칸만 씁니다)"
    });
    const $pasteFindBtn = $("<button></button>").attr("type", "button").addClass("btn btn-sm").text("찾기");
    const $pasteResults = $("<div></div>").addClass("col-bulk-paste-results").attr("aria-live", "polite");
    const $pasteAddBtn = $("<button></button>").attr("type", "button").addClass("btn btn-sm btn-primary").text("체크한 용어 추가").prop("disabled", true);
    const $pastePane = $("<div></div>").addClass("col-bulk-pane").attr("hidden", true).append(
        $pasteText,
        $("<div></div>").addClass("col-bulk-search-actions").append($pasteFindBtn),
        $pasteResults,
        $("<div></div>").addClass("col-bulk-search-actions").append($pasteAddBtn)
    );

    // 다른 테이블에서 복사
    const $modeCopy = $("<button></button>").attr({type: "button", "aria-pressed": false}).addClass("btn btn-sm col-bulk-mode").text("다른 테이블에서 복사");
    const $copyTableInput = $("<input>").attr({type: "text", autocomplete: "off", placeholder: "복사할 테이블명 또는 일부 (예: TB_USER)"}).addClass("form-control");
    const $copyTableSelect = $("<select></select>").addClass("form-control lookup-select").attr({size: 4, "aria-label": "복사할 테이블 선택", hidden: true});
    const $copyResults = $("<div></div>").addClass("col-bulk-results col-bulk-copy-results").attr("aria-live", "polite");
    const $copyCheckAllBtn = $("<button></button>").attr("type", "button").addClass("btn btn-sm").text("전체 선택").prop("disabled", true);
    const $copyAddBtn = $("<button></button>").attr("type", "button").addClass("btn btn-sm btn-primary").text("체크한 컬럼 복사").prop("disabled", true);
    const $copyPane = $("<div></div>").addClass("col-bulk-pane").attr("hidden", true).append(
        $copyTableInput,
        $copyTableSelect,
        $copyResults,
        $("<div></div>").addClass("col-bulk-search-actions").append($copyCheckAllBtn, $copyAddBtn)
    );

    // 공통 컬럼 세트
    const $commonAddBtn = $("<button></button>").attr({type: "button", title: "프로젝트 공통 컬럼 세트(등록일시/등록자 등)를 표 끝에 한 번에 추가합니다."})
        .addClass("btn btn-sm").text("공통 컬럼 추가");
    const $commonManageBtn = $("<button></button>").attr({type: "button", title: "프로젝트 공통 컬럼 세트를 정합니다. (프로젝트 관리자)"})
        .addClass("btn btn-sm").text("공통 컬럼 세트 관리").attr("hidden", true);

    const $search = $("<section></section>").addClass("col-bulk-search").append(
        $("<div></div>").addClass("col-bulk-search-head").append(
            $("<div></div>").addClass("col-bulk-modes").append($modeSearch, $modePaste, $modeCopy),
            $("<div></div>").addClass("col-bulk-search-tools").append($recommendToggle, $commonAddBtn, $commonManageBtn, $createTermBtn)
        ),
        $searchPane,
        $pastePane,
        $copyPane
    );

    const MODES = [
        {$button: $modeSearch, $pane: $searchPane, $focus: $searchInput},
        {$button: $modePaste, $pane: $pastePane, $focus: $pasteText},
        {$button: $modeCopy, $pane: $copyPane, $focus: $copyTableInput}
    ];
    MODES.forEach(mode => mode.$button.on("click", () => {
        MODES.forEach(m => {
            const active = m === mode;
            m.$button.toggleClass("is-active", active).attr("aria-pressed", active);
            m.$pane.attr("hidden", !active);
        });
        mode.$focus.trigger("focus");
    }));

    const $generalError = $("<div></div>").addClass("col-bulk-general-error").attr("hidden", true);
    const $tbody = $("<tbody></tbody>");
    const $table = $("<table></table>").addClass("col-bulk-table").append(
        $("<thead></thead>").append($("<tr></tr>").append(
            ["#", "", "컬럼명", "컬럼 설명", "데이터 타입", "길이", "PK", "NULL", "기본값", "", ""]
                .map((h, i) => $("<th></th>").text(h).addClass(["", "col-move", "", "", "", "col-num", "col-chk", "col-chk", "", "col-icon", "col-icon"][i]))
        )),
        $tbody
    );
    const $datalist = $("<datalist></datalist>").attr("id", "colBulkDataTypes")
        .append(DATA_TYPES.map(t => $("<option></option>").val(t)));

    const $summary = $("<span></span>").addClass("col-bulk-summary");
    const $cancelBtn = $("<button></button>").attr("type", "button").addClass("btn").text("닫기");
    const $saveBtn = $("<button></button>").attr("type", "button").addClass("btn btn-primary").text("일괄 저장");

    $root.append(
        $("<p></p>").addClass("form-help col-bulk-help")
            .text("승인된 컬럼은 잠겨 있습니다. 승인 전 컬럼은 바로 고치거나 순서를 바꾸고 삭제할 수 있습니다. 컬럼명(용어)을 바꾸려면 삭제 후 다시 추가하세요. \"속성 자동 추천\"을 켜 두면 여부/등록 일시/등록자/첫 식별자 컬럼의 NOT NULL·기본값·PK 를 미리 채웁니다."),
        $search,
        $generalError,
        $("<div></div>").addClass("col-bulk-table-wrap").append($table),
        $datalist,
        $("<div></div>").addClass("gh-dialog-actions col-bulk-actions").append($summary, $cancelBtn, $saveBtn)
    );

    // ---------------------------------------------------------------- 표준용어 검색
    let searchTimer = null;
    $searchInput.add($searchType).on("input change", () => {
        clearTimeout(searchTimer);
        const keyword = $searchInput.val().trim();
        if (!keyword) {
            state.searchResults = [];
            renderResults();
            return;
        }
        searchTimer = setTimeout(() => searchTerms($searchType.val(), keyword), 300);
    });
    $searchInput.on("keydown", (e) => {
        if (e.key === "Enter") e.preventDefault(); // 다이얼로그 안 폼 제출 방지
    });

    function searchTerms(type, keyword, then) {
        const mySeq = ++state.searchSeq;
        $.ajax({
            url: `/api/selectColumn/term/${encodeURIComponent(type)}/${encodeURIComponent(keyword)}`,
            type: "GET",
            success: (response) => {
                if (mySeq !== state.searchSeq) return;
                state.searchResults = response.termDtos || [];
                renderResults();
                if (typeof then === "function") then(state.searchResults);
            }
        });
    }

    function usedColumnNames() {
        return new Set(state.rows.filter(r => !r.deleted).map(r => r.columnName));
    }

    function renderResults() {
        $results.empty();
        const used = usedColumnNames();

        if (!$searchInput.val().trim()) {
            $results.append($("<p></p>").addClass("form-help").text("표준용어명이나 영문약어명을 입력하면 바로 검색합니다. 여러 개를 체크해 한 번에 추가할 수 있습니다."));
        } else if (state.searchResults.length === 0) {
            $results.append($("<p></p>").addClass("table-name-error").text("조건에 맞는 표준용어가 없습니다. [+ 새 표준용어 등록]으로 바로 등록할 수 있습니다."));
        }

        state.searchResults.forEach(term => {
            const already = used.has(term.commonStandardTermAbbreviation);
            const $check = $("<input>").attr({type: "checkbox"}).addClass("form-check-input")
                .val(term.id).prop("disabled", already);
            const $label = $("<label></label>").addClass("col-bulk-result").toggleClass("is-disabled", already).append(
                $check,
                $("<span></span>").addClass("gh-mono").text(term.commonStandardTermAbbreviation),
                $("<span></span>").text(String(term.commonStandardTermName ?? "").replaceAll("_", " ")),
                $("<span></span>").addClass("gh-muted").text(term.dataType ?? "-")
            );
            if (term.isApprovalYn !== "Y") $label.append($("<span></span>").addClass("gh-label gh-label-attention").text("용어 미승인"));
            if (already) $label.append($("<span></span>").addClass("gh-muted").text("이미 추가됨"));
            $results.append($label);
        });

        const selectable = $results.find("input:checkbox:not(:disabled)").length;
        $checkAllBtn.prop("disabled", selectable === 0);
        updateAddButton();
    }

    $results.on("change", "input:checkbox", updateAddButton);

    function updateAddButton() {
        const n = $results.find("input:checkbox:checked").length;
        $addBtn.prop("disabled", n === 0).text(n > 0 ? `선택 추가 (${n})` : "선택 추가");
    }

    $checkAllBtn.on("click", () => {
        const $boxes = $results.find("input:checkbox:not(:disabled)");
        const allChecked = $boxes.length > 0 && $boxes.filter(":checked").length === $boxes.length;
        $boxes.prop("checked", !allChecked);
        updateAddButton();
    });

    $addBtn.on("click", () => {
        const ids = $results.find("input:checkbox:checked").map((_, el) => $(el).val()).get();
        const terms = state.searchResults.filter(t => ids.includes(String(t.id)));
        const added = addTerms(terms);
        renderResults();
        if (added > 0) focusLastNew();
    });

    /**
     * 새 행을 표 끝에 넣는다. 이미 있는 컬럼명은 건너뛴다.
     * @param items [{row, term}] : term 이 있으면(용어로 만든 행) 자동 추천을 적용한다. 공통 컬럼/복사 행은 term 없이 넘겨 값 그대로 쓴다.
     * @returns 추가한 행 수
     */
    function pushRows(items) {
        const used = usedColumnNames();
        let added = 0;
        items.forEach(({row, term}) => {
            if (!row.columnName || used.has(row.columnName)) return;
            used.add(row.columnName);

            if (term && state.recommend) {
                const remaining = state.rows.filter(r => !r.deleted);
                row.recommended = applyRecommendation(term, row.fields, {
                    isFirst: remaining.length === 0,
                    hasPk: remaining.some(r => r.fields.isPk === "Y")
                });
            }
            state.rows.push(row);
            added++;
        });
        renderRows();
        // 각 추가 목록의 "이미 추가됨" 표시 갱신
        renderPasteResults();
        renderCopyResults();
        return added;
    }

    function addTerms(terms) {
        return pushRows(terms.map(term => ({row: fromTerm(term, `new-${++seq}`), term})));
    }

    // ---------------------------------------------------------------- 공통 컬럼 세트
    let commonEditable = false;
    $.ajax({
        url: "/api/commonColumn",
        type: "GET",
        success: (response) => {
            commonEditable = response.editable === true;
            $commonManageBtn.attr("hidden", !commonEditable);
        }
    });

    $commonAddBtn.on("click", () => {
        $.ajax({
            url: "/api/commonColumn",
            type: "GET",
            success: (response) => {
                const items = response.items || [];
                if (items.length === 0) {
                    window.openAlert(commonEditable
                        ? "공통 컬럼 세트가 비어 있습니다. [공통 컬럼 세트 관리]에서 먼저 정해주세요."
                        : "공통 컬럼 세트가 비어 있습니다. 프로젝트 관리자에게 세트 등록을 요청해주세요.");
                    return;
                }

                const missing = items.filter(item => !item.term).length;
                const used = usedColumnNames();
                const skipped = items.filter(item => item.term && used.has(item.term.commonStandardTermAbbreviation)).length;
                const added = pushRows(items.filter(item => item.term).map(item => ({row: fromCommonColumn(item, `new-${++seq}`)})));

                const notes = [`공통 컬럼 ${added}개를 추가했습니다.`];
                if (skipped) notes.push(`이미 있는 ${skipped}개는 건너뛰었습니다.`);
                if (missing) notes.push(`표준용어가 지워진 ${missing}개는 추가하지 못했습니다.`);
                if (added > 0) focusLastNew();
                window.openAlert(notes.join(" "));
            }
        });
    });

    $commonManageBtn.on("click", () => openCommonColumnSet());

    // ---------------------------------------------------------------- 다른 테이블에서 복사
    const copyState = {tables: [], columns: [], tableSeq: 0, table: null};
    let copyTimer = null;

    $copyTableInput.on("input", () => {
        clearTimeout(copyTimer);
        const keyword = $copyTableInput.val().trim();
        if (!keyword) {
            $copyTableSelect.attr("hidden", true).empty();
            return;
        }
        copyTimer = setTimeout(() => lookupCopyTables(keyword), 300);
    }).on("keydown", (e) => {
        if (e.key === "Enter") e.preventDefault();
    });

    function lookupCopyTables(keyword) {
        const mySeq = ++copyState.tableSeq;
        $.ajax({
            url: `/api/selectColumn/table/${encodeURIComponent(keyword)}`,
            type: "GET",
            success: (response) => {
                if (mySeq !== copyState.tableSeq) return;
                // 지금 편집 중인 테이블은 빼고, 승인된 테이블만 (조회 API 기준)
                copyState.tables = (response.tableInfos || []).filter(t => String(t.id) !== String(table.id));
                $copyTableSelect.empty().attr("hidden", copyState.tables.length === 0);
                copyState.tables.forEach(t => $copyTableSelect.append($("<option></option>").val(t.id)
                    .text(t.tableKorName ? `${t.tableName} · ${t.tableKorName}` : t.tableName)));
                if (copyState.tables.length === 0) {
                    copyState.table = null;
                    copyState.columns = [];
                    renderCopyResults("조건에 맞는 다른 승인 테이블이 없습니다.");
                } else if (copyState.tables.length === 1) {
                    $copyTableSelect.val(String(copyState.tables[0].id)).trigger("change");
                }
            }
        });
    }

    $copyTableSelect.on("change", function () {
        const source = copyState.tables.find(t => String(t.id) === $(this).val());
        if (!source) return;
        copyState.table = source;
        $.ajax({
            url: `/api/selectColumn/tableColumn/${encodeURIComponent(source.id)}`,
            type: "GET",
            success: (response) => {
                if (copyState.table !== source) return; // 그 사이 다른 테이블 선택
                copyState.columns = (response.columnInfos || []).slice()
                    .sort((a, b) => (a.sortOrder ?? Number.MAX_SAFE_INTEGER) - (b.sortOrder ?? Number.MAX_SAFE_INTEGER));
                renderCopyResults();
            }
        });
    });

    function renderCopyResults(message) {
        $copyResults.empty();
        if (message) {
            $copyResults.append($("<p></p>").addClass("table-name-error").text(message));
        }
        if (!copyState.table) {
            $copyCheckAllBtn.prop("disabled", true);
            updateCopyAddButton();
            return;
        }
        if (copyState.columns.length === 0) {
            $copyResults.append($("<p></p>").addClass("form-help").text(`${copyState.table.tableName} 에 등록된 컬럼이 없습니다.`));
        }

        const used = usedColumnNames();
        copyState.columns.forEach(col => {
            const noTerm = !col.standardTerms?.id;
            const already = used.has(col.columnName);
            const disabled = noTerm || already;
            const $label = $("<label></label>").addClass("col-bulk-result").toggleClass("is-disabled", disabled).append(
                $("<input>").attr("type", "checkbox").addClass("form-check-input").val(col.id).prop({disabled, checked: false}),
                $("<span></span>").addClass("gh-mono").text(col.columnName),
                $("<span></span>").text(col.columnDesc ?? ""),
                $("<span></span>").addClass("gh-muted").text(`${col.dataType ?? "-"}${col.maxLength ? `(${col.maxLength})` : ""}`)
            );
            if (col.isPk === "Y") $label.append($("<span></span>").addClass("gh-label gh-label-accent").text("PK"));
            if (noTerm) $label.append($("<span></span>").addClass("gh-muted").text("표준용어 연결 없음"));
            if (already) $label.append($("<span></span>").addClass("gh-muted").text("이미 추가됨"));
            $copyResults.append($label);
        });

        $copyCheckAllBtn.prop("disabled", $copyResults.find("input:checkbox:not(:disabled)").length === 0);
        updateCopyAddButton();
    }

    $copyResults.on("change", "input:checkbox", updateCopyAddButton);

    function updateCopyAddButton() {
        const n = $copyResults.find("input:checkbox:checked").length;
        $copyAddBtn.prop("disabled", n === 0).text(n > 0 ? `체크한 컬럼 복사 (${n})` : "체크한 컬럼 복사");
    }

    $copyCheckAllBtn.on("click", () => {
        const $boxes = $copyResults.find("input:checkbox:not(:disabled)");
        const allChecked = $boxes.length > 0 && $boxes.filter(":checked").length === $boxes.length;
        $boxes.prop("checked", !allChecked);
        updateCopyAddButton();
    });

    $copyAddBtn.on("click", () => {
        const ids = $copyResults.find("input:checkbox:checked").map((_, el) => $(el).val()).get();
        const columns = copyState.columns.filter(col => ids.includes(String(col.id)));
        const hasPk = state.rows.some(r => !r.deleted && r.fields.isPk === "Y");
        const items = columns.map(col => ({row: fromCopiedColumn(col, `new-${++seq}`, copyState.table.tableName, hasPk)}));
        const added = pushRows(items);
        renderResults();
        if (added > 0) focusLastNew();
    });

    // ---------------------------------------------------------------- 붙여넣기로 추가
    /**
     * 붙여넣은 글을 줄 단위로 나눈다. 엑셀에서 여러 칸을 복사하면 첫 칸만, 한 줄에 쉼표로 나열했으면 쉼표로 나눈다.
     * 앞의 번호/글머리표(1. 2) - * •)는 지운다.
     */
    function parsePaste(text) {
        let lines = String(text ?? "").split(/\r?\n/).map(line => line.split("\t")[0]);
        const nonEmpty = lines.filter(line => line.trim());
        if (nonEmpty.length === 1 && nonEmpty[0].includes(",")) lines = nonEmpty[0].split(",");
        return lines
            .map(line => line.replace(/^\s*(?:\d+\s*[.)]|[-*•·])\s*/, "").trim())
            .filter(Boolean);
    }

    $pasteFindBtn.on("click", () => {
        const inputs = parsePaste($pasteText.val());
        if (inputs.length === 0) {
            window.openAlert("붙여넣은 내용이 없습니다.");
            return;
        }
        if (inputs.length > 300) {
            window.openAlert("한 번에 300줄까지 찾을 수 있습니다. 나눠서 붙여넣어 주세요.");
            return;
        }
        resolveInputs(inputs, (items) => {
            state.pasteItems = items.map(toPasteItem);
            renderPasteResults();
        });
    });

    function resolveInputs(inputs, then) {
        $pasteFindBtn.prop("disabled", true);
        $.ajax({
            url: "/api/selectColumn/term/resolve",
            type: "POST",
            data: JSON.stringify(inputs),
            success: (response) => then(response.items || []),
            complete: () => $pasteFindBtn.prop("disabled", false)
        });
    }

    function toPasteItem(item) {
        return {
            input: item.input,
            status: item.status,
            terms: item.terms || [],
            selectedId: item.terms?.[0]?.id ?? null, // 여러 건이면 승인된 용어가 앞에 온다
            checked: item.status !== "NOT_FOUND"
        };
    }

    function selectedTerm(item) {
        return item.terms.find(t => t.id === item.selectedId) ?? null;
    }

    function renderPasteResults() {
        $pasteResults.empty();
        if (state.pasteItems.length === 0) {
            $pasteAddBtn.prop("disabled", true).text("체크한 용어 추가");
            return;
        }

        const used = usedColumnNames();
        const seen = new Set(); // 붙여넣기 안에서 같은 컬럼명이 두 번 나오는지
        const counts = {MATCHED: 0, AMBIGUOUS: 0, NOT_FOUND: 0};

        const $list = $("<ul></ul>").addClass("col-bulk-paste-list");
        state.pasteItems.forEach((item, index) => {
            counts[item.status] = (counts[item.status] ?? 0) + 1;
            const term = selectedTerm(item);
            const abbr = term?.commonStandardTermAbbreviation;
            const already = !!abbr && used.has(abbr);
            const duplicated = !!abbr && !already && seen.has(abbr);
            if (abbr) seen.add(abbr);
            const blocked = !term || already || duplicated;
            if (blocked) item.checked = false;

            const $li = $("<li></li>").addClass(`col-bulk-paste-item is-${item.status.toLowerCase()}`).toggleClass("is-disabled", blocked);
            const $check = $("<input>").attr({type: "checkbox", "aria-label": `${item.input} 추가`}).addClass("form-check-input")
                .prop({checked: item.checked, disabled: blocked})
                .on("change", function () {
                    item.checked = this.checked;
                    updatePasteAddButton();
                });

            $li.append($check, $("<span></span>").addClass("col-bulk-paste-input").text(item.input), $("<span></span>").addClass("gh-muted").text("→"));

            if (item.status === "NOT_FOUND") {
                $li.append(
                    $("<span></span>").addClass("gh-label gh-label-danger").text("용어 없음"),
                    $("<button></button>").attr("type", "button").addClass("btn btn-sm").text("등록")
                        .on("click", () => createTermForPaste(index))
                );
            } else {
                if (item.status === "AMBIGUOUS") {
                    const $select = $("<select></select>").addClass("form-control col-bulk-paste-select").attr("aria-label", `${item.input} 용어 선택`)
                        .on("change", function () {
                            item.selectedId = Number($(this).val());
                            item.checked = true;
                            renderPasteResults();
                        });
                    item.terms.forEach(t => $select.append($("<option></option>").val(t.id)
                        .text(`${t.commonStandardTermAbbreviation} · ${t.commonStandardDomainName ?? "-"} · ${t.dataType ?? "-"}${t.isApprovalYn === "Y" ? "" : " (미승인)"}`)));
                    $select.val(String(item.selectedId));
                    $li.append($select, $("<span></span>").addClass("gh-label gh-label-attention").text(`${item.terms.length}건 중 선택`));
                } else {
                    $li.append(
                        $("<span></span>").addClass("gh-mono").text(abbr),
                        $("<span></span>").addClass("gh-muted").text(term.dataType ?? "-")
                    );
                    if (term.isApprovalYn !== "Y") $li.append($("<span></span>").addClass("gh-label gh-label-attention").text("용어 미승인"));
                }
                if (already) $li.append($("<span></span>").addClass("gh-muted").text("이미 추가됨"));
                if (duplicated) $li.append($("<span></span>").addClass("gh-muted").text("위에 같은 컬럼 있음"));
            }
            $list.append($li);
        });

        $pasteResults.append(
            $("<p></p>").addClass("form-help").text(
                `${state.pasteItems.length}줄 · 찾음 ${counts.MATCHED} · 여러 건 ${counts.AMBIGUOUS} · 없음 ${counts.NOT_FOUND}` +
                (counts.NOT_FOUND ? " — 없는 용어는 [등록]으로 바로 만들 수 있습니다." : "")),
            $list
        );
        updatePasteAddButton();
    }

    function updatePasteAddButton() {
        const n = state.pasteItems.filter(item => item.checked).length;
        $pasteAddBtn.prop("disabled", n === 0).text(n > 0 ? `체크한 용어 추가 (${n})` : "체크한 용어 추가");
    }

    $pasteAddBtn.on("click", () => {
        // 붙여넣은 줄 순서대로 추가한다
        const terms = state.pasteItems.filter(item => item.checked).map(selectedTerm).filter(Boolean);
        const added = addTerms(terms);
        renderResults();
        if (added > 0) {
            // 추가가 끝나면 붙여넣기 영역을 비워 표가 보이게 한다 (못 찾은 줄이 남아 있으면 그대로 둔다)
            if (!state.pasteItems.some(item => item.status === "NOT_FOUND")) {
                $pasteText.val("");
                state.pasteItems = [];
                renderPasteResults();
            }
            focusLastNew();
        }
    });

    /**
     * 붙여넣기에서 찾지 못한 줄을 새 표준용어로 등록하고, 그 줄을 다시 찾는다.
     */
    function createTermForPaste(index) {
        const item = state.pasteItems[index];
        openTermCreate(item.input, (saved) => {
            resolveInputs([item.input], (items) => {
                let next = items[0] ? toPasteItem(items[0]) : null;
                // 입력한 글이 용어명과 다르면(예: 영문약어로 붙여넣음) 등록한 약어로 다시 찾는다
                if (!next || next.status === "NOT_FOUND") {
                    resolveInputs([saved.commonStandardTermAbbreviation], (byAbbr) => {
                        const found = byAbbr[0] ? toPasteItem(byAbbr[0]) : null;
                        if (found) state.pasteItems[index] = {...found, input: item.input};
                        renderPasteResults();
                    });
                    return;
                }
                state.pasteItems[index] = next;
                renderPasteResults();
            });
        });
    }

    function focusLastNew() {
        const $wrap = $root.find(".col-bulk-table-wrap");
        $wrap.scrollTop($wrap[0].scrollHeight);
        $wrap[0].scrollIntoView({block: "nearest"}); // 화면이 낮으면 다이얼로그 안에서 표가 보이도록
    }

    // ---------------------------------------------------------------- 새 표준용어 등록 (팝업 다이얼로그)
    /**
     * 새 표준용어 등록 팝업 (이 다이얼로그 위의 #popupDialog). 승인/미승인은 기존 용어 등록처럼 서버에서 권한으로 정한다.
     * @param initialName 표준용어명 초기값
     * @param onSaved     (saved) => void : 등록 성공 후 (팝업은 닫힌 상태)
     */
    function openTermCreate(initialName, onSaved) {
        openPopupDialog({
            url: "/popup/standardTermCreate",
            title: "새 표준용어 등록",
            onLoad: ($popup) => {
                const form = createTermForm({
                    initialTermName: initialName,
                    submitText: "등록",
                    onSaved: (saved) => {
                        closePopupDialog();
                        onSaved(saved);
                    }
                });
                $popup.find("[data-term-form]").append(form);
                form.find(".term-word-input").trigger("focus");
            }
        });
    }

    $createTermBtn.on("click", () => {
        const keyword = $searchType.val() === "termName" ? $searchInput.val().trim() : "";

        openTermCreate(keyword, (saved) => {
            // 등록한 용어를 영문약어명으로 다시 찾아 바로 컬럼으로 추가한다
            const abbreviation = saved.commonStandardTermAbbreviation;
            $modeSearch.trigger("click");
            $searchType.val("termAbbreviation");
            $searchInput.val(abbreviation);
            searchTerms("termAbbreviation", abbreviation, (terms) => {
                const term = terms.find(t => t.commonStandardTermAbbreviation === abbreviation && t.commonStandardTermName === saved.commonStandardTermName)
                    ?? terms.find(t => t.commonStandardTermAbbreviation === abbreviation);
                if (term && addTerms([term]) > 0) {
                    renderResults();
                    focusLastNew();
                    window.openAlert(`표준용어 ${abbreviation} 이(가) ${term.isApprovalYn === "Y" ? "승인" : "승인 대기"} 상태로 등록되어 컬럼으로 추가되었습니다.`);
                }
            });
        });
    });

    // ---------------------------------------------------------------- 컬럼 표
    function renderRows() {
        $tbody.empty();

        if (state.rows.length === 0) {
            $tbody.append($("<tr></tr>").append($("<td></td>").attr("colspan", 11).addClass("col-bulk-empty")
                .text("등록된 컬럼이 없습니다. 위에서 표준용어를 검색해 추가하세요.")));
        }

        let visibleIndex = 0;
        state.rows.forEach((row, index) => {
            $tbody.append(rowElement(row, index, row.deleted ? null : ++visibleIndex));
            if (row.expanded && !row.deleted) $tbody.append(detailElement(row));
            if (row.error) {
                $tbody.append($("<tr></tr>").addClass("col-bulk-error-row").append(
                    $("<td></td>").attr("colspan", 11).append($("<p></p>").addClass("table-name-error").text(row.error))));
            }
        });

        updateSummary();
    }

    function rowElement(row, index, number) {
        const locked = row.approved;
        const $tr = $("<tr></tr>").attr("data-key", row.key)
            .toggleClass("is-locked", locked)
            .toggleClass("is-new", row.isNew)
            .toggleClass("is-deleted", !!row.deleted)
            .toggleClass("has-error", !!row.error);

        // 순번
        $tr.append($("<td></td>").addClass("col-num-cell").text(number ?? "-"));

        // 이동
        const $move = $("<td></td>").addClass("col-move");
        if (!row.deleted) {
            $move.append(
                iconButton(ICON_UP, "위로", !canMove(index, -1), () => move(index, -1)),
                iconButton(ICON_DOWN, "아래로", !canMove(index, 1), () => move(index, 1))
            );
        }
        $tr.append($move);

        // 컬럼명 + 상태
        const $name = $("<td></td>").addClass("col-name").append($("<span></span>").addClass("gh-mono").text(row.columnName));
        if (row.isNew) $name.append($("<span></span>").addClass("gh-label gh-label-accent").text("신규"));
        else if (locked) $name.append($("<span></span>").addClass("gh-label gh-label-success").text("승인"));
        else if (row.deleted) $name.append($("<span></span>").addClass("gh-label gh-label-danger").text("삭제 예정"));
        else if (isModified(row)) $name.append($("<span></span>").addClass("gh-label gh-label-attention").text("수정"));
        if (row.termNotApproved) $name.append($("<span></span>").addClass("gh-label gh-label-attention").text("용어 미승인"));
        if (row.source && !row.deleted) {
            $name.append($("<span></span>").addClass("gh-label col-bulk-source").attr("title", row.source.title).text(row.source.label));
        }
        if (row.recommended?.length && !row.deleted) {
            $name.append($("<span></span>").addClass("gh-label gh-label-done col-bulk-recommended")
                .attr("title", `자동 추천 적용 : ${row.recommended.join(" / ")} (표에서 바로 바꿀 수 있습니다)`)
                .text("추천"));
        }
        $tr.append($name);

        const disabled = locked || !!row.deleted;
        $tr.append(
            $("<td></td>").append(textInput(row, "columnDesc", disabled)),
            $("<td></td>").append(textInput(row, "dataType", disabled).attr("list", "colBulkDataTypes").addClass("gh-mono")),
            $("<td></td>").addClass("col-num").append(numberInput(row, "maxLength", disabled)),
            $("<td></td>").addClass("col-chk").append(checkInput(row, "isPk", disabled, "PK")),
            $("<td></td>").addClass("col-chk").append(checkInput(row, "isNullable", disabled || row.fields.isPk === "Y", "NULL 허용")),
            $("<td></td>").append(textInput(row, "defaultValue", disabled).addClass("gh-mono"))
        );

        // 상세 펼치기
        const $detail = $("<td></td>").addClass("col-icon");
        if (!row.deleted) {
            $detail.append($("<button></button>").attr({type: "button", title: row.expanded ? "상세 닫기" : "상세 (정밀도/참조 테이블/속성)", "aria-expanded": !!row.expanded})
                .addClass("btn btn-sm col-bulk-icon").text(row.expanded ? "▴" : "⋯")
                .on("click", () => {
                    row.expanded = !row.expanded;
                    renderRows();
                }));
        }
        $tr.append($detail);

        // 삭제 / 되돌리기
        const $remove = $("<td></td>").addClass("col-icon");
        if (row.deleted) {
            $remove.append($("<button></button>").attr({type: "button", title: "삭제 취소"}).addClass("btn btn-sm col-bulk-icon").text("↺")
                .on("click", () => {
                    if (usedColumnNames().has(row.columnName)) {
                        window.openAlert(`같은 컬럼명(${row.columnName})이 이미 추가되어 있어 되돌릴 수 없습니다.`);
                        return;
                    }
                    row.deleted = false;
                    row.error = null;
                    renderRows();
                    renderResults();
                }));
        } else if (!locked) {
            $remove.append($("<button></button>").attr({type: "button", title: "삭제"}).addClass("btn btn-sm col-bulk-icon col-bulk-remove").text("✕")
                .on("click", () => {
                    if (row.isNew) state.rows.splice(index, 1);
                    else row.deleted = true;
                    row.error = null;
                    renderRows();
                    renderResults();
                }));
        }
        $tr.append($remove);

        return $tr;
    }

    function detailElement(row) {
        const disabled = row.approved;
        const field = (label, $input) => $("<label></label>").addClass("col-bulk-field").append($("<span></span>").text(label), $input);

        const $flags = $("<div></div>").addClass("col-bulk-flags");
        DETAIL_FLAGS.forEach(([key, label]) => {
            // PK 는 필수/고유값으로 저장된다 (서버와 같은 규칙)
            const forced = row.fields.isPk === "Y" && (key === "isRequired" || key === "isUnique");
            const $box = checkInput(row, key, disabled || forced, label);
            if (forced) $box.prop("checked", true);
            $flags.append($("<label></label>").addClass("col-bulk-flag").append($box, $("<span></span>").text(label)));
        });

        return $("<tr></tr>").addClass("col-bulk-detail-row").append(
            $("<td></td>").attr("colspan", 2),
            $("<td></td>").attr("colspan", 9).append(
                $("<div></div>").addClass("col-bulk-detail").append(
                    field("정밀도", numberInput(row, "precision", disabled)),
                    field("소수 자릿수", numberInput(row, "scale", disabled)),
                    field("참조 테이블명", textInput(row, "refTableName", disabled).addClass("gh-mono").attr("placeholder", "예: TB_USER")),
                    field("데이터 예시", textInput(row, "example", disabled))
                ),
                $flags
            )
        );
    }

    function textInput(row, key, disabled) {
        return $("<input>").attr({type: "text", "aria-label": key}).addClass("form-control")
            .val(row.fields[key] ?? "")
            .prop("disabled", disabled)
            .on("input", function () {
                row.fields[key] = $(this).val();
                onFieldChanged(row);
            });
    }

    function numberInput(row, key, disabled) {
        return $("<input>").attr({type: "number", min: 0, "aria-label": key}).addClass("form-control")
            .val(row.fields[key] ?? "")
            .prop("disabled", disabled)
            .on("keydown", (e) => {
                if (["e", "E", "+", "-"].includes(e.key)) e.preventDefault();
            })
            .on("input", function () {
                row.fields[key] = $(this).val() === "" ? null : $(this).val();
                onFieldChanged(row);
            });
    }

    function checkInput(row, key, disabled, title) {
        return $("<input>").attr({type: "checkbox", title, "aria-label": title}).addClass("form-check-input")
            .prop("checked", row.fields[key] === "Y")
            .prop("disabled", disabled)
            .on("change", function () {
                row.fields[key] = this.checked ? "Y" : "N";
                if (key === "isPk" && this.checked) row.fields.isNullable = "N"; // PK 는 NULL 비허용
                row.error = null;
                renderRows(); // PK 에 따라 NULL/필수/고유값 잠금이 바뀐다
            });
    }

    function onFieldChanged(row) {
        if (row.error) {
            row.error = null;
            $tbody.find(`tr[data-key="${cssEscape(row.key)}"]`).removeClass("has-error").next(".col-bulk-error-row").remove();
        }
        // 입력 중에는 표를 다시 그리지 않는다(포커스 유지). 상태 라벨/요약만 갱신한다.
        const $name = $tbody.find(`tr[data-key="${cssEscape(row.key)}"] .col-name`);
        $name.find(".gh-label-attention:contains('수정')").remove();
        if (!row.isNew && !row.approved && isModified(row)) {
            $name.find(".gh-mono").after($("<span></span>").addClass("gh-label gh-label-attention").text("수정"));
        }
        updateSummary();
    }

    function iconButton(svg, title, disabled, onClick) {
        return $("<button></button>").attr({type: "button", title, "aria-label": title}).addClass("btn btn-sm col-bulk-icon")
            .html(svg) // 고정 SVG 아이콘
            .prop("disabled", disabled)
            .on("click", onClick);
    }

    // 승인 전 컬럼끼리만 자리를 바꾼다. 승인된 컬럼은 자리를 지키고 다른 컬럼이 넘어갈 수도 없다. (삭제 예정 행은 건너뛴다)
    function neighbor(index, dir) {
        let j = index + dir;
        while (j >= 0 && j < state.rows.length && state.rows[j].deleted) j += dir;
        return j >= 0 && j < state.rows.length ? j : -1;
    }

    function canMove(index, dir) {
        const row = state.rows[index];
        if (row.approved || row.deleted) return false;
        const j = neighbor(index, dir);
        return j >= 0 && !state.rows[j].approved;
    }

    function move(index, dir) {
        const j = neighbor(index, dir);
        if (j < 0) return;
        [state.rows[index], state.rows[j]] = [state.rows[j], state.rows[index]];
        renderRows();
    }

    // ---------------------------------------------------------------- 변경 요약 / 저장
    function orderChanged() {
        const current = state.rows.filter(r => !r.deleted && !r.isNew).map(r => r.id);
        const original = state.originalOrder.filter(id => current.includes(id));
        return current.some((id, i) => id !== original[i]);
    }

    function counts() {
        return {
            created: state.rows.filter(r => r.isNew).length,
            updated: state.rows.filter(r => !r.isNew && !r.deleted && !r.approved && isModified(r)).length,
            deleted: state.rows.filter(r => r.deleted).length,
            reordered: orderChanged()
        };
    }

    function isDirty() {
        const c = counts();
        return c.created + c.updated + c.deleted > 0 || c.reordered;
    }

    function updateSummary() {
        const c = counts();
        const parts = [`신규 ${c.created}`, `수정 ${c.updated}`, `삭제 ${c.deleted}`];
        if (c.reordered) parts.push("순서 변경");
        $summary.text(parts.join(" · "));
        $saveBtn.prop("disabled", state.saving || !isDirty());
    }

    $cancelBtn.on("click", () => {
        if (isDirty()) {
            window.openConfirm("저장하지 않은 변경 내용이 있습니다. 닫을까요?", () => window.closeDialog("div"));
        } else {
            window.closeDialog("div");
        }
    });

    $saveBtn.on("click", () => {
        // 화면에서 먼저 확인 : 필수값 (서버도 같은 기준으로 다시 확인한다)
        let firstError = null;
        state.rows.forEach(row => {
            row.error = null;
            if (row.deleted || row.approved) return;
            const missing = [];
            if (!String(row.fields.columnDesc ?? "").trim()) missing.push("컬럼 설명");
            if (!String(row.fields.dataType ?? "").trim()) missing.push("데이터 타입");
            if (missing.length) {
                row.error = `${row.columnName} : ${missing.join(", ")}을(를) 입력해주세요.`;
                firstError ??= row;
            }
        });
        showGeneralErrors([]);
        if (firstError) {
            renderRows();
            scrollToRow(firstError.key);
            return;
        }

        const c = counts();
        const message = `신규 ${c.created}건, 수정 ${c.updated}건, 삭제 ${c.deleted}건${c.reordered ? ", 순서 변경" : ""}을 저장하시겠습니까?`;
        window.openConfirm(message, save);
    });

    function save() {
        const payload = {
            tableInfoId: table.id,
            rows: state.rows.filter(r => !r.deleted).map(toPayloadRow),
            deletedIds: state.rows.filter(r => r.deleted).map(r => r.id)
        };

        state.saving = true;
        updateSummary();

        $.ajax({
            url: "/api/saveColumn/bulk",
            type: "PUT",
            data: JSON.stringify(payload),
            success: (response) => {
                if (!response.result) {
                    applyServerErrors(response.errors || []);
                    return;
                }
                window.closeDialog("div");
                window.openAlert(`저장되었습니다. (신규 ${response.created} · 수정 ${response.updated} · 삭제 ${response.deleted})`, () => {
                    if (typeof onSaved === "function") onSaved();
                });
            },
            complete: () => {
                state.saving = false;
                updateSummary();
            }
        });
    }

    function applyServerErrors(errors) {
        const general = [];
        errors.forEach(err => {
            const row = state.rows.find(r => r.key === err.key);
            if (row) row.error = err.message;
            else general.push(err.message);
        });
        renderRows();
        showGeneralErrors(general);
        const first = state.rows.find(r => r.error);
        if (first) scrollToRow(first.key);
        else if (general.length) $generalError[0].scrollIntoView({block: "nearest"});
    }

    function showGeneralErrors(messages) {
        $generalError.empty().attr("hidden", messages.length === 0);
        messages.forEach(m => $generalError.append($("<p></p>").addClass("table-name-error").text(m)));
    }

    function scrollToRow(key) {
        const el = $tbody.find(`tr[data-key="${cssEscape(key)}"]`)[0];
        if (el) el.scrollIntoView({block: "center"});
    }

    // ---------------------------------------------------------------- 열기
    renderResults();
    renderRows();

    window.openDialog("div", {title: `컬럼 편집 · ${table.tableName ?? ""}${table.tableKorName ? ` (${table.tableKorName})` : ""}`, content: $root});
    const dialog = document.getElementById("mainConfirm");
    dialog.classList.add("dialog-xl");
    // 같은 다이얼로그를 다른 화면도 쓰므로 닫힐 때 넓은 크기를 되돌린다.
    dialog.addEventListener("close", () => dialog.classList.remove("dialog-xl"), {once: true});
    $searchInput.trigger("focus");
}

// ---------------------------------------------------------------- 행 데이터 변환

function fromExisting(col) {
    const fields = {};
    FIELD_KEYS.forEach(key => fields[key] = normalize(key, col[key]));
    return {
        key: String(col.id),
        id: col.id,
        termId: col.standardTerms?.id ?? null,
        columnName: col.columnName,
        approved: col.isApproval === "Y",
        isNew: false,
        termNotApproved: false,
        fields,
        original: {...fields}
    };
}

/**
 * 표준용어로 새 컬럼 행을 만든다 : 설명은 용어명(공백 구분), 데이터 타입 "VARCHAR(100)" / "NUMBER(12,2)" 은 타입과 길이/정밀도로 나눈다.
 * NULL 허용은 기본으로 켜 둔다 (NOT NULL 은 PK 또는 직접 선택).
 */
function fromTerm(term, key) {
    const raw = String(term.dataType ?? "").trim();
    const match = raw.match(/^\s*([^(]+?)\s*(?:\(\s*(\d+)\s*(?:,\s*(\d+)\s*)?\))?\s*$/);
    const base = match ? match[1].toUpperCase() : raw.toUpperCase();
    const isNumeric = ["NUMBER", "NUMERIC", "DECIMAL"].includes(base);

    const fields = {};
    FIELD_KEYS.forEach(k => fields[k] = k.startsWith("is") ? "N" : null);
    Object.assign(fields, {
        columnDesc: String(term.commonStandardTermName ?? "").replaceAll("_", " "),
        dataType: base || null,
        isNullable: "Y"
    });
    if (match?.[2]) {
        if (isNumeric) {
            fields.precision = match[2];
            if (match[3]) fields.scale = match[3];
        } else {
            fields.maxLength = match[2];
        }
    }

    return {
        key,
        id: null,
        termId: term.id,
        columnName: term.commonStandardTermAbbreviation,
        approved: false,
        isNew: true,
        termNotApproved: term.isApprovalYn !== "Y",
        fields,
        original: null
    };
}

/**
 * 공통 컬럼 세트 항목으로 새 행을 만든다 : 용어로 기본값을 채운 뒤 세트의 NULL 허용/기본값/인덱스를 덮어쓴다. (자동 추천은 쓰지 않는다)
 */
function fromCommonColumn(item, key) {
    const row = fromTerm(item.term, key);
    row.fields.isNullable = item.isNullable === "Y" ? "Y" : "N";
    row.fields.isIndex = item.isIndex === "Y" ? "Y" : "N";
    if (item.defaultValue) row.fields.defaultValue = item.defaultValue;
    row.source = {label: "공통", title: "프로젝트 공통 컬럼 세트에서 추가"};
    return row;
}

/**
 * 다른 테이블의 컬럼을 복사해 새 행을 만든다 : 설명/타입/길이/속성을 그대로 가져온다.
 * 이 테이블에 이미 PK 가 있으면 복사한 PK 는 풀어 둔다. (의도하지 않은 복합 PK 방지)
 */
function fromCopiedColumn(col, key, sourceTableName, targetHasPk) {
    const fields = {};
    FIELD_KEYS.forEach(k => fields[k] = normalize(k, col[k]));

    const notes = [`${sourceTableName} 에서 복사`];
    if (targetHasPk && fields.isPk === "Y") {
        fields.isPk = "N";
        notes.push("이 테이블에 이미 PK 가 있어 PK 는 해제했습니다");
    }

    return {
        key,
        id: null,
        termId: col.standardTerms.id,
        columnName: col.columnName,
        approved: false,
        isNew: true,
        termNotApproved: col.standardTerms.isApprovalYn !== "Y",
        fields,
        original: null,
        source: {label: "복사", title: notes.join(" · ")}
    };
}

function toPayloadRow(row) {
    const payload = {key: row.key, id: row.id, termId: row.isNew ? row.termId : null};
    FIELD_KEYS.forEach(key => {
        const value = row.fields[key];
        if (NUMBER_KEYS.includes(key)) payload[key] = value === null || value === "" ? null : Number(value);
        else if (key.startsWith("is")) payload[key] = value === "Y" ? "Y" : "N";
        else payload[key] = value === null ? null : String(value);
    });
    return payload;
}

function normalize(key, value) {
    if (key.startsWith("is")) return value === "Y" ? "Y" : "N";
    if (value === null || value === undefined || value === "") return null;
    return String(value);
}

function isModified(row) {
    if (!row.original) return false;
    return FIELD_KEYS.some(key => String(row.fields[key] ?? "").trim() !== String(row.original[key] ?? "").trim());
}

function cssEscape(value) {
    return window.CSS?.escape ? CSS.escape(value) : String(value).replace(/"/g, '\\"');
}
