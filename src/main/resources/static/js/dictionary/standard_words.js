/**
 * 표준 단어 승인 버튼
 *
 */
$("#grd-active-standardWords").on("click", () => {
    window.openConfirm("체크된 표준 단어를 승인하시겠습니까?", () => {
       const checkedData = getCheckedData("standardWords");
       console.log(checkedData);

       // 승인 필요 대상이 하나라도 존재한다면 true로 반환되어 승인로직을 탈 수 있게 된다.
       let isApproval = false;

       checkedData.forEach(e => {
           if(e.isApprovalYn === 'N'){
                isApproval = true;
           }
       });

       if(!isApproval) window.openAlert("승인이 필요한 표준 단어가 선택되지 않았습니다.");
    });
});

/**
 * 표준 단어 추가 버튼
 */
$("#grd-add-standardWords").on("click", () => {

});

/**
 * 표준 단어 삭제 버튼
 */
$("#grd-delete-standardWords").on("click", () => {

});

/**
 * 그리드 선택 callback Function
 */
export function selectRow(rowData, tableId){
    console.log(rowData);
}

window.gridCallbacks["standardWords_selectRow"] = selectRow;
