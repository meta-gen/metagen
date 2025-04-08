import {setupAjaxCsrf} from "../../common/csrf.js";


$(document).ready(() => {
    setupAjaxCsrf();
    const tableId = "accessGrid";


    $("#grd-save-accessGrid").on("click", () => {

        window.openConfirm("인가 목록의 수정된 권한을 저장하시겠습니까?", () => {
            let data = window.getSelectedData(tableId);

            $.ajax({
                url: "/api/updateAccess/role",
                type: "PUT",
                data: JSON.stringify(data),
                success : (response) => {
                    if(response.result){
                        openAlert("정상적으로 수정되었습니다.", () => {
                            $.ajax({
                                url: "/api/updateRole",
                                type: "GET",
                                success: (response) => {
                                    if(response.result){
                                        window.searchGrid(tableId);
                                    }
                                }
                            });
                        });
                    }
                }
            })
        });

    });
});
