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
        } else if (key === "useAbbreviation" || key === "isFormatWord") {
            input = $("<select></select>")
                .attr("id", key)
                .attr("name", key)
                .css("width", "100%")
                .css("height", "30px");

            const options = [
                { value: "Y", text: "TRUE" },
                { value: "N", text: "FALSE" }
            ];

            options.forEach(opt => {
                input.append($("<option></option>")
                    .attr("value", opt.value)
                    .text(opt.text));
            });
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
        .attr("type", "submit")
        .text("추가")
        .addClass("btn btn-primary")
        .css("margin-top", "20px")
        .on("click", saveStandardWords);

    form.append(saveBtn);
    dialogContent.append(form);

    dialog.showModal();
}

/**
 * 그리드 선택 callback Function
 */
export function selectRow(rowData, columnList, isManager, tableId) {

    if(rowData.isApprovalYn === 'Y') return;

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

    const inputType = $("<input>").attr("id", "type")
        .attr("name", "type")
        .attr("type", "hidden")
        .attr("value", "U");

    form.append(inputType);

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
        }else if (key === "useAbbreviation" || key === "isFormatWord") {
            input = $("<select></select>")
                .attr("id", key)
                .attr("name", key)
                .css("width", "100%")
                .css("height", "30px");

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
        }else {
            input = $("<input>")
                .attr("type", "text")
                .attr("id", key)
                .attr("name", key)
                .val(value)
                .css("width", "100%");
        }

        if (key === "id" || key === "projectId" || key === "commonStandardWordName" || key === "commonStandardWordAbbreviation" || key === "commonStandardWordEnglishName" || key === "commonStandardDomainCategory" || key === "synonyms") {
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
            .css("margin-top", "20px")
            .on("click", saveStandardWords);

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

function saveStandardWords(e){
    e.preventDefault();

    const tableId = "standardWords";

    const form = $(e.target).closest("form");
    const inputs = form.find("input, textarea, select");

    const formData = {};
    let isValid = true;

    let type = "C";

    const requiredFields = [
        "commonStandardDomainCategory", "synonyms", "restrictedWords", "revisionNumber"
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
