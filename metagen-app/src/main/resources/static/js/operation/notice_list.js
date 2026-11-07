import { setupAjaxCsrf } from "../common/csrf.js";
import { registerRowDecorator, setLabel, setUser } from "../common/grid_cells.js";
import { openNoticeDetail, openNoticeSave } from "../popup/notice_popup.js";

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
	 * 저장 후 완료 메세지 출력.
	 */
	function noticeSaveSuccess() {
	    openAlert("정상적으로 공지사항이 저장되었습니다.", () => {
	        window.searchGrid(tableId);
	    });
	}


	/** 추가 버튼 이벤트
	 *  공지사항 등록 팝업을 띄운다.
	 */
	$("#grd-add-noticeList").on("click", () => {
	    openNoticeSave({ type: "add", onSuccess: noticeSaveSuccess });
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
		// 매니저일 경우 수정, 아니면 상세보기 (닫으면 읽음 표시를 갱신한다)
		if (isManager) {
			openNoticeSave({ type: "modify", id: rowData.id, onSuccess: noticeSaveSuccess });
		} else {
			openNoticeDetail(rowData.id, { onClose: () => window.searchGrid(tableId) });
		}
	}

	window.gridCallbacks["noticeList_selectRow"] = selectRow;

});
