import { setupAjaxCsrf } from "../common/csrf.js";
import { registerRowDecorator, setLabel, setUser } from "../common/grid_cells.js";

/**
 * 공지사항 행 꾸미기 : 안 읽은 글은 파란 점 + 굵은 제목 (GitHub 알림 목록 형태)
 */
registerRowDecorator("noticeList", (row, data, cellOf) => {
	const isUnread = data.isHit !== "확인";
	row.classList.toggle("notice-unread", isUnread);

	const titleCell = cellOf("title");
	if (titleCell) {
		const wrap = document.createElement("span");
		wrap.className = "notice-title";

		const dot = document.createElement("span");
		dot.className = "notice-dot";
		dot.title = isUnread ? "미확인" : "확인";

		const text = document.createElement("span");
		text.className = "notice-title-text";
		text.textContent = data.title ?? "";

		wrap.append(dot, text);
		titleCell.replaceChildren(wrap);
	}

	setUser(cellOf("username"), data.username);

	if (isUnread) setLabel(cellOf("isHit"), "새 글", "accent");
	else setLabel(cellOf("isHit"), "읽음");
});

$(document).ready(function() {

	setupAjaxCsrf();

	const tableId = "noticeList";

	/**
	 * 팝업 닫은 후 완료 메세지 출력.
	 */
	function noticeSaveSuccess() {
		
	    openAlert("정상적으로 공지사항이 저장되었습니다.", () => {
			
	        window.searchGrid(tableId);
	    });
	}
	
	window.popupFunction = window.popupFunction || {}; // 혹시 없을 경우 방지
	window.popupFunction['noticeSaveSuccess'] = noticeSaveSuccess;

	
	/** 추가 버튼 이벤트
	 *  공지사항 등록 팝업을 띄운다.
	 */
	$("#grd-add-noticeList").on("click", () => {
	    // 팝업 열기
	    const popup = window.open(
			
	        `/popup/noticePopupSave?type=add`  // 전달 파라미터
	      , "공지사항 등록/수정"
	      , "width=800,height=900,resizable=yes,scrollbars=yes"
	    );
	});

	$("#grd-delete-noticeList").on("click", function() {
		const checkedData = getCheckedDataIsNonNull(tableId);

		if(!checkedData) return;

		window.openConfirm("선택한 공지사항을 삭제하시겠습니까?", () => {
			$.ajax({
				url: "/api/deleteNotice",
				type : "DELETE",
				data: JSON.stringify(checkedData),
				success: (response) => {
					if(response.result){
						openAlert("선택한 공지사항이 삭제되었습니다.", () => {
							window.searchGrid(tableId);
						});
					}
				}
			})
		})
	});


	// 데이터 상세 내용 확인
	function selectRow(rowData, columnList, isManager, tableId) {
		// 매니저일 경우
		if (isManager) {
			
			const popup = window.open(
				
			        `/popup/noticePopupSave?id=${rowData.id}&type=modify`,  // 팝업으로 띄울 URL
			        "공지사항 수정",     // 팝업 이름 (중복 방지용)
			        "width=700,height=800,resizable=yes,scrollbars=yes"
			    );
		}
		else {

			const popup = window.open(
				
			        `/popup/noticePopupDetail/${rowData.id}`,  // 팝업으로 띄울 URL
			        "공지사항 상세보기",     // 팝업 이름 (중복 방지용)
			        "width=700,height=800,resizable=yes,scrollbars=yes"
			    );
		}
	}

	window.gridCallbacks["noticeList_selectRow"] = selectRow;
	
});
