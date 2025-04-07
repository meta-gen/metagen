import {getCsrfToken, setupAjaxCsrf} from "../../common/csrf.js";

$(document).ready(() => {
    setupAjaxCsrf();

    const tableId = "userTable";

    /**
     * 저장 버튼 클릭
     */
    $("#grd-save-userTable").on("click", function(){

        openConfirm("변경 데이터를 저장하시겠습니까?", () => {
            let data = window.getSelectedData(tableId);

            $.ajax({
                url : '/api/saveUser/update',
                type : "PUT",
                data : JSON.stringify(data),
                success : (response) => {
                    if(response.result){
                        window.openAlert("정상적으로 저장되었습니다.", () => {
                            window.searchGrid(tableId);
                        });
                    }
                }
            });
        });
    });

    /**
     * 초기화 버튼을 클릭했을 떄 비밀번호를 초기화할 수 있도록 한다.
     */
    window.getClickedData(tableId, (data) => {

        const message = `해당 ${data.rowData.username}의 비밀번호를 초기화 하시겠습니까? 아이디와 동일한 값으로 비밀번호가 초기화됩니다.`;

        window.openConfirm(message, () => {
            if(data.field === "resetButton"){
                $.ajax({
                    url: "/api/saveUser/password",
                    type: "PUT",
                    data: JSON.stringify(data.rowData),
                    success: (response) => {
                        if(response.result){
                            window.openAlert("정상적으로 초기화되었습니다.", () => {
                                window.searchGrid(tableId);
                            });
                        }
                    }
                });
            }
        });
    });

    $("#grd-delete-userTable").on("click", function(){

        const checkedData = getCheckedDataIsNonNull(tableId);
        if(!checkedData) return;

        window.openConfirm("선택한 정보를 삭제하시겠습니까?", () => {
           $.ajax({
               url: "/api/deleteUser/user",
               type: "DELETE",
               data: JSON.stringify(checkedData),
               success: (response) => {
                   if(response.result){
                       window.openAlert("정상적으로 삭제되었습니다.", () => {
                           window.searchGrid(tableId);
                       });
                   }
               }
           });
        });
    });

});
