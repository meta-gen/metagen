import {closePopupDialog, openPopupDialog} from "../common/popup_dialog.js";

/**
 * 컬럼 순서 변경 다이얼로그 (데이터 관리 > 컬럼 관리 > 행 선택)
 * 같은 테이블의 (승인 전) 컬럼을 카드로 보여주고, 화살표로 순서를 바꿔 저장한다.
 *
 * @param columnInfos 같은 테이블의 컬럼 목록 (/api/selectColumn/detail/{id} 응답)
 * @param onSuccess   순서 저장 성공 시 호출 (다이얼로그는 닫힌 뒤 호출)
 */
export function openColumnTableDetail(columnInfos, {onSuccess} = {}) {
    if (!Array.isArray(columnInfos) || columnInfos.length === 0) return;

    openPopupDialog({
        url: "/popup/columTableSearch/detail",
        title: `컬럼 순서 · ${columnInfos[0].tableName ?? ""}`,
        size: "large",
        onLoad: ($root) => initColumnTableDetail($root, columnInfos, onSuccess)
    });
}

const CARD_ROWS = [
    [
        {label: "컬럼 설명", key: "columnDesc"},
        {label: "데이터 타입", key: "dataType"},
        {label: "최대 길이", key: "maxLength"},
        {label: "PK 여부", key: "isPk"}
    ],
    [
        {label: "기본값", key: "defaultValue"},
        {label: "NULL 허용", key: "isNullable"},
        {label: "필수 입력", key: "isRequired"},
        {label: "암호화 여부", key: "isEncrypted"}
    ],
    [
        {label: "정렬 순서", key: "sortOrder"},
        {label: "참조 테이블명", key: "refTableName"},
        {label: "예시", key: "example"},
        {label: "민감정보 여부", key: "isSensitive"}
    ]
];

const ICON_UP = '<svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M3.22 10.53a.749.749 0 0 1 0-1.06l4.25-4.25a.749.749 0 0 1 1.06 0l4.25 4.25a.749.749 0 1 1-1.06 1.06L8 6.811 4.28 10.53a.749.749 0 0 1-1.06 0Z"/></svg>';
const ICON_DOWN = '<svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M12.78 5.22a.749.749 0 0 1 0 1.06l-4.25 4.25a.749.749 0 0 1-1.06 0L3.22 6.28a.749.749 0 1 1 1.06-1.06L8 8.939l3.72-3.719a.749.749 0 0 1 1.06 0Z"/></svg>';

function initColumnTableDetail($root, columnInfos, onSuccess) {
    const q = (selector) => $root.find(selector);

    const $cardList = $('<div class="column-card-list"></div>');
    columnInfos.forEach(col => $cardList.append(createCard(col)));
    q("#column-table-container").empty().append($cardList);
    updateSortOrders($root);

    // 정렬 버튼 (다이얼로그 안에서만)
    $root.on("click", ".btn-move-up", function () {
        const $card = $(this).closest(".column-card");
        const $prev = $card.prev(".column-card");
        if ($prev.length) {
            $card.insertBefore($prev);
            updateSortOrders($root);
        }
    });

    $root.on("click", ".btn-move-down", function () {
        const $card = $(this).closest(".column-card");
        const $next = $card.next(".column-card");
        if ($next.length) {
            $card.insertAfter($next);
            updateSortOrders($root);
        }
    });

    /**
     * 순서저장 : 화면에 보이는 순서대로 정렬순서를 저장한다.
     */
    q("#save-sort-order").on("click", () => {
        const sortData = q(".column-card").map((index, el) => ({
            id: $(el).data("id"),
            sortOrder: index + 1
        })).get();

        $.ajax({
            url: "/api/saveColumn/sortOrder",
            type: "PATCH",
            data: JSON.stringify(sortData),
            success: (response) => {
                if (response.result) {
                    closePopupDialog();
                    if (typeof onSuccess === "function") onSuccess();
                }
            }
        });
    });
}

function createCard(col) {
    const $card = $('<div class="column-card"></div>').attr("data-id", col.id);

    // 컬럼명 + 정렬 버튼
    $card.append($('<div class="column-row full"></div>').append(
        $('<span class="label"></span>').text(col.columnName ?? ""),
        $('<div class="sort-buttons"></div>').append(
            $('<button type="button" class="btn-move-up" title="위로" aria-label="위로"></button>').html(ICON_UP),
            $('<button type="button" class="btn-move-down" title="아래로" aria-label="아래로"></button>').html(ICON_DOWN)
        )
    ));

    CARD_ROWS.forEach(rowSet => {
        const $row = $('<div class="column-grid-row"></div>');
        rowSet.forEach(field => {
            const value = col[field.key] ?? "-";
            const highlight = value === "Y" && ["isRequired", "isPk", "isEncrypted"].includes(field.key);
            $row.append($('<div class="pair"></div>').attr("data-field", field.key).append(
                $('<span class="label"></span>').text(field.label),
                $('<span class="value"></span>').toggleClass("highlight", highlight).text(value)
            ));
        });
        $card.append($row);
    });

    return $card;
}

function updateSortOrders($root) {
    $root.find(".column-card").each((index, card) => {
        $(card).find('.pair[data-field="sortOrder"] .value').text(index + 1);
    });
}
