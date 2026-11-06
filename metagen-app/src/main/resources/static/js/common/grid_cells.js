/**
 * 그리드 셀 꾸미기 헬퍼 (GitHub Primer 스타일)
 * window.gridRowDecorators[tableId] = (row, data, cellOf) => { ... } 안에서 사용한다.
 * 모든 값은 textContent 로 넣어 HTML 이 해석되지 않도록 한다.
 */

/**
 * 셀 내용을 라벨로 교체한다.
 * @param cell    td 요소
 * @param text    라벨 문구
 * @param variant accent | success | attention | danger | done | (없으면 기본 회색)
 * @param withDot 앞에 점 표시 여부
 */
export function setLabel(cell, text, variant = "", withDot = false) {
    if (!cell) return;

    const label = document.createElement("span");
    label.className = ["gh-label", variant ? `gh-label-${variant}` : "", withDot ? "gh-label-dot" : ""]
        .filter(Boolean).join(" ");
    label.textContent = text ?? "";

    cell.replaceChildren(label);
}

/**
 * 셀 내용을 "이니셜 아바타 + 이름" 형태로 교체한다.
 */
export function setUser(cell, name) {
    if (!cell) return;

    const value = String(name ?? "").trim();
    if (!value) {
        setMuted(cell, "-");
        return;
    }

    const wrap = document.createElement("span");
    wrap.className = "gh-user";

    const avatar = document.createElement("span");
    avatar.className = "gh-avatar";
    avatar.textContent = value.charAt(0);

    const text = document.createElement("span");
    text.textContent = value;

    wrap.append(avatar, text);
    cell.replaceChildren(wrap);
}

/**
 * 셀 내용을 고정폭 글꼴(코드 형태)로 교체한다.
 */
export function setMono(cell, text) {
    if (!cell) return;

    const span = document.createElement("span");
    span.className = "gh-mono";
    span.textContent = text ?? "";
    cell.replaceChildren(span);
}

/**
 * 셀 내용을 흐린 글자로 교체한다.
 */
export function setMuted(cell, text) {
    if (!cell) return;

    const span = document.createElement("span");
    span.className = "gh-muted";
    span.textContent = text ?? "";
    cell.replaceChildren(span);
}

/**
 * ISO 일시 문자열을 "yyyy-MM-dd HH:mm:ss" 로 표시한다.
 */
export function setDateTime(cell, value) {
    if (!cell) return;

    const date = new Date(value);
    if (!value || isNaN(date.getTime())) {
        setMuted(cell, "-");
        return;
    }

    const pad = (n) => String(n).padStart(2, "0");
    const text = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} `
        + `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;

    const time = document.createElement("time");
    time.className = "gh-muted";
    time.dateTime = value;
    time.textContent = text;
    cell.replaceChildren(time);
}

/**
 * 그리드에 행 꾸미기 함수를 등록한다.
 */
export function registerRowDecorator(tableId, decorator) {
    window.gridRowDecorators = window.gridRowDecorators || {};
    window.gridRowDecorators[tableId] = decorator;
}

/**
 * 승인 여부(Y/N) → "승인" / "승인 대기" 상태 라벨
 */
export function setApproval(cell, value) {
    if (value === "Y") setLabel(cell, "승인", "success", true);
    else if (value === "N") setLabel(cell, "승인 대기", "attention", true);
}

/**
 * Y/N 값 → Y 는 파란 라벨, N 은 흐린 글자
 * @param text Y 일 때 표시할 문구 (기본 "Y")
 */
export function setYN(cell, value, text = "Y") {
    if (value === "Y") setLabel(cell, text, "accent");
    else if (value === "N") setMuted(cell, "N");
}

/**
 * 여러 필드에 같은 꾸미기 함수를 적용한다.
 */
export function applyAll(cellOf, data, fields, fn) {
    fields.forEach(field => fn(cellOf(field), data[field]));
}
