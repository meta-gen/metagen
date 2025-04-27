import {setupAjaxCsrf} from "../common/csrf.js";

$(document).ready(function () {
    setupAjaxCsrf();

    function receiveColumnDetailData(data) {
        if (!Array.isArray(data) || data.length === 0) return;

        const container = $("#column-table-container");
        container.empty();

        $("#table-name").html(`<strong>${data[0].tableName}</strong>`);
        const cardList = $('<div class="column-card-list"></div>');

        const rows = [
            [
                { label: "컬럼 설명", key: "columnDesc" },
                { label: "데이터 타입", key: "dataType" },
                { label: "최대 길이", key: "maxLength" },
                { label: "PK 여부", key: "isPk" }
            ],
            [
                { label: "기본값", key: "defaultValue" },
                { label: "NULL 허용", key: "isNullable" },
                { label: "필수 입력", key: "isRequired" },
                { label: "암호화 여부", key: "isEncrypted" }
            ],
            [
                { label: "정렬 순서", key: "sortOrder" },
                { label: "참조 테이블명", key: "refTableName" },
                { label: "예시", key: "example" },
                { label: "민감정보 여부", key: "isSensitive" }
            ]
        ];

        data.forEach((col, idx) => {
            const card = $(`<div class="column-card" data-id="${col.id}"></div>`);

            // 컬럼명 + 정렬 버튼
            card.append(`
                <div class="column-row full">
                    <span class="label">${col.columnName}</span>
                    <div class="sort-buttons">
                        <button class="btn-move-up" title="위로">⬆️</button>
                        <button class="btn-move-down" title="아래로">⬇️</button>
                    </div>
                </div>
            `);

            rows.forEach(rowSet => {
                const row = $('<div class="column-grid-row"></div>');
                rowSet.forEach(field => {
                    const value = col[field.key] ?? "-";
                    const highlight = value === 'Y' && ["isRequired", "isPk", "isEncrypted"].includes(field.key) ? 'highlight' : '';
                    row.append(`
                        <div class="pair" data-field="${field.key}">
                            <span class="label">${field.label}</span>
                            <span class="value ${highlight}">${value}</span>
                        </div>
                    `);
                });
                card.append(row);
            });

            cardList.append(card);
        });

        container.append(cardList);
        updateSortOrders(); // 초기 정렬 순서 설정
    }

    window.receiveColumnDetailData = receiveColumnDetailData;

    // 정렬 순서 갱신 함수
    function updateSortOrders() {
        $(".column-card").each((index, card) => {
            const sortOrder = index + 1;
            const pair = $(card).find('.pair[data-field="sortOrder"]');
            if (pair.length) {
                pair.find('.value').text(sortOrder);
            }
        });
    }

    // 정렬 버튼 이벤트
    $(document).on("click", ".btn-move-up", function () {
        const card = $(this).closest(".column-card");
        const prev = card.prev(".column-card");
        if (prev.length) {
            card.insertBefore(prev);
            updateSortOrders();
        }
    });

    $(document).on("click", ".btn-move-down", function () {
        const card = $(this).closest(".column-card");
        const next = card.next(".column-card");
        if (next.length) {
            card.insertAfter(next);
            updateSortOrders();
        }
    });

    /**
     * 저장버튼을 클릭하여 정렬순서를 저장한다.
     */
    $("#save-sort-order").on("click", () => {
        const sortData = [];

        $(".column-card").each((index, el) => {
            const id = $(el).data("id");
            const sortOrder = index + 1;

            sortData.push({
                id: id,
                sortOrder: sortOrder
            });
        });

        console.log("정렬 순서 저장 데이터:", sortData);

        $.ajax({
            url: "/api/saveColumn/sortOrder",
            type: "PATCH",
            data: JSON.stringify(sortData),
            success: (response) => {
                if(response.result){
                    // 부모로 데이터 전달
                    if (window.opener && typeof window.opener.popupFunction?.columnManageGridSuccess === 'function') {
                        window.opener.popupFunction.columnManageGridSuccess();
                    } else {
                        alert("부모 창과의 통신이 불가능합니다.");
                    }

                    window.close();
                }
            }
        })
    });
});
