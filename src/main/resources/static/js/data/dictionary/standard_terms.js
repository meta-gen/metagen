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
    const tableId = "standardTerms";
    const checkedData = getCheckedDataIsNonNull(tableId);
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

    window.openConfirm("체크된 데이터를 삭제하시겠습니까?", () => {
        $.ajax({
            url : `/api/deleteDataDictionary/${tableId}`,
            type : 'DELETE',
            data : JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 삭제처리 되었습니다.", () => {
                        window.searchGrid(tableId);
                    });
                }
            }
        });
    });
});

/**
 * 표준용어를 등록한다.
 */
function addStandardTerms(){
    const tableId = "standardTerms";

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

    const form = $("<form></form>").attr("id", `edit-${tableId}`);

    const inputType = $("<input>").attr("id", "type")
        .attr("name", "type")
        .attr("type", "hidden")
        .attr("value", "C");

    form.append(inputType);

    columnList.forEach(col => {
        const key = col.column;
        const labelText = col.columnName || key;

        if (key === "commonStandardTermName") {
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
            form.append(label, wrapper); // label 따로, wrapper 따로 append
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
            }else if(key === "revisionNumber"){
                input = $("<input>")
                    .attr("type", "number")
                    .attr("id", key)
                    .attr("name", key)
                    .css("width", "100%")
                    .on("keydown", function(event){
                        if (event.key === "e" || event.key === "E" || event.key === "+" || event.key === "-") {
                            event.preventDefault();
                        }
                    });
            } else {
                input = $("<input>")
                    .attr("type", "text")
                    .attr("id", key)
                    .attr("name", key)
                    .css("width", "100%");
            }

            // id, projectId는 제외하거나 readonly
            if (key === "id" || key === "projectId" || key === "commonStandardDomainName" || key === "allowedValues" || key === "storageFormat" || key === "displayFormat" || key === "synonyms" || key === "commonStandardTermName" || key === "commonStandardTermAbbreviation") {
                input.prop("readonly", true).css({
                        flex: "1",
                        backgroundColor: "#f0f0f0",
                        height: "30px",
                        paddingLeft: "10px",
                        border: "1px solid #ccc",
                        borderRadius: "4px"
                    });
            }

            form.append(label, input);
        }
    });

    const saveBtn = $("<button></button>")
        .attr("type", "button")
        .text("추가")
        .attr("id", "approvalSubmit")
        .attr("name", "approvalSubmit")
        .addClass("btn btn-primary")
        .css("margin-top", "20px")
        .on("click", approvalSubmitClick);

    form.append(saveBtn);
    dialogContent.append(form);

    dialog.showModal();
}

/**
 * 그리드 선택 callback Function
 */
export function selectRow(rowData, columnList, isManager, tableId) {

    const dialogContent = $("<div></div>");
    const form = $("<form></form>").attr("id", `edit-${tableId}`);

    const inputType = $("<input>").attr("id", "type")
        .attr("name", "type")
        .attr("type", "hidden")
        .attr("value", "U");

    form.append(inputType);

    // key와 columnName을 매핑할 Map 생성
    const columnMap = new Map();
    columnList.forEach(col => {
        columnMap.set(col.column, col.columnName);
    });

    for (const [key, value] of Object.entries(rowData)) {
        const labelText = columnMap.get(key) || key; // 매칭되는 columnName 없으면 key 그대로 사용

        if (key === "commonStandardTermName") {
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
/*

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
*/

            wrapper.append(input);
            form.append(label, wrapper); // label 따로, wrapper 따로 append
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
            }else if(key === "revisionNumber"){
                input = $("<input>")
                    .attr("type", "number")
                    .attr("id", key)
                    .attr("name", key)
                    .val(value)
                    .css("width", "100%")
                    .on("keydown", function(event){
                        if (event.key === "e" || event.key === "E" || event.key === "+" || event.key === "-") {
                            event.preventDefault();
                        }
                    });
            } else {
                input = $("<input>")
                    .attr("type", "text")
                    .attr("id", key)
                    .attr("name", key)
                    .val(value)
                    .css("width", "100%");
            }

            // 수정 불가 필드 처리
            if (key === "id" || key === "projectId" || key === "commonStandardDomainName" || key === "allowedValues" || key === "storageFormat" || key === "displayFormat" || key === "synonyms" || key === "commonStandardTermName" || key === "commonStandardTermAbbreviation") {
                input.prop("readonly", true);
                input.css("background-color", "#f0f0f0");
            }
            form.append(label, input);
        }
    }

    if(isManager || rowData['isApprovalYn'] === 'N'){
        const saveBtn = $("<button></button>")
            .attr("type", "button")
            .text("저장")
            .attr("id", "approvalSubmit")
            .attr("name", "approvalSubmit")
            .addClass("btn btn-primary")
            .css("margin-top", "20px")
            .on("click", approvalSubmitClick);

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

    const popup = window.open(
        "/popup/standardTermSearch",  // 팝업으로 띄울 URL
        "약어 명 검색",     // 팝업 이름 (중복 방지용)
        "width=600,height=800,resizable=yes,scrollbars=yes"
    );

    popup.name = JSON.stringify({ "standardTermName" : value });
}

function receiveTermAbbreviation(data){
    $("#commonStandardTermAbbreviation").val(data.abbreviation);
    $("#commonStandardDomainName").val(data.domainName);
    $("#allowedValues").val(data.allowedValues);
    $("#storageFormat").val(data.storageFormat);
    $("#displayFormat").val(data.displayFormat);
    $("#synonyms").val(data.synonyms);
    $("#commonStandardTermName").val(data.termWordText);
}

window.popupFunction = window.popupFunction || {}; // 혹시 없을 경우 방지
window.popupFunction['receiveTermAbbreviation'] = receiveTermAbbreviation;


/**
 * 저장/추가버튼을 눌려 저장을 수행한다.
 * @param e
 */
function approvalSubmitClick(e){
    e.preventDefault();

    const tableId = "standardTerms";

    const requiredFields = [
        "commonStandardDomainName",
        "allowedValues",
        "storageFormat",
        "displayFormat",
        "synonyms",
        "commonStandardTermName",
        "commonStandardTermDescription"
    ];

    const form = $(e.target).closest("form");
    const inputs = form.find("input, textarea");

    const formData = {};
    let isValid = true;

    let type = "C";

    inputs.each(function () {
        const $input = $(this);
        const name = $input.attr("name");
        const value = $input.val();

        // 값 저장
        if(name !== "type"){
            formData[name] = value;
        }else{
            type = value;
        }

        // 필수값 체크
        if (requiredFields.includes(name)) {
            if (!value || value.trim() === "") {
                const labelText = form.find(`label[for='${name}']`).text() || name;
                alert(`필수값 누락: ${labelText}`);
                $input.focus();
                isValid = false;
                return false; // .each 중단
            }
        }
    });

    if (!isValid) return;

    const url = "/api/" + (type === "C" ? `insertDataDictionary/${tableId}` : `updateDataDictionary/${tableId}`);
    const ajaxType = type === "C" ? "POST" : "PUT"

    $.ajax({
        url: url,
        type : ajaxType,
        data : JSON.stringify(formData),
        success : (response) => {

            if(response.result){
                window.openAlert(`정상적으로 ${type === "C" ? "등록" : "수정"}되었습니다.`, () => {
                    window.closeDialog("div");
                    window.searchGrid(tableId);
                });
            }
        }
    })


}
