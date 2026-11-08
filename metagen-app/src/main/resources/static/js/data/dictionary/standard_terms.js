import {openDictionaryDetail} from "./dictionary_detail.js";
import {createTermForm} from "./term_create_form.js";
import {registerRowDecorator, setApproval, setMono} from "../../common/grid_cells.js";

/**
 * 표준 용어 행 꾸미기 : 영문약어는 코드 글꼴, 승인여부는 상태 라벨
 */
registerRowDecorator("standardTerms", (row, data, cellOf) => {
    setMono(cellOf("commonStandardTermAbbreviation"), data.commonStandardTermAbbreviation);
    setApproval(cellOf("isApprovalYn"), data.isApprovalYn);
});

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
 * 표준용어를 등록한다. (등록 폼/단어 조회/저장은 term_create_form.js 공용 모듈)
 */
function addStandardTerms(){
    const form = createTermForm({
        onSaved: () => {
            window.openAlert("정상적으로 등록되었습니다.", () => {
                window.closeDialog("div");
                window.searchGrid("standardTerms");
            });
        }
    });

    window.openDialog("div", {title: "표준용어 추가", content: form});
    form.find(".term-word-input").trigger("focus");
}

/**
 * 그리드 선택 callback Function
 */
export function selectRow(rowData, columnList, isManager, tableId) {
    // 그리드에서 숨긴 항목까지 상세보기로 보여주고, 승인 전 항목만 수정 팝업으로 이동한다.
    openDictionaryDetail({
        title: "표준용어 상세",
        rowData,
        columnList,
        onEdit: rowData.isApprovalYn === 'Y' ? null : () => openEditDialog(rowData, columnList, isManager, tableId)
    });
}

/**
 * 표준용어 수정 팝업
 */
function openEditDialog(rowData, columnList, isManager, tableId) {

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
            // 수정 시 표준용어명은 바꿀 수 없다 (읽기 전용)
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
        "commonStandardTermName",
        "commonStandardTermDescription"
    ];

    const form = $(e.target).closest("form");

    // 단어 입력칸(termWordInput)은 화면용이므로 name 이 없는 항목은 보내지 않는다.
    const inputs = form.find("input[name], textarea[name]");

    const formData = {};
    let isValid = true;

    let type = "C";

    inputs.each(function () {
        const $input = $(this);
        const name = $input.attr("name");
        const value = $input.val();

        // 값 저장
        if(name === "revisionNumber"){
            // 제정차수는 int : 비어 있으면 0 ("" 는 서버에서 변환되지 않는다)
            formData[name] = String(value ?? "").trim() === "" ? 0 : Number(value);
        }else if(name !== "type"){
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
