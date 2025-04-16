$(document).ready(() => {

    /**
     * 조회버튼 클릭 시 검색조건에 존재하는 데이터를 기준으로 테이블 정보를 조회한다.
     */
    $("#search-btn").on("click", (e) => {

        e.preventDefault();

        const tableName = $("#table_name_text").val();

        const $confirm = $("#confirm-btn");

        $confirm.css("display", "none");
        $confirm.prop("disabled", true);
        $("#term-search-box").css("display", "none");

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

    $("#confirm-btn").on("click", () => {
        const $select = $("#table-select");
        const term = $("#term-select");
        // 부모로 데이터 전달
        if (window.opener && typeof window.opener.popupFunction?.receiveTableInfo === 'function') {
            window.opener.popupFunction.receiveTableInfo({key : $select.val(), value : $select.find("option:selected").text(), term: term.val(), termValue: term.find("option:selected").text()});
        } else {
            alert("부모 창과의 통신이 불가능합니다.");
        }

        window.close();
    });

    $("#search-term-btn").on("click", () => {
        const type = $("#term-search-type").val();
        const searchData = $("#term-search-text").val();

        if (!searchData) {
            alert("검색어를 입력해주세요.");
            return;
        }

        $.ajax({
            url: `/api/selectColumn/term/${type}/${searchData}`,
            type: "GET",
            success: (response) => {
                if(response.result){
                    const termDtos = response.termDtos || [];

                    const $select = $("#term-select");
                    $select.empty();
                    $select.append(`<option value="">표준용어 선택</option>`);

                    termDtos.forEach(term => {
                        const $option = $("<option>")
                            .val(term.id)
                            .text(`${term.commonStandardTermAbbreviation} (${term.commonStandardTermName} ${term.commonStandardDomainName})`);
                        $select.append($option);
                    });
                }
            }
        });
    });

    $("#term-select").on("change", function () {
        const selectedVal = $(this).val();
        const $confirm = $("#confirm-btn");

        if (selectedVal !== "") {
            $confirm.css("display", "inline-block");
            $confirm.prop("disabled", false);
        } else {
            $confirm.css("display", "none");
            $confirm.prop("disabled", true);
        }
    });

});

/**
 * 테이블 조회 후 select 박스 생성
 * @param tableInfos
 */
function addTableSelector(tableInfos) {
    const $select = $("#table-select");

    // 기존 옵션 초기화 및 기본 옵션 추가
    $select.html('<option value="">테이블을 선택하세요</option>');

    // 옵션 추가
    tableInfos.forEach(table => {
        const $option = $("<option>")
            .val(table.id)
            .text(table.tableName);
        $select.append($option);
    });

    $select.on("change", function () {
        const selectedId = $(this).val();

        if(selectedId !== ""){
            $("#term-search-box").css("display", "flex");
        }else{
            $("#term-search-box").css("display", "none");
        }

        $.ajax({
            url: `/api/selectColumn/tableColumn/${selectedId}`,
            type: "GET",
            success: (response) => {
                if(response.result){
                    const $descBox = $("#term-desc-box"); // 출력 위치
                    $descBox.empty();

                    const columns = response.columnInfos || [];

                    if (columns.length === 0) {
                        $descBox.append(`<p style="color: red;">표시할 컬럼 정보가 없습니다.</p>`);
                        return;
                    }

                    columns.forEach(col => {
                        const $block = $("<div>").css({
                            marginBottom: "15px",
                            borderBottom: "1px solid #ddd",
                            paddingBottom: "10px"
                        });

                        $block.append(`<p><strong>컬럼명:</strong> ${col.columnName}</p>`);
                        $block.append(`<p><strong>설명:</strong> ${col.columnDesc || '-'}</p>`);
                        $block.append(`<p><strong>데이터 타입:</strong> ${col.dataType}</p>`);
                        $block.append(`<p><strong>PK 여부:</strong> ${col.isPk === 'Y' ? '✔️' : ''}</p>`);
                        $block.append(`<p><strong>NULL 허용 여부:</strong> ${col.isNullable === 'Y' ? '허용' : '불가'}</p>`);

                        $descBox.append($block);
                    });
                }
            }
        })

    });
}

