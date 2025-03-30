/**
 * 표준 용어 승인 버튼
 *
 */
$("#grd-active-standardTerms").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardTerms");
    if(!checkedData) return;

    window.openConfirm("체크된 표준 용어를 승인하시겠습니까?", () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApprovalYn === 'N'){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert("승인이 필요한 표준 용어가 선택되지 않았습니다.");
            return;
        }

        $.ajax({

            url: "/api/approvalStandardTerms/true",
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 승인처리 되었습니다.", () => {
                        window.searchGrid("standardTerms");
                    });
                }
            }
        });
    });
});

/**
 * 표준 용어 승인취소 버튼
 *
 */
$("#grd-unactive-standardTerms").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardTerms");
    if(!checkedData) return;

    window.openConfirm("체크된 표준 용어를 승인하시겠습니까?", () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApprovalYn === 'Y'){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert("승인취소가 필요한 표준 용어가 선택되지 않았습니다.");
            return;
        }

        $.ajax({
            url: "/api/approvalStandardTerms/false",
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 승인취소처리 되었습니다.", () => {
                        window.searchGrid("standardTerms");
                    });
                }
            }
        });
    });
});

/**
 * 표준용어 추가 버튼
 */
$("#grd-add-standardTerms").on("click", () => {
    addStandardTerms();
});

/**
 * 표준용어 삭제 버튼
 */
$("#grd-delete-standardTerms").on("click", () => {
    const checkedData = getCheckedDataIsNonNull("standardTerms");
    if(!checkedData) return;

    let isApproval = true;
    checkedData.forEach((e) => {
        if(e.isApprovalYn === 'Y'){
            isApproval = false;
        }
    });

    if(!isApproval){
        window.openAlert("체크된 데이터에 승인된 정보도 포함되어있습니다. 승인이 완료된 경우, 삭제가 불가능합니다.");
        return;
    }
});

/**
 * 표준용어를 등록한다.
 */
function addStandardTerms(){
    const columnList = window.tableInstances["standardTerms"]
        .settings()
        .init()
        .columns
        .filter(col => col.data !== "id" && col.data !== "isApprovalYn")
        .map(col => {
            return {
                column: col.data,
                columnName: col.title || col.columnName || col.data
            };
        });

    const dialog = $("#mainConfirm")[0];
    const dialogTitle = $("#mainDialogTitle");
    const dialogContent = $("#mainDialogContent");

    dialogTitle.text("표준용어 추가");
    dialogContent.empty(); // 초기화

    const form = $("<form></form>").attr("id", "add-domain-form");

    columnList.forEach(col => {
        const key = col.column;
        const labelText = col.columnName || key;

        if (key === "commonStandardTermAbbreviation") {
            const label = $("<label></label>")
                .attr("for", key)
                .text(labelText)
                .css({
                    display: "block",
                    marginTop: "10px",
                    fontWeight: "bold"
                });

            const wrapper = $("<div></div>").css({
                display: "flex",
                alignItems: "center",
                gap: "10px",
                marginBottom: "10px"
            });

            const input = $("<input>")
                .attr("type", "text")
                .attr("id", key)
                .attr("name", key)
                .prop("readonly", true)
                .css({
                    flex: "1",
                    backgroundColor: "#f0f0f0",
                    height: "38px",
                    paddingLeft: "10px",
                    border: "1px solid #ccc",
                    borderRadius: "4px"
                });

            const searchBtn = $("<button></button>")
                .attr("type", "button")
                .addClass("btn btn-secondary")
                .html("🔍")
                .css({
                    height: "38px"
                })
                .on("click", function () {
                    openDomainSearchPopup();
                });

            wrapper.append(input, searchBtn);
            form.append(label, wrapper); // ✅ label 따로, wrapper 따로 append
        }else{

            const label = $("<label></label>")
                .text(labelText)
                .attr("for", key)
                .css("display", "block")
                .css("margin-top", "10px");

            let input;

            if (["commonStandardDomainDescription", "allowedValues"].includes(key)) {
                input = $("<textarea></textarea>")
                    .attr("id", key)
                    .attr("name", key)
                    .css("width", "100%")
                    .css("height", "60px");
            } else {
                input = $("<input>")
                    .attr("type", "text")
                    .attr("id", key)
                    .attr("name", key)
                    .css("width", "100%");
            }

            // id, projectId는 제외하거나 readonly
            if (key === "id" || key === "projectId") {
                input.prop("readonly", true).css("background-color", "#f0f0f0");
            }

            form.append(label, input);
        }
    });

    const saveBtn = $("<button></button>")
        .attr("type", "submit")
        .text("추가")
        .addClass("btn btn-primary")
        .css("margin-top", "20px");

    form.append(saveBtn);
    dialogContent.append(form);

    dialog.showModal();
}

