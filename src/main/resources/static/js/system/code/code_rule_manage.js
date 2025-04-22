import {setupAjaxCsrf} from "../../common/csrf.js";

const tableId = 'codeRuleManageGrid';

$(document).ready(function () {
    setupAjaxCsrf();

    const $selector = $("#projectSelector");

    // 초기 표시
    const selectedOption = $selector.find("option:selected");
    const isDicAbbrUsed = selectedOption.data("isdicabbrused");
    setSwaggerText(isDicAbbrUsed);

    // 선택 변경 시
    $selector.on("change", function () {
        const selectedOption = $(this).find("option:selected");
        const isDicAbbrUsed = selectedOption.data("isdicabbrused");

        setSwaggerText(isDicAbbrUsed);

        if (window.tableInstances[tableId]) {
            window.tableInstances[tableId].destroy();
            delete window.tableInstances[tableId];
        }

        const dataUrl = '/api/selectCodeRuleManage/' + $(this).val();

        $('#' + tableId + ' thead tr').empty();
        window.grid(tableId, dataUrl, 'cd', 'codeRuleManageGrid_selectRow');
    });

    function setSwaggerText(isDicAbbrUsed) {
        let text = "";

        if (isDicAbbrUsed === true || isDicAbbrUsed === "true") {
            text = "등록된 표준용어 약어를 기준으로 메소드 이름이 생성됩니다.";
        } else {
            text = "데이터 이름의 띄어쓰기를 기준으로 표준단어의 영문명을 조합해 메소드 이름이 생성됩니다.";
        }

        $("#projectSelectorSwaggerText").text(text);
        $("#mProjectSelectorSwaggerText").text(text);
    }

    /**
     * 템플릿 등록
     */
    $("#btn-project-add").on("click", () => {
        const dialogContent = $("<div></div>");
        const form = $("<form></form>").attr("id", `edit-${tableId}`);

        // 템플릿 이름
        form.append(`
            <div class="form-group">
                <label for="templateName">템플릿 이름</label>
                <input type="text" id="templateName" name="templateName" class="form-control" />
            </div>
        `);

        // 템플릿 설명
        form.append(`
            <div class="form-group">
                <label for="templateDescription">템플릿 설명</label>
                <input type="text" id="templateDescription" name="templateDescription" class="form-control" />
            </div>
        `);


        const saveBtn = $("<button></button>")
            .attr("type", "submit")
            .text("저장")
            .attr("id", "approvalSubmit")
            .attr("name", "approvalSubmit")
            .addClass("btn btn-primary")
            .css("margin-top", "20px")
            .on("click", codeRuleSubmitClick);

        form.append(saveBtn);
        dialogContent.append(form);

        openDialog("div", {
            title: "템플릿 등록",
            content: dialogContent
        });
    });


    /**
     * 템플릿 제거
     */
    $("#btn-project-delete").on("click", () => {

        const projectId = $('#projectSelector').val();

        $.ajax({
            url: `/api/selectCodeRuleManage/template/${projectId}`,
            type: "GET",
            success: (response) => {
                if(response.result){
                    deleteTemplate(response);
                }
            }
        })
    });

    $("#grd-add-codeRuleManageGrid").on("click", () => {
        const projectId = $('#projectSelector').val();

        // 팝업 열기
        const popup = window.open(
            `/popup/codeRulePopup?projectId=${projectId}&type=add`,  // 전달 파라미터
            "코드규칙관리 등록/수정",
            "width=800,height=900,resizable=yes,scrollbars=yes"
        );
    });

    $("#grd-delete-codeRuleManageGrid").on("click", () => {
       const projectId = $('#projectSelector').val();

        const checkedData = getCheckedDataIsNonNull(tableId);

        if(checkedData.length === 0){
            return;
        }

       openConfirm("선택한 코드규칙을 삭제하시겠습니까?", () => {
           $.ajax({
               url: `/api/deleteCodeRuleManage/${projectId}`,
               type: "DELETE",
               data: JSON.stringify(checkedData),
               success: (response) => {
                   if(response.result){
                       openAlert("선택한 코드규칙을 정상적으로 삭제하였습니다.", () => {
                          window.searchGrid(tableId);
                       });
                   }
               }
           })
       });
    });



});

