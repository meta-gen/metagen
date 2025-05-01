import {setupAjaxCsrf} from "../common/csrf.js";

setupAjaxCsrf();

$(document).ready(function () {
	
    // 저장 버튼 클릭 시
    $("#save-button").on("click", function (e) {
		
        e.preventDefault(); // 폼 기본 제출 막기
		
        saveNotice();
    });

    // 페이지 로드 시 파라미터로 데이터 조회
    const params    = new URLSearchParams(window.location.search);
    const projectId = params.get("projectId");
    const type      = params.get("type");

    $.ajax({
        url: `/api/selectCodeRuleManage/template/${projectId}`
      , type: "GET"
      , success: function (response) {
		
            if (response.result) {
				
                setCodeRuleTemplates(response.templates || [], type);
				
                $("#projectId").val(projectId);

                if (type === "modified" && projectId) {
					
                    const id = params.get("id");

                    $.ajax({

                        url: `/api/selectCodeRuleManage/detail/${projectId}/${id}`
                      , type: "GET"
                      , success: (response) => {
						
                            if(response.result) {
								
                                const codeRule = response.codeRuleDto;

                                $("#id").val(codeRule.id);
                                $("#codeRuleName").val(codeRule.codeRuleName);
                                $("#codeRuleDescription").val(codeRule.codeRuleDescription);
                                $("#prefix").val(codeRule.prefix);
                                $("#suffix").val(codeRule.suffix);
                                $("#methodForm").val(codeRule.methodForm);
                                $("#templateSelect").val(String(codeRule.templateId)).prop("disabled", true);
                            }
                        }
                    });
                }
            }
        }
    });

    $("#methodForm").on("keydown", function(e) {
        if (e.key === "Tab") {
            e.preventDefault();

            const start = this.selectionStart;
            const end = this.selectionEnd;

            $(this).val(function(i, val) {
                return val.substring(0, start) + "\t" + val.substring(end);
            });

            this.selectionStart = this.selectionEnd = start + 1;
        }
    });
});


/**
 * 공지사항 등록
 */
function saveNotice(type) {
	
    const $form = $("#editNoticeForm");

    const msg = type === "C" ? "저장" : "수정";

	// 필수값 설정
    const requiredFields = [
		
        { name: "noticeTitle"  , label: "공지사항 제목" }
      , { name: "noticeContent", label: "공지사항 내용" }
    ];

    // 필수값 검증
    for (const field of requiredFields) {
		
        const value = $form.find(`[name='${field.name}']`).val();
		
        if (!value || value.trim() === "") {
			
            openAlert(`${field.label}은(는) 필수 입력 항목입니다.`);
			
            return;
        }
    }

    const noticeData = {
		
        id            : $form.find("[name='id']").val()
      , type          : $form.find("[name='type']").val()
      , noticeTitle   : $form.find("[name='noticeTitle']").val()
      , noticeContent : $form.find("[name='noticeContent']").val()
    };

    const method = type === "C" ? "POST" : "PUT";

    openConfirm(`${msg}하시겠습니까?`, () => {
		
        $.ajax({
            url     : "/api/insertNotice" // 필요 시 type에 따라 URL 분기 가능
          , type    : method
          , data    : JSON.stringify(noticeData)
          , success : (response) => {
			
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