/**
 * 그리드 선택 callback Function
 */
export function selectRow(rowData, columnList, isManager, tableId) {

    console.log(rowData);
    console.log(columnList);


    const dialogContent = $("<div></div>");
    const form = $("<form></form>").attr("id", `edit-${tableId}`);

    // key와 columnName을 매핑할 Map 생성
    const columnMap = new Map();
    columnList.forEach(col => {
        columnMap.set(col.column, col.columnName);
    });

    for (const [key, value] of Object.entries(rowData)) {
        const labelText = columnMap.get(key) || key; // 매칭되는 columnName 없으면 key 그대로 사용

        if (key === "commonStandardTermAbbreviation") {
            const label = $("<label></label>")
                .attr("for", key)
                .text(labelText)
                .css({
                    display: "block",
                    marginTop: "10px",
                    fontWeight: "bold"
                });

            const wrapper = $("<div></div>").css({
                display: "flex",
                alignItems: "center",
                gap: "10px",
                marginBottom: "10px"
            });

            const input = $("<input>")
                .attr("type", "text")
                .attr("id", key)
                .attr("name", key)
                .val(value || "") // 기본값 처리
                .prop("readonly", true)
                .css({
                    flex: "1",
                    backgroundColor: "#f0f0f0",
                    height: "38px",
                    paddingLeft: "10px",
                    border: "1px solid #ccc",
                    borderRadius: "4px"
                });

            const searchBtn = $("<button></button>")
                .attr("type", "button")
                .addClass("btn btn-secondary")
                .html("🔍")
                .css({
                    height: "38px"
                })
                .on("click", function () {
                    openDomainSearchPopup();
                });

            wrapper.append(input, searchBtn);
            form.append(label, wrapper); // ✅ label 따로, wrapper 따로 append
        }else{

            const label = $("<label></label>")
                .text(labelText)
                .attr("for", key)
                .css("display", "block")
                .css("margin-top", "10px");

            let input;

            if (["commonStandardTermDescription", "synonyms"].includes(key)) {
                input = $("<textarea></textarea>")
                    .attr("id", key)
                    .attr("name", key)
                    .val(value)
                    .css("width", "100%")
                    .css("height", "60px");
            }else if (key === "isApprovalYn") {
                continue;
            } else {
                input = $("<input>")
                    .attr("type", "text")
                    .attr("id", key)
                    .attr("name", key)
                    .val(value)
                    .css("width", "100%");
            }

            // 수정 불가 필드 처리
            if (key === "id" || key === "projectId") {
                input.prop("readonly", true);
                input.css("background-color", "#f0f0f0");
            }
            form.append(label, input);
        }
    }

    if(isManager || rowData['isApprovalYn'] === 'N'){
        const saveBtn = $("<button></button>")
            .attr("type", "submit")
            .text("저장")
            .addClass("btn btn-primary")
            .css("margin-top", "20px");

        form.append(saveBtn);
    }else{
        $(`#edit-${tableId} input`).each((index, data) => {
            $(data).prop("readonly",true);
            $(data).css("background-color", "#f0f0f0");
        });
    }

    dialogContent.append(form);

    openDialog("div", {
        title: "표준용어 수정",
        content: dialogContent
    });
}

window.gridCallbacks["standardTerms_selectRow"] = selectRow;


/**
 * 용어 검색 팝업을 호출한다.
 */
function openDomainSearchPopup(){
    const value = $("#commonStandardTermName").val();
    if(value === undefined || value === ""){
        window.openAlert("표준용어명 입력 후 검색이 가능합니다.");
        return;
    }

    const popup = window.open(
        "/popup/standardTermSearch",  // 팝업으로 띄울 URL
        "약어 명 검색",     // 팝업 이름 (중복 방지용)
        "width=600,height=800,resizable=yes,scrollbars=yes"
    );

    popup.name = JSON.stringify({ "standardTermName" : value });
}
