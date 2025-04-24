import {getCsrfToken} from "./common/csrf.js";

$(document).ready(function(){
    const $activeUserList = $("#active-user-list");

    if ($activeUserList.length > 0 && $activeUserList.is(":visible")) {
        fetchActiveUsers(); // 최초 1회
        setInterval(fetchActiveUsers, 30_000); // 30초마다 갱신
    }else if($("#sidebarToggleBtn").length > 0){
        // 토글 버튼을 클릭해서 모바일로 접근했을 때
        $("#sidebarToggleBtn").on("click", function () {
            const sidebar = $(".sidebar");

            if(sidebar.length > 0 && sidebar.is(":visible")){
                fetchActiveUsers();
            }
        });
    }
});

function fetchActiveUsers() {
    $.ajax({
        url: "/api/activeUsers",
        method: "GET",
        beforeSend: function(xhr, settings) {
            xhr.setRequestHeader('X-XSRF-TOKEN', getCsrfToken());
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
            xhr.setRequestHeader('Content-Type', 'application/json');
        },
        complete: function () {
            // 로딩바 없이 작업수행
        },
        success: function (response) {
            const $activeList = $("#user-list-active");
            const $inactiveList = $("#user-list-inactive");

            $activeList.empty();
            $inactiveList.empty();

            (response.activeUsers || []).forEach(user => {
                const $li = $(`
                    <li class="user-item">
                        ${user.name} ${user.project}(${user.role})
                    </li>
                `);

                $li.on("click", function () {
                    console.log("메시지 대상:", user);
                });

                $("#user-list-active").append($li);
            });

            (response.inactiveUsers || []).forEach(user => {
                const $li = $(`
                    <li class="user-item">
                        ${user.name} (${user.username})
                    </li>
                `);

                $("#user-list-inactive").append($li);
            });
        }
    });
}

