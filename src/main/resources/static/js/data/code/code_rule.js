import {setupAjaxCsrf} from "../../common/csrf.js";

const tableId = "codeRuleGrid";

$(document).ready(() => {
    setupAjaxCsrf();

    /**
     * 추가버튼 클릭 시
     */
    $("#grd-add-codeRuleGrid").on("click", () => {
        const popup = window.open(
            `/popup/codeRuleDetailPopup?type=add`,  // 전달 파라미터
            "코드규칙 등록/수정",
            "width=800,height=900,resizable=yes,scrollbars=yes"
        );
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
    const codeRuleDetailId = rowData.id;

    // 팝업 열기
    const popup = window.open(
        `/popup/codeRuleDetailPopup?type=modified&id=${codeRuleDetailId}`,  // 전달 파라미터
        "코드규칙 등록/수정",
        "width=800,height=900,resizable=yes,scrollbars=yes"
    );
}

window.gridCallbacks["codeRuleTable_selectRow"] = codeRuleGridClick;


function codeRuleSuccess(){
    openAlert("정상적으로 코드규칙이 저장되었습니다.", () => {
        window.searchGrid(tableId);
    });
}

window.popupFunction = window.popupFunction || {}; // 혹시 없을 경우 방지
window.popupFunction['codeRuleSuccess'] = codeRuleSuccess;