/**
 * 템플릿 정보를 가져와 등록을 수행한다.
 * @param e
 */
function codeRuleSubmitClick(e){
    e.preventDefault();

    const templateName = $("#templateName").val().trim();
    const templateDescription = $("#templateDescription").val().trim();

    // 유효성 검사 (옵션)
    if (!templateName || !templateDescription) {
        openAlert("템플릿 정보를 입력해주세요.");
        return;
    }

    const payload = {
        templateName,
        templateDescription
    };

    const projectId = $('#projectSelector').val();

    $.ajax({
        url: `/api/saveCodeRuleManage/template/${projectId}`,
        type: "POST",
        data: JSON.stringify(payload),
        success: (response) => {
            if(response.result){
                openAlert("정상적으로 템플릿이 저장되었습니다.", () => {
                    window.searchGrid(tableId);
                    closeDialog("div");
                });
            }
        }
    })
}

function deleteTemplate(response){
    const templateList = response.templates || [];

    if (templateList.length === 0) {
        openAlert("삭제할 수 있는 템플릿이 없습니다.");
        return;
    }

    const dialogContent = $("<div></div>");
    const form = $("<form></form>").attr("id", "delete-template-form");

    // 템플릿 선택 셀렉트 박스
    const selectBox = $(`
                        <div class="form-group">
                            <label for="templateSelect">삭제할 템플릿 선택</label>
                            <select id="templateSelect" class="form-control">
                                <option value="">템플릿을 선택하세요</option>
                            </select>
                        </div>
                    `);

    templateList.forEach(template => {
        const option = $("<option></option>")
            .val(template.id)
            .text(template.templateName);
        selectBox.find("select").append(option);
    });

    // 삭제 버튼
    const deleteBtn = $("<button></button>")
        .attr("type", "submit")
        .text("삭제")
        .addClass("btn btn-secondary")
        .css("margin-top", "20px")
        .css("width", "100%")
        .on("click", codeRuleSelectorDelete);

    form.append(selectBox).append(deleteBtn);
    dialogContent.append(form);

    openDialog("div", {
        title: "템플릿 삭제",
        content: dialogContent
    });
}

function codeRuleSelectorDelete(e) {

    e.preventDefault();

    const selectedId = $("#templateSelect").val();
    if (!selectedId) {
        openAlert("삭제할 템플릿을 선택해주세요.");
        return;
    }

    openConfirm("선택한 템플릿을 삭제하시겠습니까?", () => {
        $.ajax({
            url: `/api/deleteCodeRuleManage/template/${selectedId}`,
            type: "DELETE",
            success: (deleteResponse) => {
                if (deleteResponse.result) {
                    openAlert("템플릿이 삭제되었습니다.", () => {
                        window.searchGrid(tableId);
                        closeDialog("div");
                    });
                }
            }
        });
    });
}

function codeRuleManageSuccess(){
    openAlert("정상적으로 코드규칙이 저장되었습니다.", () => {
        window.searchGrid(tableId);
    });
}

window.popupFunction = window.popupFunction || {}; // 혹시 없을 경우 방지
window.popupFunction['codeRuleManageSuccess'] = codeRuleManageSuccess;


/**
 *
 * 코드규칙관리 상세 조회
 * @param rowData
 * @param columnList
 * @param isManager
 * @param tableId
 */
export function codeRuleManageDetail(rowData, columnList, isManager, tableId){

    const projectId = $('#projectSelector').val();
    const codeRuleId = rowData.id;

    // 팝업 열기
    const popup = window.open(
        `/popup/codeRulePopup?projectId=${projectId}&type=modified&id=${codeRuleId}`,  // 전달 파라미터
        "코드규칙관리 등록/수정",
        "width=800,height=900,resizable=yes,scrollbars=yes"
    );
}

window.gridCallbacks["codeRuleManageGrid_selectRow"] = codeRuleManageDetail;
