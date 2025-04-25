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

    $("#inactive-toggle").on("click", function () {
        const $list = $("#user-list-inactive");
        const $arrow = $("#toggle-arrow");

        $list.slideToggle(150); // 부드럽게 열리고 닫힘
        const isOpen = $arrow.text() === "▲";
        $arrow.text(isOpen ? "▼" : "▲");
    });
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
                let li;

                if(user.isMyData === "true"){
                    li = $(`
                            <li class="user-my-item">
                                ${user.name} ${user.project}(${user.role})
                            </li>
                        `);
                }else{
                   li = $(`
                            <li class="user-item">
                                ${user.name} ${user.project}(${user.role})
                            </li>
                        `);
                }

                li.on("click", function () {
                    submitMessage(user);
                });

                $("#user-list-active").append(li);
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

function submitMessage(user){
    if(user.isMyData === "false"){
        console.log(user);
    }
}
