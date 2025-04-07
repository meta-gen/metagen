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
        switch (data.field){
            case "resetButton" :
                debugger
                break;
        }
    });

});
