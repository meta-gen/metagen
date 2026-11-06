import {getCsrfToken} from "../common/csrf.js";

document.addEventListener("DOMContentLoaded", function () {

    const loginForm = document.getElementById("loginForm");

    // 로그인 실패로 돌아온 경우 이전에 입력한 아이디 복원
    if (new URLSearchParams(window.location.search).has("error")) {
        document.getElementById("username").value = sessionStorage.getItem("loginUsername") || "";
    }
    sessionStorage.removeItem("loginUsername");

    // 버튼 클릭 / Enter 입력 시 바로 제출하지 않고 프로젝트 선택 다이얼로그를 연다
    // (프로젝트 선택 후 $("#loginForm").submit()으로 제출되며, 이 경우 submit 이벤트는 발생하지 않음)
    loginForm.addEventListener("submit", function (event) {
        event.preventDefault(); // 폼 제출 막기
        openProjectDialog(); // 프로젝트 선택 다이얼로그 열기
    });

    /**
     * 1. 로그인 정보(아이디/비밀번호) 확인 → 2. 접근 가능한 프로젝트 조회
     * 로그인 정보가 틀리거나 접근 가능한 프로젝트가 없으면 서버가 오류 메시지를 반환한다.
     */
    function fetchLoginProjects(username, password) {
        return $.ajax({
            url: "/api/login/projects",
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify({ username, password }),
            headers: {
                "X-XSRF-TOKEN": getCsrfToken(),
                "X-Requested-With": "XMLHttpRequest"
            }
        });
    }

    /**
     * 프로젝트를 선택하며, 선택할 프로젝트가 한가지라면 바로 로그인할 수 있도록 도와준다.
     * localStorage에 프로젝트 명을 입력한다 -> 실제로 서비스에 영향을 주지 않으며, Header에만 저장되므로, localStorage에 저장하는것이 문제가 되지 않는다.
     */
    function openProjectDialog() {
        const username = document.getElementById("username").value.trim();

        if (!username) {
            openAlert("아이디를 입력하세요.");
            return;
        }

        if (!document.getElementById("password").value) {
            openAlert("패스워드를 입력하세요.");
            return;
        }

        const password = document.getElementById("password").value;

        fetchLoginProjects(username, password).fail(xhr => {
            // 초기 비밀번호(admin/admin, 관리자 초기화) 사용 중 : 비밀번호를 바꾸기 전에는 로그인할 수 없다
            if (xhr.responseJSON?.code === "PASSWORD_CHANGE_REQUIRED") {
                openChangePasswordDialog(username, password);
                return;
            }
            openAlert(xhr.responseJSON?.message || "로그인 정보를 확인하는 중 문제가 발생했습니다.");
        }).done(projects => {
            if(projects.length === 1){
                openConfirm(`${projects[0].projectName} 프로젝트로 로그인됩니다.`, () => {
                    $("#project_id").val(projects[0].id);
                    localStorage.setItem("selectedProject", projects[0].projectName);
                    $("#loginForm").submit(); // 자동 로그인 (폼 제출)
                });

                return
            }

            // 프로젝트 목록 (GitHub 저장소 선택 목록 형태) : 클릭 시 해당 프로젝트로 바로 로그인
            const repoIcon = '<svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M2 2.5A2.5 2.5 0 0 1 4.5 0h8.75a.75.75 0 0 1 .75.75v12.5a.75.75 0 0 1-.75.75h-2.5a.75.75 0 0 1 0-1.5h1.75v-2h-8a1 1 0 0 0-.714 1.7.75.75 0 1 1-1.072 1.05A2.495 2.495 0 0 1 2 11.5Zm10.5-1h-8a1 1 0 0 0-1 1v6.708A2.486 2.486 0 0 1 4.5 9h8Z"/></svg>';
            const goIcon = '<svg class="auth-project-go" viewBox="0 0 16 16" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M8.22 2.97a.75.75 0 0 1 1.06 0l4.25 4.25a.75.75 0 0 1 0 1.06l-4.25 4.25a.751.751 0 0 1-1.042-.018.751.751 0 0 1-.018-1.042l2.97-2.97H3.75a.75.75 0 0 1 0-1.5h7.44L8.22 4.03a.75.75 0 0 1 0-1.06Z"/></svg>';

            const list = document.createElement("ul");
            list.className = "auth-project-list";
            list.id = "projectSelect";

            projects.forEach(project => {
                const item = document.createElement("li");
                const button = document.createElement("button");
                button.type = "button";
                button.innerHTML = repoIcon + goIcon;

                const name = document.createElement("span");
                name.textContent = project.projectName;
                button.insertBefore(name, button.lastChild);

                button.addEventListener("click", () => {
                    document.getElementById("project_id").value = project.id;
                    localStorage.setItem("selectedProject", project.projectName);

                    // 선택한 프로젝트로 바로 로그인
                    closeDialog("div", false);
                    $("#loginForm").submit();
                });

                item.appendChild(button);
                list.appendChild(item);
            });

            const guide = document.createElement("p");
            guide.className = "gh-dialog-text";
            guide.textContent = "로그인할 프로젝트를 선택하세요.";

            const dialogContent = document.createElement("div");
            dialogContent.append(guide, list);

            openDialog("div", { title: "프로젝트 선택", content: dialogContent });
        });
    }

    window.openProjectDialog = openProjectDialog;

    /**
     * 초기 비밀번호 변경 다이얼로그 (로그인 전)
     * 변경에 성공하면 새 비밀번호로 로그인을 이어서 진행한다.
     */
    function openChangePasswordDialog(username, password) {
        const content = $(`
            <form id="initialPasswordForm" autocomplete="off">
                <div class="gh-flash gh-flash-attention">
                    <svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M6.457 1.047c.659-1.234 2.427-1.234 3.086 0l6.082 11.378A1.75 1.75 0 0 1 14.082 15H1.918a1.75 1.75 0 0 1-1.543-2.575Zm1.763.707a.25.25 0 0 0-.44 0L1.698 13.132a.25.25 0 0 0 .22.368h12.164a.25.25 0 0 0 .22-.368Zm.53 3.996v2.5a.75.75 0 0 1-1.5 0v-2.5a.75.75 0 0 1 1.5 0ZM9 11a1 1 0 1 1-2 0 1 1 0 0 1 2 0Z"/></svg>
                    <span>초기 비밀번호를 사용 중입니다. 비밀번호를 변경해야 로그인할 수 있습니다.</span>
                </div>
                <div class="form-group">
                    <label for="initialNewPassword">새 비밀번호</label>
                    <input type="password" class="form-control" id="initialNewPassword" autocomplete="new-password" required>
                </div>
                <div class="form-group">
                    <label for="initialNewPassword2">새 비밀번호 확인</label>
                    <input type="password" class="form-control" id="initialNewPassword2" autocomplete="new-password" required>
                    <p class="auth-field-note">8자 이상, 대문자와 특수문자를 각각 하나 이상 포함해야 합니다.</p>
                </div>
                <div class="gh-dialog-actions">
                    <button type="submit" class="btn btn-primary">변경하고 로그인</button>
                </div>
            </form>
        `);

        content.on("submit", function (event) {
            event.preventDefault();

            const newPassword = $("#initialNewPassword").val();

            if (!newPassword) {
                openAlert("새 비밀번호를 입력하세요.");
                return;
            }
            if (newPassword !== $("#initialNewPassword2").val()) {
                openAlert("새 비밀번호가 일치하지 않습니다.");
                return;
            }
            if (newPassword === password || newPassword === username) {
                openAlert("새 비밀번호는 현재 비밀번호 및 아이디와 달라야 합니다.");
                return;
            }

            $.ajax({
                url: "/api/login/password",
                type: "POST",
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify({ username, password, newPassword }),
                headers: {
                    "X-XSRF-TOKEN": getCsrfToken(),
                    "X-Requested-With": "XMLHttpRequest"
                }
            }).fail(xhr => {
                openAlert(xhr.responseJSON?.message || "비밀번호를 변경하는 중 문제가 발생했습니다.");
            }).done(() => {
                closeDialog("div", false);

                // 새 비밀번호로 로그인 계속 진행 (프로젝트 선택)
                document.getElementById("password").value = newPassword;
                // (알림 닫기 콜백이 끝난 뒤 다음 다이얼로그를 열어야 콜백이 초기화되지 않는다)
                openAlert("비밀번호가 변경되었습니다. 로그인할 프로젝트를 선택합니다.", () => setTimeout(openProjectDialog, 0));
            });
        });

        openDialog("div", { title: "비밀번호 변경", content: content });
        setTimeout(() => $("#initialNewPassword").trigger("focus"), 0);
    }

    function beforeLogin() {

        // 로그인 실패 시 아이디를 복원하기 위해 저장
        sessionStorage.setItem("loginUsername", document.getElementById("username").value.trim());

        if(!$("#project_id").val()) return;

        const username = document.getElementById("username").value.trim();
        const projectId = document.getElementById("project_id").value.trim();

        if (!username) {
            openAlert("아이디를 입력하세요.");
            return false;
        }

        if (!projectId) {
            openAlert("프로젝트를 선택하세요.");
            return false;
        }

        return true; // 프로젝트가 선택된 상태에서 로그인 진행
    }

    window.beforeLogin = beforeLogin;
});
