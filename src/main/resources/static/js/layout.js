// 메뉴 상태 저장
window.toggleMenu = function (event, menuId, menuLink) {
    event.preventDefault(); // 기본 동작 방지
    const menu = document.getElementById(menuId);
    const isOpen = menu.classList.contains('show');

    // 상태 저장
    sessionStorage.setItem('activeMenu', isOpen ? '' : menuId);

    // 모든 메뉴 닫기
    document.querySelectorAll('.list-group .collapse').forEach(item => item.classList.remove('show'));
    document.querySelectorAll('.list-group a').forEach(link => link.classList.remove('active'));

    if (!isOpen) {
        menu.classList.add('show');
        menuLink.classList.add('active');
    }
};

// 활성화된 메뉴 저장 (1-depth 포함)
window.setActiveMenu = function (event, menuId) {
    event.preventDefault();
    sessionStorage.setItem('activeMenu', menuId);
    sessionStorage.removeItem('activeSubmenu'); // 2-depth 초기화

    // 모든 메뉴 비활성화
    document.querySelectorAll('.list-group a').forEach(link => link.classList.remove('active'));

    // 클릭한 메뉴 활성화
    const menuLink = document.querySelector(`[data-menu-id="${menuId}"]`);
    if (menuLink) {
        menuLink.classList.add('active');
    }

    // 페이지 이동
    window.location.href = event.target.href;
};

window.setActiveSubmenu = function (event, submenuId) {
    event.preventDefault();
    const parentMenuId = event.target.closest('.collapse').id;
    sessionStorage.setItem('activeMenu', parentMenuId);
    sessionStorage.setItem('activeSubmenu', submenuId);

    // 페이지 이동
    window.location.href = event.target.href;
};

window.restoreMenuState = function () {
    const activeMenu = sessionStorage.getItem('activeMenu');
    const activeSubmenu = sessionStorage.getItem('activeSubmenu');

    // 1-depth 활성화
    if (activeMenu) {
        const menu = document.getElementById(activeMenu);
        const menuLink = document.querySelector(`[data-menu-id="${activeMenu}"]`);
        if (menuLink) {
            menuLink.classList.add('active'); // 1-depth 활성화
        }
        if (menu) {
            menu.classList.add('show'); // 메뉴 열기
        }
    }

    // 2-depth 활성화
    if (activeSubmenu) {
        const submenuLink = document.querySelector(`[data-submenu-id="${activeSubmenu}"]`);
        if (submenuLink) {
            submenuLink.classList.add('active'); // 2-depth 활성화
        }
    }
};

// 페이지 로드 후 상태 복구
document.addEventListener('DOMContentLoaded', window.restoreMenuState);

document.addEventListener('DOMContentLoaded', function () {
    // 현재 URL의 경로를 가져옵니다.
    const currentPath = window.location.pathname;

    // 포커스를 제거할 경로 목록
    const excludedPaths = ['/login', '/signup', '/account'];

    // 포커스를 제거할 조건
    if (excludedPaths.includes(currentPath)) {
        // 사이드바에서 active 클래스 제거
        document.querySelectorAll('.list-group a').forEach(item => {
            item.classList.remove('active');
        });

        // 모든 열려 있는 메뉴를 닫습니다.
        document.querySelectorAll('.list-group .collapse').forEach(item => {
            item.classList.remove('show');
        });
    }

    // 다이얼로그 이동에 대한 Function
    makeDialogDraggable("myAlert");
    makeDialogDraggable("myConfirm");
    makeDialogDraggable("mainConfirm", "#mainDialogTitle");
});

function callableFunction() {
}

function openAlert(message, callableFunc){
    openDialog("alert", message, callableFunc);
}

function openConfirm(message, callableFunc){
    openDialog("confirm", message, callableFunc);
}


// 다이얼로그 열기
function openDialog(type, message, callableFunc) {
    const dialog = document.getElementById("alert" === type ? 'myAlert' : "div" === type ? "mainConfirm" : "myConfirm");
    const content = document.getElementById("alert" === type ? 'alertContent' : "div" === type ? "mainDialogTitle" : "confirmContent");

    if (type === "div") {
        content.textContent = message.title;

        // 기존 내용 초기화
        const dialogContent = document.getElementById("mainDialogContent");
        dialogContent.innerHTML = ''; // 기존 내용 삭제

        if (message.content) {
            if (message.content instanceof jQuery) {
                dialogContent.appendChild(message.content.get(0)); // jQuery 객체 → DOM 요소 변환
            } else if (message.content instanceof HTMLElement) {
                dialogContent.appendChild(message.content); // DOM 요소면 그대로 추가
            } else if (typeof message.content === "string") {
                dialogContent.insertAdjacentHTML("beforeend", message.content); // 문자열이면 HTML 삽입
            } else {
                console.error("Invalid content type:", message.content);
            }
        }
    } else if (content) {
        content.textContent = message; // 다이얼로그에 메시지 삽입
    }

    if (dialog) {
        dialog.showModal(); // 다이얼로그 열기
    }

    if (callableFunc) {
        callableFunction = callableFunc;
    }
}

// 다이얼로그 닫기
function closeDialog(type, isCallableStart) {
    const dialog = document.getElementById(
        type === "alert" ? "myAlert" : type === "div" ? "mainConfirm" : "myConfirm"
    );

    if (!dialog) {
        console.error(`Dialog not found for type: ${type}`);
        return;
    }

    // `open` 속성 확인 후 닫기
    if (dialog.open) {
        dialog.close();
    } else {
        console.warn("Dialog is already closed.");
    }

    if (isCallableStart && typeof callableFunction === "function") {
        callableFunction(); // 닫은 이후 추가 함수 호출
    }

    // callableFunction 초기화
    callableFunction = () => {};
}

/**
 * 크롬 #top-layer 생성에 따른 먹통현상 방지 반영
 */
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("dialog").forEach(dialog => {
        if (!document.body.contains(dialog)) {
            document.body.appendChild(dialog); // body 아래로 이동
        }
    });
});

window.closeDialog = closeDialog;

function makeDialogDraggable(dialogId) {
    const dialog = document.getElementById(dialogId);
    if (!dialog) return;

    let isDragging = false;
    let offsetX = 0;
    let offsetY = 0;

    dialog.style.cursor = "move";
    dialog.style.position = "absolute";
    dialog.style.margin = "0";

    dialog.addEventListener("mousedown", function (e) {
        // input, textarea, button은 제외
        const tag = e.target.tagName.toLowerCase();
        if (["input", "textarea", "select", "button", "label"].includes(tag)) return;

        isDragging = true;
        offsetX = e.clientX - dialog.offsetLeft;
        offsetY = e.clientY - dialog.offsetTop;
        document.body.style.userSelect = "none";
    });

    document.addEventListener("mousemove", function (e) {
        if (isDragging) {
            dialog.style.left = `${e.clientX - offsetX}px`;
            dialog.style.top = `${e.clientY - offsetY}px`;
        }
    });

    document.addEventListener("mouseup", function () {
        isDragging = false;
        document.body.style.userSelect = "";
    });
}

function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar.classList.contains('d-none')) {
        sidebar.classList.remove('d-none');
        sidebar.classList.add('d-block');
    } else {
        sidebar.classList.remove('d-block');
        sidebar.classList.add('d-none');
    }
}
