/**
 * 표준용어 등록 폼 (데이터 사전 > 표준용어 추가, 컬럼 관리 > 컬럼 등록 > 새 표준용어 등록 에서 함께 사용)
 *
 * - 표준용어명을 입력하면 표준 단어를 바로 조회해 영문약어명/이음동의어를 채우고, 마지막 단어의 도메인 분류로 도메인을 고른다.
 * - 저장은 기존 등록 API(/api/insertDataDictionary/standardTerms)를 그대로 사용한다.
 *   승인/미승인 여부는 서버에서 권한(AuthUtil.isApprovalAvailable)에 따라 정한다.
 * - 폼 안의 요소만 찾으므로(메인 다이얼로그와 팝업 다이얼로그에 동시에 떠도) 다른 화면과 충돌하지 않는다.
 * - 모든 값은 textContent / val 로 넣어 HTML 이 해석되지 않도록 한다.
 */

const FIELDS = [
    {key: "revisionNumber", label: "제정차수", type: "number"},
    {key: "commonStandardTermName", label: "표준용어명", type: "termWord"},
    {key: "commonStandardTermDescription", label: "표준용어설명", type: "textarea", required: true},
    {key: "commonStandardTermAbbreviation", label: "표준용어영문약어명", readonly: true, mono: true},
    {key: "commonStandardDomainName", label: "표준도메인명", readonly: true, required: true},
    {key: "allowedValues", label: "허용값", type: "textarea", readonly: true, required: true},
    {key: "storageFormat", label: "저장 형식", readonly: true, required: true},
    {key: "displayFormat", label: "표현 형식", readonly: true, required: true},
    {key: "administrativeStandardCodeName", label: "행정표준코드명"},
    {key: "responsibleOrganization", label: "소관기관명"},
    {key: "synonyms", label: "용어 이음동의어 목록", readonly: true}
];

// 표준용어명이 바뀌면 다시 채워야 하는 항목 (조회/도메인 선택으로 자동 입력)
const AUTO_FIELDS = ["commonStandardTermAbbreviation", "commonStandardDomainName", "allowedValues", "storageFormat", "displayFormat", "synonyms"];

const WORD_STATUS = {
    APPROVED: {variant: "success"},
    NOT_APPROVED: {variant: "attention", text: "미승인"},
    NOT_FOUND: {variant: "danger", text: "미등록"},
    NOT_DOMAIN: {variant: "danger", text: "도메인 분류 없음"}
};

/**
 * @param initialTermName 표준용어명 초기값 (예: 컬럼 등록에서 검색하던 용어명)
 * @param submitText      저장 버튼 문구
 * @param onSaved         (savedTerm) => void : 저장 성공 시 호출. savedTerm 은 보낸 값(영문약어명/용어명/도메인명 등)
 * @returns jQuery form
 */
