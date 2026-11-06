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

            const select = document.createElement("select");
            select.classList.add("form-control");
            select.id = "projectSelect";

            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.textContent = "프로젝트를 선택하세요";
            defaultOption.selected = true;
            defaultOption.disabled = true;
            select.appendChild(defaultOption);

            projects.forEach(project => {
                const option = document.createElement("option");
                option.value = project.id;
                option.textContent = project.projectName;
                select.appendChild(option);
            });

            select.addEventListener("change", function () {
                const selectedOption = this.options[this.selectedIndex];

                if(selectedOption.value === ""){
                    return;
                }

                document.getElementById("project_id").value = selectedOption.value;
                localStorage.setItem("selectedProject", selectedOption.text);

                // 선택한 프로젝트로 바로 로그인
                closeDialog("div", false);
                $("#loginForm").submit();
            });

            const dialogContent = document.createElement("div");
            dialogContent.appendChild(select);

            openDialog("div", { title: "프로젝트 선택", content: dialogContent });
        });
    }

    window.openProjectDialog = openProjectDialog;

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
