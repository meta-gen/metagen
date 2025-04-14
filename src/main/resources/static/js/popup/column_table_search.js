$(document).ready(() => {

    /**
     * 조회버튼 클릭 시 검색조건에 존재하는 데이터를 기준으로 테이블 정보를 조회한다.
     */
    $("#search-btn").on("click", () => {

        const tableName = $("#table_name_text").val();

        if(tableName === ""){
            alert("검색조건이 입력되지 않았습니다. 검색조건 입력 이후 검색이 가능합니다.");
            return;
        }

        $.ajax({
            url: `/api/selectColumn/table/${tableName}`,
            type: "GET",
            success: (response) => {
                if(response.result){
                    addTableSelector(response.tableInfos);
                }
            }
        });
    });
});

function addTableSelector(tableInfos){
    // 기존 셀렉트 박스 가져오기
    const select = document.getElementById("table-select");

    // 기본 옵션 추가
    select.innerHTML = '<option value="">테이블을 선택하세요</option>';

    // 데이터 기반으로 옵션 추가
    tableInfos.forEach(table => {
        const option = document.createElement("option");
        option.value = table.id;
        option.textContent = table.tableName;
        select.appendChild(option);
    });
}
