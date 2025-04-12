import {setupAjaxCsrf} from "../../common/csrf.js";

setupAjaxCsrf();
const tableId = "tableManageGrid";

$(document).ready(() => {
    /**
     * 추가버튼 선택 시 테이블을 생성한다.
     */
    $("#grd-add-tableManageGrid").on("click", function(){
        const type = "C";
        const form = createForm({}, type);

        window.openDialog('div', { title: type === 'C' ? '테이블 등록' : '테이블 수정', content: form });

        /**
         * 저장버튼 클릭
         */
        $("#btn-save-table").on("click", function(e){
            e.preventDefault();
            saveTable(type);
        });
    });

    /**
     * 삭제버튼 선택 시 테이블을 삭제처리한다.
     */
    $("#grd-delete-tableManageGrid").on("click", function(){
        const checkedData = getCheckedDataIsNonNull(tableId);

        if(!checkedData) return;

        let isApproval = true;
        checkedData.forEach((e) => {
            if(e.isApproval === 'Y'){
                isApproval = false;
            }
        });

        if(!isApproval){
            window.openAlert("체크된 데이터에 승인된 정보도 포함되어있습니다. 승인이 완료된 경우, 삭제가 불가능합니다.");
            return;
        }

        window.openConfirm("체크된 데이터를 삭제하시겠습니까?", () => {
            $.ajax({
                url: "/api/deleteTable",
                type: "DELETE",
                data: JSON.stringify(checkedData),
                success: (response) => {
                    if(response.result){
                        openAlert("정상적으로 삭제되었습니다.", () => {
                            window.searchGrid(tableId);
                        });
                    }
                }
            })
        });

    });


    $("#grd-active-tableManageGrid").on("click", () => {
        updateActive(true);
    });


    $("#grd-unactive-tableManageGrid").on("click", () => {
        updateActive(false);
    });
});

function updateActive(type){
    const checkedData = getCheckedDataIsNonNull(tableId);
    if(!checkedData) return;

    const msg = type ? "승인" : "승인취소";

    window.openConfirm(`체크된 테이블 정보를 ${msg}하시겠습니까?`, () => {
        // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
        let isApproval = false;

        checkedData.forEach(e => {
            if(e.isApproval === (type ? 'N' : 'Y')){
                isApproval = true;
            }
        });

        if(!isApproval){
            window.openAlert(`${msg} 필요한 정보가 선택되지 않았습니다.`);
            return;
        }

        $.ajax({
            url: `/api/updateTable/${type}`,
            type: 'PATCH',
            data: JSON.stringify(checkedData),
            success : (response) => {
                if(response.result){
                    window.openAlert(`정상적으로 ${msg}처리 되었습니다.`, () => {
                        window.searchGrid(tableId);
                    });
                }
            }
        });
    });
}

/**
 * 폼을 생성한다.
 * @param rowData
 * @param type
 * @returns {string}
 */
function createForm(rowData, type) {
    return `<form id="editTableForm" class="edit-form">
        <input type="hidden" name="id" value="${rowData.id ?? 0}"/>
        <input type="hidden" name="type" value="${type}" />
        
        <div class="form-group">
            <label for="tableName">테이블명</label>
            <input type="text" class="form-control" name="tableName" id="tableName" value="${rowData.tableName ?? ''}" required/>
        </div>

        <div class="form-group">
            <label for="tableDescription">테이블 설명</label>
            <input type="text" class="form-control" name="tableDescription" id="tableDescription"
                   value="${rowData.tableDescription ?? ''}"/>
        </div>

        <div class="form-check" style="margin: 10px 0;">
            <input type="checkbox" class="form-check-input" name="isMasterTable" id="isMasterTable"
                   ${rowData.isMasterTable === 'Y' ? 'checked' : ''} />
            <label class="form-check-label" for="isMasterTable">마스터 테이블 여부</label>
        </div>

        <div class="form-group" style="margin-bottom: 10px;">
            <label for="sortOrder">정렬 순서</label>
            <input type="number" class="form-control" name="sortOrder" id="sortOrder"
                   value="${rowData.sortOrder ?? ''}"
                   onkeydown="if (event.key === 'e' || event.key === 'E' || event.key === '+' || event.key === '-') event.preventDefault();" />
        </div>

        <button type="submit" class="btn btn-primary" id="btn-save-table">저장</button>
    </form>`
}


function saveTable(type){
    const $form = $("#editTableForm");

    const msg = type === "C" ? "저장" : "수정";

    const requiredFields = [
        { name: "tableName", label: "테이블명" },
        { name: "tableDescription", label: "테이블 설명" }
    ];

    // 필수값 검증
    for (const field of requiredFields) {
        const value = $form.find(`[name='${field.name}']`).val();
        if (!value || value.trim() === "") {
            openAlert(`${field.label}은(는) 필수 입력 항목입니다.`);
            return;
        }
    }

    const tableData = {
        id: $form.find("[name='id']").val(),
        type: $form.find("[name='type']").val(),
        tableName: $form.find("[name='tableName']").val(),
        tableDescription: $form.find("[name='tableDescription']").val(),
        isMasterTable: $form.find("[name='isMasterTable']").is(":checked") ? "Y" : "N",
        sortOrder: parseInt($form.find("[name='sortOrder']").val()) || 0
    };

    const method = type === "C" ? "POST" : "PUT";

    openConfirm(`${msg}하시겠습니까?`, () => {
        $.ajax({
            url: "/api/updateTable", // 필요 시 type에 따라 URL 분기 가능
            type: method,
            data: JSON.stringify(tableData),
            success: (response) => {
                if (response.result) {
                    openAlert(`정상적으로 ${msg}되었습니다.`, () => {
                        window.closeDialog("div");
                        window.searchGrid(tableId);
                    });
                }
            }
        });
    });
}

export function selectRow(rowData, columnList, isManager, tableId){
    if(rowData.isApproval === "Y") return;

    const type = "U";
    const form = createForm(rowData, type);

    window.openDialog('div', { title: type === 'C' ? '테이블 등록' : '테이블 수정', content: form });

    /**
     * 저장버튼 클릭
     */
    $("#btn-save-table").on("click", function(e){
        e.preventDefault();
        saveTable(type);
    });
}


window.gridCallbacks["tableManageGrid_selectRow"] = selectRow;
