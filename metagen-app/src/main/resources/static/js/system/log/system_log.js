$(document).ready(function () {

    const tableId = 'logTable';

    // 상세보기에 표시할 항목
    const detailFields = [
        { field: "requestBody", label: "요청 데이터" },
        { field: "responseBody", label: "응답 데이터" },
        { field: "errorMessage", label: "에러 메시지" },
        { field: "userAgent", label: "브라우저 정보" }
    ];

    /**
     * 상세보기 버튼 클릭 시 요청/응답 데이터, 에러 메시지, 브라우저 정보를 다이얼로그로 보여준다.
     */
    window.getClickedData(tableId, (data) => {
        if (data.field !== "detailButton") return;

        const rowData = data.rowData;
        const $content = $("<div></div>");

        detailFields.forEach(item => {
            $content.append($("<label></label>").addClass("dialog-detail-label").text(item.label));
            $content.append($("<pre></pre>").addClass("dialog-detail-value").text(formatValue(rowData[item.field])));
        });

        $("#mainConfirm").addClass("dialog-wide");
        window.openDialog("div", { title: `${rowData.method ?? ""} ${rowData.logUrl ?? ""}`, content: $content });
    });

    /**
     * JSON 문자열이면 보기 좋게 정렬하고, 값이 없으면 '-'를 반환한다.
     */
    function formatValue(value) {
        if (value === null || value === undefined || String(value).trim() === "") return "-";

        try {
            return JSON.stringify(JSON.parse(value), null, 2);
        } catch (e) {
            return String(value);
        }
    }
});
