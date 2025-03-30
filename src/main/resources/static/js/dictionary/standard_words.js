/**
 * 표준 단어 승인 버튼
 *
 */
$("#grd-active-standardWords").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardWords");
    if(!checkedData) return;

    window.openConfirm("체크된 표준 단어를 승인하시겠습니까?", () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApprovalYn === 'N'){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert("승인이 필요한 표준 단어가 선택되지 않았습니다.");
            return;
        }

        $.ajax({

            url: "/api/approvalStandardWords/true",
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 승인처리 되었습니다.", () => {
                        window.searchGrid("standardWords");
                    });
                }
            }
        });
    });
});

/**
 * 표준 단어 승인취소 버튼
 *
 */
$("#grd-unactive-standardWords").on("click", () => {

    const checkedData = getCheckedDataIsNonNull("standardWords");
    if(!checkedData) return;

    window.openConfirm("체크된 표준 단어를 승인하시겠습니까?", () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApprovalYn === 'Y'){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert("승인취소가 필요한 표준 단어가 선택되지 않았습니다.");
            return;
        }

        $.ajax({
            url: "/api/approvalStandardWords/false",
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert("정상적으로 승인취소처리 되었습니다.", () => {
                        window.searchGrid("standardWords");
                    });
                }
            }
        });
    });
});

/**
 * 표준 단어 추가 버튼
 */
$("#grd-add-standardWords").on("click", () => {
    addStandardWords();
});


/**
 * 표준 단어 삭제 버튼
 */
$("#grd-delete-standardWords").on("click", () => {
    const tableId = "standardWords";
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

function addStandardWords(){
    const columnList = window.tableInstances["standardWords"]
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

    dialogTitle.text("표준단어 추가");
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
        } else if (key === "isFormatWord") {
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
export function selectRow(rowData, columnList, isManager, tableId) {

    console.log(rowData);
    console.log(columnList);

    const dialog = $("#mainConfirm")[0];
    const dialogTitle = $("#mainDialogTitle");
    const dialogContent = $("#mainDialogContent");

    dialogTitle.text("표준단어 수정");
    dialogContent.empty(); // 초기화

    // key와 columnName을 매핑할 Map 생성
    const columnMap = new Map();
    columnList.forEach(col => {
        columnMap.set(col.column, col.columnName);
    });

    const form = $("<form></form>").attr("id", `edit-${tableId}`);

    for (const [key, value] of Object.entries(rowData)) {
        const labelText = columnMap.get(key) || key;

        const label = $("<label></label>")
            .text(labelText)
            .attr("for", key)
            .css("display", "block")
            .css("margin-top", "10px");

        let input;

        if(key === "isApprovalYn"){
            continue;
        }else if ( key === "isFormatWord") {
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

        if (key === "id" || key === "projectId") {
            input.prop("readonly", true);
            input.css("background-color", "#f0f0f0");
        }

        form.append(label, input);
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

    dialog.showModal(); // 그대로 유지
}

window.gridCallbacks["standardWords_selectRow"] = selectRow;
