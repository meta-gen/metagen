import {getCsrfToken, setupAjaxCsrf} from "../../common/csrf.js";
import {downloadFile} from "../../common/common.js";

setupAjaxCsrf();
const tableId = "tableManageGrid";

$(document).ready(() => {

    /**
     * 템플릿 다운로드 버튼 클릭 이벤트
     */
    $("#download-template").on("click", function () {
        $.ajax({
            url: '/api/downloadTemplate/tableTemplate',
            type: 'GET',
            xhrFields: {
                responseType: 'blob'
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "테이블 템플릿.xlsx");
                closeDialog("div");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });

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

    $("#excel-upload").on("click", () => {
        $("#upload-file").click();
    });

    /**
     * 엑셀 파일이 업로드 된 이후 서버에 전송 및 파싱처리
     */
    $("#upload-file").on("change", () => {
        window.openConfirm("업로드 하시겠습니까?", () => {
            const file = $("#upload-file")[0].files[0]; // 선택한 파일 가져오기
            if (!file) {
                alert("업로드할 파일을 선택해주세요.");
                return;
            }

            // FormData 생성 후 파일 추가
            const formData = new FormData();
            formData.append("file", file);

            for (let pair of formData.entries()) {
                console.log(pair[0] + ', ' + pair[1]);
            }

            $.ajax({
                url: "/api/uploadTableExcelFile",
                type: "POST",
                data: formData,
                processData: false,  // FormData를 쿼리 스트링으로 변환하지 않음
                contentType: false,  // `multipart/form-data`가 자동 설정되도록 false로 설정
                beforeSend: function (xhr) {  // setupAjaxCsrf() 영향 방지
                    xhr.setRequestHeader('X-XSRF-TOKEN', getCsrfToken()); // CSRF 토큰만 추가
                    // AJAX 요청이 시작될 때 로딩 바 표시
                    $("#loading-bar").show();
                },
                success: function (response) {
                    if(response.result === "success"){
                        window.openAlert("업로드가 완료되었습니다.");
                        window.searchGrid(tableId);
                        $("#upload-file").val('');
                    }
                },
                error: function (xhr, status, error) {
                    alert("파일 업로드 중 오류가 발생했습니다.");
                    console.error("업로드 실패:", status, error);
                }
            });
        });
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

export function tableSelectRow(rowData, columnList, isManager, tableId){
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


window.gridCallbacks["tableManageGrid_selectRow"] = tableSelectRow;
