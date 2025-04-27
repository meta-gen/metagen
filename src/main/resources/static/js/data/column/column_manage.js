import {getCsrfToken, setupAjaxCsrf} from "../../common/csrf.js";
import {downloadFile} from "../../common/common.js";

const tableId = "columnManageGrid";

$(document).ready(() => {
    setupAjaxCsrf();

    /**
     * 템플릿 다운로드 버튼 클릭 이벤트
     */
    $("#download-template").on("click", function () {
        $.ajax({
            url: '/api/downloadTemplate/columnTemplate',
            type: 'GET',
            xhrFields: {
                responseType: 'blob'
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "컬럼 템플릿.xlsx");
                closeDialog("div");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });

    $("#grd-add-columnManageGrid").on("click", () => {
       const type = "C";
       const form = createForm({}, type);

        window.openDialog('div', { title: type === 'C' ? '컬럼 등록' : '컬럼 수정', content: form });

        /**
         * 저장버튼 클릭
         */
        $("#btn-save-column").on("click", function(e){
            e.preventDefault();
            saveColumn(type);
        });

        /**
         * 추가 시에만 수정이 가능하도록 설계
         */
        $("#open-table-select").on("click", function(){
           openTableSelectPopup();
        });
    });

    /**
     * 승인버튼 클릭 시
     */
    $("#grd-active-columnManageGrid").on("click", () => {
        updateActive(true);
    });

    /**
     * 승인취소버튼 클릭 시
     */
    $("#grd-unactive-columnManageGrid").on("click", () => {
        updateActive(false);
    });

    $("#grd-delete-columnManageGrid").on("click", () => {
       deleteColumns();
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
                url: "/api/uploadColumnExcelFile",
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
                    window.openAlert(xhr.responseJSON.message);
                }
            });
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

    const dataTypes = ["VARCHAR", "CHAR", "NUMBER", "DATE", "TIMESTAMP", "BOOLEAN", "CLOB", "BLOB"];

    const dataTypeDatalist = `
        <datalist id="dataTypeOptions">
            ${dataTypes.map(dt => `<option value="${dt}"></option>`).join("")}
        </datalist>
    `;

    const tableSelectButton = type === "C"
        ? `<button type="button" class="btn btn-secondary" id="open-table-select">🔍</button>`
        : "";

    return `<form id="editTableForm" class="edit-form">
        <input type="hidden" name="id" value="${rowData.id ?? 0}"/>
        <input type="hidden" name="type" value="${type}" />
        <input type="hidden" name="tableInfoId" id="tableInfoId" value=""/>
        <input type="hidden" name="termId" id="termId" value=""/>
        
        <!-- 필수 정보 -->
        <div class="form-group">
            <label for="tableName">테이블명</label>
            <div style="display: flex; gap: 8px;">
                <input type="text" class="form-control" name="tableName" id="tableName"
                       value="${rowData.tableName ?? ''}" readonly disabled style="flex: 1;" />
                ${tableSelectButton}
            </div>
        </div>

        <div class="form-group">
            <label for="columnName">컬럼명</label>
            <input type="text" class="form-control" name="columnName" id="columnName"
                   value="${rowData.columnName ?? ''}" readonly disabled required />
        </div>

        <div class="form-group">
            <label for="dataType">데이터 타입</label>
            <input type="text" class="form-control" name="dataType" id="dataType"
                   list="dataTypeOptions" value="${rowData.dataType ?? ''}" />
        </div>
        ${dataTypeDatalist}
        
        <div class="form-group">
            <label for="columnDesc">컬럼 설명</label>
            <input type="text" class="form-control" name="columnDesc" id="columnDesc"
                   value="${rowData.columnDesc ?? ''}" />
        </div>

        <div class="form-check">
            <input type="checkbox" class="form-check-input" name="isNullable" id="isNullable"
                   ${rowData.isNullable === 'Y' ? 'checked' : ''} />
            <label class="form-check-label" for="isNullable">NULL 허용 여부</label>
        </div>
        
        <div class="form-check">
            <input type="checkbox" class="form-check-input" name="isNullable" id="isPk"
                   ${rowData.isPk === 'Y' ? 'checked' : ''} />
            <label class="form-check-label" for="isPk">PK 여부</label>
        </div>
        
        <div class="form-group">
            <label for="maxLength">최대 길이</label>
            <input type="number" class="form-control" name="maxLength" id="maxLength"
                   value="${rowData.maxLength ?? ''}" />
        </div>        

        <!-- 부가 정보 펼치기 -->
        <div class="form-group mt-3">
            <button type="button" class="btn btn-outline-secondary btn-sm" onclick="toggleExtraInfo()">부가 정보 보기 ▼</button>
        </div>

        <div id="extra-info-section" style="display: none; margin-top: 10px; border-top: 1px solid #ccc; padding-top: 10px;">
            
            <div class="form-group">
                <label for="precision">정밀도</label>
                <input type="number" class="form-control" name="precision" id="precision"
                       value="${rowData.precision ?? ''}" />
            </div>

            <div class="form-group">
                <label for="scale">소수 자릿수</label>
                <input type="number" class="form-control" name="scale" id="scale"
                       value="${rowData.scale ?? ''}" />
            </div>

            <div class="form-group">
                <label for="defaultValue">기본값</label>
                <input type="text" class="form-control" name="defaultValue" id="defaultValue"
                       value="${rowData.defaultValue ?? ''}" />
            </div>

            <div class="form-group">
                <label for="sortOrder">정렬 순서</label>
                <input type="number" class="form-control" name="sortOrder" id="sortOrder"
                       value="${rowData.sortOrder ?? ''}" />
            </div>

            <div class="form-group">
                <label for="refTableName">참조 테이블명</label>
                <input type="text" class="form-control" name="refTableName" id="refTableName"
                       value="${rowData.refTableName ?? ''}" />
            </div>

            <div class="form-group">
                <label for="example">데이터 예시</label>
                <input type="text" class="form-control" name="example" id="example"
                       value="${rowData.example ?? ''}" />
            </div>

            ${[
        { id: 'isMasterData', label: '마스터 데이터 여부' },
        { id: 'isRequired', label: '필수 입력 여부' },
        { id: 'isSensitive', label: '민감정보 여부' },
        { id: 'isUnique', label: '고유값 여부' },
        { id: 'isIndex', label: '인덱스 생성 여부' },
        { id: 'isEncrypted', label: '암호화 필요 여부' }
    ].map(info => `
                <div class="form-check">
                    <input type="checkbox" class="form-check-input" name="${info.id}" id="${info.id}"
                        ${rowData[info.id] === 'Y' ? 'checked' : ''} />
                    <label class="form-check-label" for="${info.id}">${info.label}</label>
                </div>
            `).join("")}
        </div>

        <button type="submit" class="btn btn-primary mt-3" id="btn-save-column">저장</button>
    </form>`;
}

/**
 * 컬럼 저장을 수행한다.
 * @param type
 */
function saveColumn(type){
    const form = document.getElementById("editTableForm");

    if (!form) {
        openAlert("폼을 찾을 수 없습니다.");
        return;
    }

    const requiredFields = {
        tableName: "테이블명을 선택해주세요.",
        columnName: "컬럼명을 입력해주세요.",
        dataType: "데이터 타입을 선택해주세요.",
        columnDesc: "컬럼 설명을 입력해주세요."
    };

    for (const [field, message] of Object.entries(requiredFields)) {
        const value = form[field]?.value?.trim();
        if (!value) {
            openAlert(message);
            return;
        }
    }

    // 데이터 수집
    const data = {
        id: Number(form.id?.value || 0),
        tableInfoId: Number(form.tableInfoId?.value),
        termId: Number(form.termId?.value),
        tableName: form.tableName.value.trim(),
        columnName: form.columnName.value.trim(),
        dataType: form.dataType.value.trim(),
        columnDesc: form.columnDesc.value.trim(),
        maxLength: form.maxLength?.value ? Number(form.maxLength.value) : null,
        precision: form.precision?.value ? Number(form.precision.value) : null,
        scale: form.scale?.value ? Number(form.scale.value) : null,
        defaultValue: form.defaultValue?.value?.trim() || "",
        sortOrder: form.sortOrder?.value ? Number(form.sortOrder.value) : null,
        refTableName: form.refTableName?.value?.trim() || "",
        example: form.example?.value?.trim() || "",
        isPk: form.isPk?.checked ? "Y" : "N",
        isNullable: form.isNullable?.checked ? "Y" : "N",
        isMasterData: form.isMasterData?.checked ? "Y" : "N",
        isRequired: form.isRequired?.checked ? "Y" : "N",
        isSensitive: form.isSensitive?.checked ? "Y" : "N",
        isUnique: form.isUnique?.checked ? "Y" : "N",
        isIndex: form.isIndex?.checked ? "Y" : "N",
        isEncrypted: form.isEncrypted?.checked ? "Y" : "N"
    };

    const msg = type === "C"? "추가" : "저장";

    const restType = type === "C" ? "POST" : "PUT";

    openConfirm(`${msg}하시겠습니까?`, () => {
        $.ajax({
            url: "/api/saveColumn",
            type: restType,
            data: JSON.stringify(data),
            success: (response) => {
                if(response.result){
                    openAlert(`정상적으로 ${msg}되었습니다.`, () => {
                        window.closeDialog("div");
                        window.searchGrid(tableId);
                    });
                }
            }
        });
    });
}

/**
 * 테이블 조회 팝업을 연다.
 * 승인된 테이블만 조회가 가능하도록 한다.
 */
function openTableSelectPopup(){
    const popup = window.open(
        "/popup/columTableSearch",  // 팝업으로 띄울 URL
        "테이블 조회",     // 팝업 이름 (중복 방지용)
        "width=700,height=800,resizable=yes,scrollbars=yes"
    );
}

window.toggleExtraInfo = function () {
    const section = document.getElementById("extra-info-section");
    if (section.style.display === "none") {
        section.style.display = "block";
    } else {
        section.style.display = "none";
    }
};

function receiveTableInfo(data){

    const term = data.termValue.split("(");

    const replaceData = term[1].replace(")","").split(" ");

    $("#tableName").val(data.value);
    $("#tableInfoId").val(data.key);
    $("#columnName").val(term[0].trim());
    $("#dataType").val(replaceData[1]);
    $("#columnDesc").val(replaceData[0]);
    $("#termId").val(data.term);
}

window.popupFunction = window.popupFunction || {}; // 혹시 없을 경우 방지
window.popupFunction['receiveTableInfo'] = receiveTableInfo;


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
            url: `/api/saveColumn/approval/${type}`,
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

function deleteColumns(){
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
            url: "/api/deleteColumn",
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
}

/**
 *
 * 컬럼관리 목록 조회
 * @param rowData
 * @param columnList
 * @param isManager
 * @param tableId
 */
export function columnSelectRow(rowData, columnList, isManager, tableId){
    if(rowData.isApproval === "Y") return;

    $.ajax({
        url: `/api/selectColumn/detail/${rowData.id}`,
        type: "GET",
        success: (response) => {
            if(response.result){
                if(!(response.columnInfos?.length)){
                    openAlert("승인된 컬럼이 존재하여 수정이 불가능합니다.");
                    return;
                }

                const popup = window.open(
                    "/popup/columTableSearch/detail",  // 팝업으로 띄울 URL
                    "컬럼 상세",     // 팝업 이름 (중복 방지용)
                    "width=700,height=800,resizable=yes,scrollbars=yes"
                );

                const sendData = () => {
                    if (popup && popup.receiveColumnDetailData) {
                        popup.receiveColumnDetailData(response.columnInfos);
                    } else {
                        // 로딩 안 끝났을 수 있으므로 재시도
                        setTimeout(sendData, 100);
                    }
                };

                // 약간의 delay를 주고 전송 시도
                setTimeout(sendData, 200);
            }
        }
    })
}

window.gridCallbacks["columnManageGrid_selectRow"] = columnSelectRow;


function columnManageGridSuccess(){
    openConfirm("정상적으로 순서가 저장되었습니다.", () => {
        window.searchGrid(tableId);
    });
}

window.popupFunction = window.popupFunction || {}; // 혹시 없을 경우 방지
window.popupFunction['columnManageGridSuccess'] = columnManageGridSuccess;
