import {setupAjaxCsrf} from "../../common/csrf.js";


$(document).ready(() => {
    setupAjaxCsrf();
    const tableId = "tableManageGrid";

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
        $("#btn-save-table").on("click", function(){
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
            debugger
        });

    });



});

/**
 * 폼을 생성한다.
 * @param rowData
 * @param type
 * @returns {string}
 */
function createForm(rowData, type) {
    return `<form id="editTableForm" class="edit-form">
        <input type="hidden" name="id" value="${rowData.id}"/>
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
                   ${rowData.isMasterTable ? 'checked' : ''} />
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

}

export function selectRow(rowData, columnList, isManager, tableId){
    if(rowData.isApproval === "Y") return;

    const type = "U";
    const form = createForm(rowData, type);

    window.openDialog('div', { title: type === 'C' ? '테이블 등록' : '테이블 수정', content: form });
}


window.gridCallbacks["tableManageGrid_selectRow"] = selectRow;
