/**
 * 표준 도메인 승인 버튼
 *
 */
$("#grd-active-standardDomains").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardDomains");
    if(!checkedData) return;

    window.openConfirm("체크된 표준 도메인를 승인하시겠습니까?", () => {
       // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
       let isApproval = false;

       checkedData.forEach(e => {
           if(e.isApprovalYn === 'N'){
                isApproval = true;
           }
       });

       if(!isApproval){
           window.openAlert("승인이 필요한 표준 도메인가 선택되지 않았습니다.");
           return;
       }

       $.ajax({

           url: "/api/approvalStandardDomains/true",
           type: 'PATCH',
           data: JSON.stringify(checkedData),
           success : (response) => {
               if(response.result){
                   window.openAlert("정상적으로 승인처리 되었습니다.", () => {
                       window.searchGrid("standardDomains");
                   });
               }
           }
       });
    });
});

/**
 * 표준 도메인 승인취소 버튼
 *
 */
$("#grd-unactive-standardDomains").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardDomains");
    if(!checkedData) return;

    window.openConfirm("체크된 표준 도메인를 승인하시겠습니까?", () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApprovalYn === 'Y'){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert("승인취소가 필요한 표준 도메인가 선택되지 않았습니다.");
            return;
        }

        $.ajax({
            url: "/api/approvalStandardDomains/false",
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 승인취소처리 되었습니다.", () => {
                        window.searchGrid("standardDomains");
                    });
                }
            }
        });
    });
});

/**
 * 표준 도메인 추가 버튼
 */
$("#grd-add-standardDomains").on("click", () => {
    addStandardDomains();
});

/**
 * 표준 도메인 삭제 버튼
 */
$("#grd-delete-standardDomains").on("click", () => {
    const tableId = "standardDomains";
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


function addStandardDomains(){
    const columnList = window.tableInstances["standardDomains"]
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

    dialogTitle.text("표준도메인 추가");
    dialogContent.empty(); // 초기화

    const dbDataTypes = [
        "VARCHAR", "CHAR", "TEXT", "CLOB",
        "INT", "INTEGER", "BIGINT", "SMALLINT",
        "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC",
        "DATE", "DATETIME", "TIMESTAMP", "TIME",
        "BOOLEAN", "BIT",
        "BLOB", "BINARY", "VARBINARY", "LONGBLOB", "MEDIUMBLOB",
        "JSON", "ENUM", "SET"
    ];

    const form = $("<form></form>").attr("id", "add-domain-form");

    const inputType = $("<input>").attr("id", "type")
        .attr("name", "type")
        .attr("type", "hidden")
        .attr("value", "C");

    form.append(inputType);

    columnList.forEach(col => {
        const key = col.column;
        const labelText = col.columnName || key;

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
        }else if(key === "revisionNumber" || key === "dataLength" || key === "dataDecimalLength"){
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
        }else if (key === "dataType") {
            input = $("<select></select>")

                .attr("id", key)
                .attr("name", key)
                .css("width", "100%")
                .css("height", "30px");

            dbDataTypes.forEach(type => {
                const option = $("<option></option>")
                    .attr("value", type)
                    .text(type);

                input.append(option);
            });
        }else {
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
    });

    const saveBtn = $("<button></button>")
        .attr("type", "button")
        .text("추가")
        .addClass("btn btn-primary")
        .css("margin-top", "20px")
        .on("click", saveStandardDomain);

    form.append(saveBtn);
    dialogContent.append(form);

    dialog.showModal();
}

/**
 * 그리드 선택 callback Function
 */
export function selectRow(rowData, columnList, isManager, tableId){

    if(rowData.isApprovalYn === 'Y') return;

    const dialogContent = $("<div></div>");

    const dbDataTypes = [
        "VARCHAR", "CHAR", "TEXT", "CLOB",
        "INT", "INTEGER", "BIGINT", "SMALLINT",
        "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC",
        "DATE", "DATETIME", "TIMESTAMP", "TIME",
        "BOOLEAN", "BIT",
        "BLOB", "BINARY", "VARBINARY", "LONGBLOB", "MEDIUMBLOB",
        "JSON", "ENUM", "SET"
    ];

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

    // 각 필드를 input으로 생성
    for (const [key, value] of Object.entries(rowData)) {

        const labelText = columnMap.get(key) || key; // 매칭되는 columnName 없으면 key 그대로 사용

        const label = $("<label></label>")
            .text(labelText)
            .attr("for", key)
            .css("display", "block")
            .css("margin-top", "10px");

        let input;

        if (key === "isApprovalYn") {
            continue;
        }else if(key === "revisionNumber" || key === "dataLength" || key === "dataDecimalLength"){
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
        }else if (key === "dataType") {
            input = $("<select></select>")
                .attr("id", key)
                .attr("name", key)
                .css("width", "100%")
                .css("height", "30px");

            dbDataTypes.forEach(type => {
                const option = $("<option></option>")
                    .attr("value", type)
                    .text(type);
                if (value === type) {
                    option.prop("selected", true);
                }
                input.append(option);
            });
        }else {
            input = $("<input>")
                .attr("type", "text")
                .attr("id", key)
                .attr("name", key)
                .val(value)
                .css("width", "100%");
        }

        // 수정 불가 필드 처리
        if (key === "id" || key === "projectId" || key === "allowedValues" || key === "commonStandardDomainName" || key === "storageFormat" || key === "displayFormat" || key === "commonStandardDomainGroupName" || key === "commonStandardDomainCategory") {
            input.prop("readonly", true);

            input.css("background-color", "#f0f0f0"); // 회색 배경
        }

        form.append(label, input);
    }

    // 저장 버튼 추가
    if(isManager || rowData['isApprovalYn'] === 'N'){
        const saveBtn = $("<button></button>")
            .attr("type", "button")
            .text("저장")
            .addClass("btn btn-primary")
            .css("margin-top", "20px")
            .on("click", saveStandardDomain);

        form.append(saveBtn);
    }else{
        $(`#edit-${tableId} input`).each((index, data) => {
            $(data).prop("readonly",true);
            $(data).css("background-color", "#f0f0f0");
        });
    }

    dialogContent.append(form);

    openDialog("div", {
        title: "표준도메인 수정",
        content: dialogContent
    });
}

window.gridCallbacks["standardDomains_selectRow"] = selectRow;

function saveStandardDomain(e){
    e.preventDefault();

    const tableId = "standardDomains";

    const form = $(e.target).closest("form");
    const inputs = form.find("input, textarea, select");

    const formData = {};
    let isValid = true;

    let type = "C";

    const requiredFields = [
        "allowedValues", "revisionNumber"
    ]

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
        if (!requiredFields.includes(name)) {
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

    console.log(formData);

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