export function createTermForm({initialTermName = "", submitText = "추가", onSaved} = {}) {
    const $form = $("<form></form>").addClass("edit-form term-create-form").attr("novalidate", true);
    const q = (selector) => $form.find(selector);
    const field = (key) => $form.find(`[name='${key}']`);

    const state = {
        termName: "",    // 마지막으로 조회한 용어명 ("_" 구분)
        valid: false,    // 모든 단어가 등록되어 있고 마지막 단어가 도메인 분류 단어인지
        pending: false,  // 조회 중 여부
        requestSeq: 0,   // 늦게 도착한 이전 응답 무시용
        domains: [],     // 마지막 단어의 도메인 분류에 속한 도메인 목록
        duplicate: {checking: false, duplicated: false, seq: 0}, // 영문약어명 + 도메인 중복 확인 결과
        saving: false
    };

    FIELDS.forEach(f => $form.append(f.type === "termWord" ? createTermWordGroup(f) : createFieldGroup(f)));

    const $submit = $("<button></button>").attr("type", "submit").addClass("btn btn-primary").text(submitText);
    $form.append($submit);
    $form.on("submit", (e) => {
        e.preventDefault();
        save();
    });

    if (initialTermName) {
        q(".term-word-input").val(initialTermName);
        onTermWordInput(); // 바로 조회
    }

    return $form;

    // ---------------------------------------------------------------- 화면 구성

    function createFieldGroup(f) {
        const id = `termForm-${f.key}`;
        const $input = f.type === "textarea"
            ? $("<textarea></textarea>").attr("rows", 2)
            : $("<input>").attr("type", f.type === "number" ? "number" : "text");

        $input.attr({id, name: f.key}).addClass("form-control").toggleClass("gh-mono", !!f.mono);
        if (f.readonly) $input.prop("readonly", true).attr("placeholder", "표준용어명/도메인을 선택하면 자동으로 입력됩니다.");
        if (f.type === "number") {
            $input.attr("min", 0).on("keydown", (event) => {
                if (["e", "E", "+", "-"].includes(event.key)) event.preventDefault();
            });
        }

        return $("<div></div>").addClass("form-group").append(
            $("<label></label>").attr("for", id).text(f.label),
            $input
        );
    }

    function createTermWordGroup(f) {
        let timer = null;

        const $input = $("<input>")
            .attr({type: "text", id: "termForm-termWordInput", autocomplete: "off", placeholder: "예) 사용자_이름 또는 사용자 이름"})
            .addClass("form-control term-word-input")
            .on("input", () => {
                clearTimeout(timer);
                timer = setTimeout(onTermWordInput, 300);
                clearResult();
                state.pending = splitWords($input.val()).length > 0;
            });

        // 서버로 보내는 표준용어명 (단어를 "_" 로 이은 값)
        const $hidden = $("<input>").attr({type: "hidden", name: f.key});

        const $domainSelect = $("<select></select>")
            .attr({id: "termForm-domainSelect"})
            .addClass("form-control term-domain-select")
            .prop("disabled", true)
            .append($("<option></option>").val("").text("표준용어명을 먼저 입력하세요"))
            .on("change", function () {
                fillDomainFields(state.domains.find(d => d.commonStandardDomainName === $(this).val()));
            });

        return $("<div></div>").addClass("form-group term-word-group").append(
            $("<label></label>").attr("for", "termForm-termWordInput").text(f.label),
            $input,
            $("<p></p>").addClass("form-help")
                .text("표준 단어를 _ 또는 공백으로 구분해 입력하면 바로 조회합니다. 마지막 단어는 도메인 분류가 있는 단어(예: 명, 일자)여야 합니다."),
            $("<div></div>").attr("aria-live", "polite").addClass("table-name-words term-words"),
            $hidden,
            $("<label></label>").attr("for", "termForm-domainSelect").text("도메인 선택").css("margin-top", "12px"),
            $domainSelect,
            $("<div></div>").attr("aria-live", "polite").addClass("table-name-words term-duplicate")
        );
    }

    // ---------------------------------------------------------------- 단어 조회

    function onTermWordInput() {
        const words = splitWords(q(".term-word-input").val());
        clearResult();
        if (words.length === 0) return;
        lookupWords(words);
    }

    function clearResult() {
        Object.assign(state, {termName: "", valid: false, pending: false, domains: []});
        resetDuplicate();
        q(".term-words").empty();
        field("commonStandardTermName").val("");
        AUTO_FIELDS.forEach(key => field(key).val(""));
        q(".term-domain-select").prop("disabled", true).empty()
            .append($("<option></option>").val("").text("표준용어명을 먼저 입력하세요"));
    }

    function resetDuplicate() {
        Object.assign(state.duplicate, {checking: false, duplicated: false});
        state.duplicate.seq++; // 진행 중인 확인 응답 무시
        q(".term-duplicate").empty();
    }

    function lookupWords(words) {
        const seq = ++state.requestSeq;
        // 표준용어명은 단어를 "_" 로 잇는다 : 영문약어명(USER_NM)과 단어 수가 같아야 한다 (서버에서도 검증)
        const termName = words.join("_");
        state.pending = true;

        $.ajax({
            url: `/api/getStandardTerms/popup/${encodeURIComponent(termName)}`,
            type: "GET",
            success: (response) => {
                if (seq !== state.requestSeq) return; // 이전 입력에 대한 응답은 무시

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

                    return {word, matched, isLast, isDomainWord, status};
                });

                // 미승인 단어는 등록 가능(승인 대기)하므로 경고만 하고, 미등록/도메인 분류 없음은 등록할 수 없다.
                const valid = results.every(r => r.status === "APPROVED" || r.status === "NOT_APPROVED") && domains.length > 0;

                Object.assign(state, {termName, valid, pending: false, domains});
                field("commonStandardTermName").val(termName);

                renderWords(results, valid, domains);

                if (!valid) return;

                field("commonStandardTermAbbreviation").val(results.map(r => r.matched.commonStandardWordAbbreviation).join("_"));
                field("synonyms").val(buildSynonyms(results));
                populateDomains(domains);
            },
            error: () => {
                if (seq === state.requestSeq) state.pending = false;
            }
        });
    }

    /**
     * 단어별 결과 라벨 : "사용자 USER" / "이름 NM · 도메인" / "나이 미등록"
     */
    function renderWords(results, valid, domains) {
        const $words = q(".term-words").empty();

        results.forEach(r => {
            const status = WORD_STATUS[r.status];
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
            messages.push(["table-name-error", "미등록 단어는 데이터 사전 > 표준 단어에서 먼저 등록해야 합니다."]);
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
     * 마지막(도메인) 단어의 이음동의어로 용어 이음동의어를 만든다. (예: 사용자_명 → 사용자_이름, 사용자_성명)
     */
    function buildSynonyms(results) {
        const last = results[results.length - 1].matched;
        const synonyms = String(last.synonyms ?? "").trim();
        if (!synonyms || synonyms === "-") return "";

        const prefix = results.slice(0, -1).map(r => r.word).join("_");
        return synonyms.split(",").map(s => s.trim()).filter(Boolean)
            .map(s => (prefix ? `${prefix}_${s}` : s)).join(",");
    }

    function populateDomains(domains) {
        const $select = q(".term-domain-select").empty().prop("disabled", false)
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
        field("commonStandardDomainName").val(domain?.commonStandardDomainName ?? "");
        field("allowedValues").val(domain?.allowedValues ?? "");
        field("storageFormat").val(domain?.storageFormat ?? "");
        field("displayFormat").val(domain?.displayFormat ?? "");

        resetDuplicate();
        if (domain) checkDuplicate(field("commonStandardTermAbbreviation").val(), domain.commonStandardDomainName);
    }

    /**
     * 같은 영문약어명 + 도메인의 용어가 이미 있는지 바로 확인한다. (저장 시 서버 중복 기준과 동일)
     */
    function checkDuplicate(abbreviation, domainName) {
        if (!abbreviation || !domainName) return;

        const duplicate = state.duplicate;
        const seq = ++duplicate.seq;
        Object.assign(duplicate, {checking: true, duplicated: false});

        $.ajax({
            url: `/api/getStandardTerms/duplicate?abbreviation=${encodeURIComponent(abbreviation)}&domainName=${encodeURIComponent(domainName)}`,
            type: "GET",
            success: (response) => {
                if (seq !== duplicate.seq) return; // 이전 선택에 대한 응답은 무시

                Object.assign(duplicate, {checking: false, duplicated: response.duplicated === true});

                const $box = q(".term-duplicate").empty();
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

    // ---------------------------------------------------------------- 저장

    function save() {
        if (state.saving) return;

        if (state.pending) return window.openAlert("표준 단어를 조회하고 있습니다. 잠시 후 다시 시도해주세요.");
        if (!state.termName) return window.openAlert("표준용어명을 입력해주세요.");
        if (!state.valid) return window.openAlert("표준용어명에 등록할 수 없는 단어가 있습니다. 단어별 안내를 확인해주세요.");
        if (!field("commonStandardDomainName").val()) return window.openAlert("도메인을 선택해주세요.");
        if (state.duplicate.checking) return window.openAlert("중복 여부를 확인하고 있습니다. 잠시 후 다시 시도해주세요.");
        if (state.duplicate.duplicated) return window.openAlert("이미 같은 영문약어명과 도메인으로 등록된 용어가 있습니다.");

        const missing = FIELDS.find(f => f.required && !String(field(f.key).val() ?? "").trim());
        if (missing) {
            window.openAlert(`${missing.label}은(는) 필수 입력 항목입니다.`);
            (missing.readonly ? q(".term-domain-select") : field(missing.key)).trigger("focus");
            return;
        }

        // 화면용 입력칸(표준용어명 입력, 도메인 선택)은 name 이 없으므로 보내지 않는다.
        // 숫자 항목(제정차수)은 숫자로 보내고, 비어 있으면 보내지 않는다. ("" 는 서버의 int 로 변환되지 않음 → 기본값 0)
        const data = {};
        FIELDS.forEach(f => {
            const value = field(f.key).val();
            if (f.type === "number") {
                if (String(value ?? "").trim() !== "") data[f.key] = Number(value);
                return;
            }
            data[f.key] = value;
        });

        state.saving = true;
        $.ajax({
            url: "/api/insertDataDictionary/standardTerms",
            type: "POST",
            data: JSON.stringify(data),
            success: (response) => {
                if (response.result && typeof onSaved === "function") onSaved(data);
            },
            complete: () => state.saving = false
        });
    }
}

function splitWords(value) {
    return String(value ?? "").trim().split(/[_\s]+/).filter(Boolean);
}
