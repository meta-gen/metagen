/**
 * 데이터 사전(표준 도메인/단어/용어) 상세보기 팝업
 * 그리드에서 숨긴 컬럼까지 모든 항목을 읽기 전용으로 보여주고, 승인 전 항목은 [수정] 버튼으로 수정 팝업을 연다.
 * 모든 값은 textContent 로 넣어 HTML 이 해석되지 않도록 한다.
 */

/**
 * @param title      팝업 제목
 * @param rowData    선택한 행 데이터
 * @param columnList 그리드 컬럼 정의 (/column 응답)
 * @param onEdit     수정 팝업 호출 함수 (없으면 수정 버튼 미표시 : 승인 완료 등)
 */
export function openDictionaryDetail({title, rowData, columnList, onEdit}) {
    const $content = $("<div></div>");
    const $list = $("<dl></dl>").addClass("dict-detail");

    columnList
        .filter(col => col.rowType !== "CHECKBOX" && col.column !== "isApprovalYn")
        .forEach(col => {
            $list.append($("<dt></dt>").text(col.columnName || col.column));
            $list.append(valueElement(rowData[col.column]));
        });

    $content.append(approvalLabel(rowData.isApprovalYn), $list);

    if (typeof onEdit === "function") {
        const $editBtn = $("<button></button>")
            .attr("type", "button")
            .addClass("btn btn-primary")
            .text("수정")
            .on("click", () => {
                // 같은 다이얼로그(#mainConfirm)를 다시 열기 때문에 먼저 닫는다.
                window.closeDialog("div");
                onEdit();
            });

        $content.append($("<div></div>").addClass("gh-dialog-actions").append($editBtn));
    }

    window.openDialog("div", {title, content: $content});
}

function valueElement(value) {
    const isEmpty = value === null || value === undefined || String(value).trim() === "";
    return $("<dd></dd>")
        .toggleClass("gh-muted", isEmpty)
        .text(isEmpty ? "-" : String(value));
}

function approvalLabel(value) {
    const approved = value === "Y";
    return $("<div></div>").addClass("dict-detail-status").append(
        $("<span></span>")
            .addClass(`gh-label gh-label-dot ${approved ? "gh-label-success" : "gh-label-attention"}`)
            .text(approved ? "승인" : "승인 대기"),
        $("<span></span>")
            .addClass("gh-muted")
            .text(approved ? "승인된 항목은 수정할 수 없습니다." : "")
    );
}
