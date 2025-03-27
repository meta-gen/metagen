/**
 * 표준 도메인 승인 버튼
 *
 */
$("#grd-active-standardDomains").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardDomains");
    debugger
    if(!checkedData) return;

    window.openConfirm("체크된 표준 도메인를 승인하시겠습니까?", () => {
       // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
       let isApproval = false;

       checkedData.forEach(e => {
           if(e.isApprovalYn === 'N'){
                isApproval = true;
           }
       });

       if(!isApproval) window.openAlert("승인이 필요한 표준 도메인가 선택되지 않았습니다.");
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
    const checkedData = getCheckedDataIsNonNull("standardDomains");
    if(!checkedData) return;
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

    const form = $("<form></form>").attr("id", "add-domain-form");

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
        } else if (key === "isApprovalYn") {
            input = $("<select></select>")
                .attr("id", key)
                .attr("name", key)
                .css("width", "100%");

            const options = [
                { value: "Y", text: "TRUE" },
                { value: "N", text: "FALSE" }
            ];

            options.forEach(opt => {
                input.append($("<option></option>")
                    .attr("value", opt.value)
                    .text(opt.text));
            });
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
export function selectRow(rowData, columnList, isManager, tableId){

    console.log(rowData);
    console.log(columnList);

    const dialogContent = $("<div></div>");
    const form = $("<form></form>").attr("id", `edit-${tableId}`);

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
            input = $("<select></select>")
                .attr("id", key)
                .attr("name", key)
                .css("width", "100%");

            const options = [
                { value: "Y", text: "TRUE" },
                { value: "N", text: "FALSE" }
            ];

            options.forEach(opt => {
                const optionEl = $("<option></option>")
                    .attr("value", opt.value)
                    .text(opt.text);

                if (opt.value === value) {
                    optionEl.attr("selected", "selected");
                }

                input.append(optionEl);
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
        if (key === "id" || key === "projectId") {
            input.prop("readonly", true);

            input.css("background-color", "#f0f0f0"); // 회색 배경
        }

        form.append(label, input);
    }

    // 저장 버튼 추가
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
        title: "표준도메인 수정",
        content: dialogContent
    });
}

window.gridCallbacks["standardDomains_selectRow"] = selectRow;
