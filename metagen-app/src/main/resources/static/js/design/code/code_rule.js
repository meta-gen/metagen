import {setupAjaxCsrf} from "../../common/csrf.js";
import {registerCodeRuleDecorator} from "./code_rule_cells.js";
import {openCodeRuleDetailPopup} from "../../popup/code_rule_detail_popup.js";

registerCodeRuleDecorator();

const tableId = "codeRuleGrid";

$(document).ready(() => {
    setupAjaxCsrf();

    /**
     * 추가버튼 클릭 시
     */
    $("#grd-add-codeRuleGrid").on("click", () => {
        openCodeRuleDetailPopup({type: "add", onSuccess: codeRuleSuccess});
    });

    /**
     * 삭제버튼 클릭 시
     */
    $("#grd-delete-codeRuleGrid").on("click", () => {
        const checkedData = getCheckedDataIsNonNull(tableId);

        if(!checkedData) return;

        window.openConfirm("선택한 코드 규칙을 삭제하시겠습니까?", () => {
            $.ajax({
                url: "/api/deleteCodeRule",
                type : "DELETE",
                data: JSON.stringify(checkedData),
                success: (response) => {
                    if(response.result){
                        openAlert("선택한 코드규칙이 삭제되었습니다.", () => {
                            window.searchGrid(tableId);
                        })
                    }
                }
            });
        });
    });

});

/**
 * 코드규칙 그리드 선택
 * @param rowData
 * @param columnList
 * @param isManager
 * @param tableId
 */
export function codeRuleGridClick(rowData, columnList, isManager, tableId){
    openCodeRuleDetailPopup({type: "modified", id: rowData.id, onSuccess: codeRuleSuccess});
}

window.gridCallbacks["codeRuleTable_selectRow"] = codeRuleGridClick;


function codeRuleSuccess(){
    openAlert("정상적으로 코드규칙이 저장되었습니다.", () => {
        window.searchGrid(tableId);
    });
}
