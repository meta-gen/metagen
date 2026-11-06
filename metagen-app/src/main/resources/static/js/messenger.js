import {getCsrfToken} from "./common/csrf.js";

/**
 * 메신저 (오른쪽 하단 스낵바)
 * - 스낵바 클릭 : 로그인 사용자 목록 열기 (새 메시지가 있으면 빨간 표시)
 * - 사용자 클릭 : 채팅 화면으로 이동, 뒤로가기(←) 클릭 시 사용자 목록으로 복귀
 */
let stompClient = null;
let currentTarget = null; // 현재 채팅 중인 사용자 id

$(document).ready(function () {
    if ($("#messenger").length === 0) return; // 비로그인 화면

    connectChat();

    fetchActiveUsers(); // 최초 1회
    setInterval(fetchActiveUsers, 30_000); // 30초마다 갱신

    // 스낵바 → 패널 열기
    $("#messenger-snackbar").on("click", function () {
        $(this).hide();
        $("#messenger-panel").show();
    });

    // 패널 → 스낵바로 접기
    $(".messenger-minimize").on("click", function () {
        $("#messenger-panel").hide();
        $("#messenger-snackbar").show();
    });

    // 채팅 → 사용자 목록으로 복귀
    $("#chat-back").on("click", showUserList);

    $("#chat-form").on("submit", function (e) {
        e.preventDefault();
        sendChatMessage();
    });

    $("#inactive-toggle").on("click", function () {
        const $arrow = $("#toggle-arrow");

        $("#user-list-inactive").slideToggle(150); // 부드럽게 열리고 닫힘
        $arrow.text($arrow.text() === "▲" ? "▼" : "▲");
    });
});

function getLoginUserId() {
    return $("#hidden-user-id").val();
}

function ajaxHeaders(xhr) {
    xhr.setRequestHeader('X-XSRF-TOKEN', getCsrfToken());
    xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
    xhr.setRequestHeader('Content-Type', 'application/json');
}

/**
 * WebSocket 연결 후 내 채널을 한 번만 구독한다.
 */
function connectChat() {
    if (typeof Stomp === 'undefined') {
        console.error('STOMP 라이브러리가 로딩되지 않았습니다.');
        return;
    }

    stompClient = Stomp.over(new SockJS("/ws-chat"));
    stompClient.debug = null; // 콘솔 로그 비활성화

    stompClient.connect({}, function () {
        stompClient.subscribe("/sub/chat/" + getLoginUserId(), function (messageOutput) {
            const message = JSON.parse(messageOutput.body);

            if (message.from === currentTarget) {
                // 보고 있는 채팅이면 바로 표시하고 읽음 처리
                appendMessage(message.from, message.content);
                markAsRead(message.from);
            } else {
                fetchActiveUsers(); // 읽지 않은 메시지 표시 갱신
            }
        });
    });
}

/**
 * 로그인 / 비로그인 사용자 목록 조회
 */
function fetchActiveUsers() {
    $.ajax({
        url: "/api/activeUsers",
        method: "GET",
        beforeSend: ajaxHeaders,
        complete: function () {
            // 로딩바 없이 작업수행
        },
        success: function (response) {
            const activeUsers = (response.activeUsers || []).filter(user => user.isMyData !== "true");
            const inactiveUsers = response.inactiveUsers || [];

            renderUserList($("#user-list-active"), activeUsers, user => `${user.project} (${user.role})`);
            renderUserList($("#user-list-inactive"), inactiveUsers, user => user.username);

            $("#messenger-active-count").text(activeUsers.length);

            const hasUnread = activeUsers.concat(inactiveUsers).some(user => user.badge !== "none");
            $("#messenger-unread").toggle(hasUnread);
        }
    });
}

function renderUserList($list, users, getSubText) {
    $list.empty();

    if (users.length === 0) {
        $list.append($("<li></li>").addClass("user-empty").text("사용자가 없습니다."));
        return;
    }

    users.forEach(user => {
        const $li = $("<li></li>").addClass("user-item").attr("data-id", user.id)
            .append($("<span></span>").addClass("name").text(user.name))
            .append($("<span></span>").addClass("sub").text(getSubText(user)))
            .append($("<span></span>").addClass("new-badge").toggle(user.badge !== "none"));

        $li.on("click", () => openChat(user));

        $list.append($li);
    });
}

/**
 * 채팅 화면 열기
 */
function openChat(user) {
    currentTarget = user.id;

    $("#chat-target").text(`${user.name}님과의 채팅`);
    $("#chat-messages").empty();
    $("#messenger-users").hide();
    $("#chat-box").show();
    $("#chat-input").trigger("focus");

    loadChatHistory(user.id);
    markAsRead(user.id);
}

/**
 * 채팅 → 로그인 사용자 목록
 */
function showUserList() {
    currentTarget = null;

    $("#chat-box").hide();
    $("#messenger-users").show();

    fetchActiveUsers();
}

function sendChatMessage() {
    const content = $("#chat-input").val();
    if (!content.trim() || !stompClient || !currentTarget) return;

    const userId = getLoginUserId();

    stompClient.send("/pub/chat.send", {}, JSON.stringify({
        from: userId,
        to: currentTarget,
        content: content,
        timestamp: new Date()
    }));

    $("#chat-input").val("");
    appendMessage(userId, content);
}

function appendMessage(sender, content) {
    const className = sender === getLoginUserId() ? "me" : "you";

    // 메시지는 text로 넣어 HTML이 실행되지 않도록 한다
    $("#chat-messages").append($("<div></div>").addClass(`chat-message ${className}`).text(content));

    const chatMessages = document.getElementById("chat-messages");
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

/**
 * 해당 사용자가 보낸 메시지를 읽음 처리
 */
function markAsRead(sender) {
    $.ajax({
        url: `/api/chat/history/${sender}`,
        type: "DELETE",
        beforeSend: ajaxHeaders,
        complete: function () {
            // 로딩바 없이 작업수행
        }
    });
}

function loadChatHistory(user) {
    $.ajax({
        url: `/api/chat/history?user=${user}`,
        type: "GET",
        beforeSend: ajaxHeaders,
        complete: function () {
            // 로딩바 없이 작업수행
        },
        success: function (messages) {
            messages.forEach(msg => appendMessage(msg.sender, msg.content));
        }
    });
}
