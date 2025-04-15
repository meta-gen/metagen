import {getCsrfToken, setupAjaxCsrf} from "../../common/csrf.js";

$(document).ready(function () {
	
	setupAjaxCsrf();
	
	const $addBtn = $('#btn-notice-add');
	const tableId = "noticeList";
	
	/** 공지사항 저장버튼 클릭 이벤트
	 *   - 공지사항 저장버튼 클릭 시 수행되는 동작 정의
	 */
	$("#grd-add-noticeList").on("click", function (e) {
		
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
		
	    return `<form id="editTableForm" class="edit-form">
	        <input type="hidden" name="id" value="${rowData.id ?? 0}"/>
	        <input type="hidden" name="type" value="${type}" />
	        
	        <div class="form-group">
	            <label for="tableName">테이블명</label>
	            <input type="text" class="form-control" name="tableName" id="tableName" value="${rowData.tableName ?? ''}" required/>
	        </div>

	        <button type="submit" class="btn btn-primary" id="btn-save-table">저장</button>
	    </form>`
	}
	
	
	/**
	 * 추가버튼 클릭 이벤트
	 */
	$("#grd-add-noticeGrid").on("click", function() {
		
	    selectProjectMemberDialog();
	});
	
	
	function selectRow(rowData, columnList, isManager, tableId) {
		
		// 매니저일 경우
		if(isManager) {
			
			const type = "U";
			const form = createForm({}, type);
			
			window.openDialog('div', { title: type === 'C' ? '공지사항 등록' : '공지사항 수정', content: form });
			
			$("#btn-save-table").on("click", (e) => {
				
				e.preventDefault();
				
				debugger;
			});
		}
		else {
			
		}
		
		debugger;
	}
	
	window.gridCallbacks["noticeList_selectRow"] = selectRow;
});


