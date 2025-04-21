import { getCsrfToken, setupAjaxCsrf } from "../../common/csrf.js";

$(document).ready(function() {

	setupAjaxCsrf();

	const $addBtn = $('#btn-notice-add');
	const tableId = "noticeList";

	/** 공지사항 저장버튼 클릭 이벤트
	 *   - 공지사항 저장버튼 클릭 시 수행되는 동작 정의
	 */
	$("#grd-add-noticeList").on("click", function(e) {

		const type = "C";
		const form = createForm({}, type);

		window.openDialog('div', { title: type === 'C' ? '공지사항 등록' : '공지사항 수정', content: form });

		$("#btn-save-table").on("click", (e) => {

			e.preventDefault();

			debugger;
		});
	});


	/**
	 * 폼을 생성한다.
	 * @param rowData
	 * @param type
	 * @returns {string}
	 */
	function createForm(rowData, type) {

		return `
			<form id="editNoticeForm" class="edit-form">
		        <input type="hidden" name="id" value="${rowData.id ?? 0}"/>
		        <input type="hidden" name="type" value="${type}" />
	        
		        <div class="form-group">
		            <label for="noticeTitle">공지사항 제목</label>
		            <input type="text" id="noticeTitle" class="form-control" name="noticeTitle" value="${rowData.noticeTitle ?? ''}" required/>
		        </div>
				<div class="form-group">
		            <label for="noticeContent">공지사항 내용</label>
		            <textarea id="noticeContent" class="form-control" name="noticeContent" style="height: 300px;"></textarea>
		        </div>
				<div class="form-group">
		            <label for="noticeFile">파일 등록</label>
		            <input type="file" class="form-control" name="noticeFile" id="noticeFile" value="${rowData.noticeContent ?? ''}" required/>
		        </div>

	        	<button type="submit" class="btn btn-primary" id="btn-save-notice">저장</button>
	    	</form>
		`
	}


	/**
	 * 추가버튼 클릭 이벤트
	 */
	$("#grd-add-noticeGrid").on("click", function() {

		selectProjectMemberDialog();
	});


	// 데이터 상세 내용 확인
	function selectRow(rowData, columnList, isManager, tableId) {

		// 매니저일 경우
		if (isManager) {

			const type = "U";
			const form = createForm({}, type);

			window.openDialog('div', { title: type === 'C' ? '공지사항 등록' : '공지사항 수정', content: form });

			$("#btn-save-notice").on("click", (e) => {

				e.preventDefault();

				saveNotice(type);
			});
		}
		else {

		}

		debugger;
	}

	window.gridCallbacks["noticeList_selectRow"] = selectRow;
	
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

