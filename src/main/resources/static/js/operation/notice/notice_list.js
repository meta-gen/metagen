import { getCsrfToken, setupAjaxCsrf } from "../../common/csrf.js";

$(document).ready(function() {

	setupAjaxCsrf();

	const $addBtn = $('#btn-notice-add');
	const tableId = "noticeList";

	/** 공지사항 저장버튼 클릭 이벤트
	 *   - 공지사항 저장버튼 클릭 시 수행되는 동작 정의
	 
	$("#grd-add-noticeList").on("click", function(e) {

		const type = "C";
		const form = createForm({}, type);

		window.openDialog('div', { title: type === 'C' ? '공지사항 등록' : '공지사항 수정', content: form });

		$("#btn-save-table").on("click", (e) => {

			e.preventDefault();

			debugger;
		});
	});
	*/
	
	/** 추가 버튼 이벤트
	 *  공지사항 등록 팝업을 띄운다.
	 */
	$("#grd-add-noticeList").on("click", () => {
		
	    const projectId = $('#projectSelector').val();

	    // 팝업 열기
	    const popup = window.open(
			
	        `/popup/noticePopupSave?projectId=${projectId}&type=add`  // 전달 파라미터
	      , "공지사항 등록/수정"
	      , "width=800,height=900,resizable=yes,scrollbars=yes"
	    );
	});


	// 데이터 상세 내용 확인
	function selectRow(rowData, columnList, isManager, tableId) {

		// 매니저일 경우
		if (isManager) {
			
			const popup = window.open(
				
			        "/popup/noticePopupSave",  // 팝업으로 띄울 URL
			        "공지사항 등록",     // 팝업 이름 (중복 방지용)
			        "width=700,height=800,resizable=yes,scrollbars=yes"
			    );
		}
		else {

			const popup = window.open(
				
			        "/popup/noticePopupDetail",  // 팝업으로 띄울 URL
			        "공지사항 상세보기",     // 팝업 이름 (중복 방지용)
			        "width=700,height=800,resizable=yes,scrollbars=yes"
			    );
		}

		debugger;
	}

	window.gridCallbacks["noticeList_selectRow"] = selectRow;
	
});
