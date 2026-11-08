import {closePopupDialog, openPopupDialog} from "../../common/popup_dialog.js";

/**
 * 공통 컬럼 세트 관리 (컬럼 일괄 편집 > 공통 컬럼 세트 관리) - 프로젝트 관리자 / ADMIN / MANAGER
 *
 * 거의 모든 테이블에 들어가는 컬럼(등록일시, 등록자, 수정일시, 수정자, 사용여부 등)을 표준용어로 정해 둔다.
 * 컬럼명/설명/데이터 타입은 표준용어에서 오고, 여기서는 순서와 NULL 허용/기본값/인덱스만 정한다.
 * 저장은 PUT /api/commonColumn 으로 세트를 통째로 바꾼다. 모든 값은 val / textContent 로 넣는다.
 */

const MAX_ITEMS = 50;

export function openCommonColumnSet({onSaved} = {}) {
    openPopupDialog({
        url: "/popup/commonColumnSet",
        title: "공통 컬럼 세트 관리",
        size: "large",
        onLoad: ($root) => {
            $.ajax({
                url: "/api/commonColumn",
                type: "GET",
                success: (response) => init($root, response.items || [], onSaved)
            });
        }
    });
}

function init($root, loaded, onSaved) {
    const q = (selector) => $root.find(selector);
    const state = {
        items: loaded.map(item => ({
            termId: item.termId,
            term: item.term,
            isNullable: item.isNullable === "Y" ? "Y" : "N",
            isIndex: item.isIndex === "Y" ? "Y" : "N",
            defaultValue: item.defaultValue ?? ""
        })),
        results: [],
        seq: 0
    };

    // ---------------------------------------------------------------- 표준용어 검색
    let timer = null;
    q("[data-set-search-type], [data-set-search]").on("input change", () => {
        clearTimeout(timer);
        const keyword = q("[data-set-search]").val().trim();
        if (!keyword) {
            state.results = [];
            renderResults();
            return;
        }
        timer = setTimeout(() => search(q("[data-set-search-type]").val(), keyword), 300);
    });
    q("[data-set-search]").on("keydown", (e) => {
        if (e.key === "Enter") e.preventDefault();
    });

    function search(type, keyword) {
        const mySeq = ++state.seq;
        $.ajax({
            url: `/api/selectColumn/term/${encodeURIComponent(type)}/${encodeURIComponent(keyword)}`,
            type: "GET",
            success: (response) => {
                if (mySeq !== state.seq) return;
                state.results = response.termDtos || [];
                renderResults();
            }
        });
    }

    function usedAbbreviations() {
        return new Set(state.items.map(item => item.term?.commonStandardTermAbbreviation).filter(Boolean));
    }

    function renderResults() {
        const $box = q("[data-set-results]").empty();
        if (!q("[data-set-search]").val().trim()) return;
        if (state.results.length === 0) {
            $box.append($("<p></p>").addClass("table-name-error").text("조건에 맞는 표준용어가 없습니다. 데이터 사전 > 표준용어에서 먼저 등록해주세요."));
            return;
        }
        const used = usedAbbreviations();
        state.results.forEach(term => {
            const already = used.has(term.commonStandardTermAbbreviation);
            $box.append($("<button></button>").attr("type", "button").addClass("common-set-result").prop("disabled", already).append(
                $("<span></span>").addClass("gh-mono").text(term.commonStandardTermAbbreviation),
                $("<span></span>").text(String(term.commonStandardTermName ?? "").replaceAll("_", " ")),
                $("<span></span>").addClass("gh-muted").text(term.dataType ?? "-"),
                $("<span></span>").addClass("gh-muted").text(already ? "이미 있음" : "+ 추가")
            ).on("click", () => {
                if (state.items.length >= MAX_ITEMS) {
                    window.openAlert(`공통 컬럼은 ${MAX_ITEMS}개까지 등록할 수 있습니다.`);
                    return;
                }
                state.items.push(defaultsFor(term));
                renderItems();
                renderResults();
            }));
        });
    }

    /**
     * 새 항목 기본값 : 여부 → NOT NULL·기본값 N, 등록·생성 일시 → NOT NULL·CURRENT_TIMESTAMP(날짜/시간 타입), 그 외 NULL 허용
     */
    function defaultsFor(term) {
        const words = String(term.commonStandardTermName ?? "").split("_");
        const first = words[0];
        const last = words[words.length - 1];
        const type = String(term.dataType ?? "").toUpperCase();
        const item = {termId: term.id, term, isNullable: "Y", isIndex: "N", defaultValue: ""};

        if (last === "여부" || /_YN$/i.test(term.commonStandardTermAbbreviation ?? "")) {
            Object.assign(item, {isNullable: "N", defaultValue: "N"});
        } else if (["등록", "생성", "최초등록", "입력"].includes(first)) {
            item.isNullable = "N";
            if (["일시", "일자"].includes(last) && /^(DATE|DATETIME|TIMESTAMP)/.test(type)) item.defaultValue = "CURRENT_TIMESTAMP";
        }
        return item;
    }

    // ---------------------------------------------------------------- 세트 목록
    function renderItems() {
        const $tbody = q("[data-set-items]").empty();
        q("[data-set-count]").text(`${state.items.length}개`);

        if (state.items.length === 0) {
            $tbody.append($("<tr></tr>").append($("<td></td>").attr("colspan", 8).addClass("common-set-empty")
                .text("공통 컬럼이 없습니다. 위에서 표준용어를 검색해 추가하세요. (예: 등록 일시, 등록자 아이디, 수정 일시, 수정자 아이디, 사용 여부)")));
            return;
        }

        state.items.forEach((item, index) => {
            const missing = !item.term;
            const $tr = $("<tr></tr>").toggleClass("is-missing", missing);

            $tr.append(
                $("<td></td>").addClass("common-set-move").append(
                    moveButton("▲", "위로", index === 0, () => swap(index, index - 1)),
                    moveButton("▼", "아래로", index === state.items.length - 1, () => swap(index, index + 1))
                ),
                $("<td></td>").append(missing
                    ? $("<span></span>").addClass("gh-label gh-label-danger").text(`용어 없음 (ID ${item.termId})`)
                    : $("<span></span>").addClass("gh-mono").text(item.term.commonStandardTermAbbreviation)),
                $("<td></td>").text(missing ? "-" : String(item.term.commonStandardTermName ?? "").replaceAll("_", " ")),
                $("<td></td>").addClass("gh-mono").text(missing ? "-" : (item.term.dataType ?? "-")),
                $("<td></td>").addClass("text-center").append(checkbox(item, "isNullable", "NULL 허용", missing)),
                $("<td></td>").append($("<input>").attr({type: "text", "aria-label": "기본값", placeholder: "예: N / CURRENT_TIMESTAMP"})
                    .addClass("form-control gh-mono").val(item.defaultValue).prop("disabled", missing)
                    .on("input", function () {
                        item.defaultValue = $(this).val();
                    })),
                $("<td></td>").addClass("text-center").append(checkbox(item, "isIndex", "인덱스", missing)),
                $("<td></td>").addClass("text-center").append($("<button></button>").attr({type: "button", title: "삭제", "aria-label": "삭제"})
                    .addClass("btn btn-sm").text("✕")
                    .on("click", () => {
                        state.items.splice(index, 1);
                        renderItems();
                        renderResults();
                    }))
            );
            $tbody.append($tr);
        });
    }

    function checkbox(item, key, title, disabled) {
        return $("<input>").attr({type: "checkbox", title, "aria-label": title}).addClass("form-check-input")
            .prop({checked: item[key] === "Y", disabled})
            .on("change", function () {
                item[key] = this.checked ? "Y" : "N";
            });
    }

    function moveButton(text, title, disabled, onClick) {
        return $("<button></button>").attr({type: "button", title, "aria-label": title}).addClass("btn btn-sm").text(text)
            .prop("disabled", disabled).on("click", onClick);
    }

    function swap(i, j) {
        [state.items[i], state.items[j]] = [state.items[j], state.items[i]];
        renderItems();
    }

    // ---------------------------------------------------------------- 저장
    q("[data-set-save]").on("click", () => {
        const missing = state.items.filter(item => !item.term).length;
        const message = missing
            ? `표준용어가 지워진 ${missing}개는 빼고 저장합니다. 저장하시겠습니까?`
            : "공통 컬럼 세트를 저장하시겠습니까?";

        window.openConfirm(message, () => {
            const payload = state.items.filter(item => item.term).map(item => ({
                termId: item.termId,
                isNullable: item.isNullable,
                isIndex: item.isIndex,
                defaultValue: String(item.defaultValue ?? "").trim() || null
            }));

            $.ajax({
                url: "/api/commonColumn",
                type: "PUT",
                data: JSON.stringify(payload),
                success: (response) => {
                    if (!response.result) return;
                    closePopupDialog();
                    window.openAlert("공통 컬럼 세트를 저장했습니다.", () => {
                        if (typeof onSaved === "function") onSaved();
                    });
                }
            });
        });
    });

    renderItems();
    q("[data-set-search]").trigger("focus");
}
