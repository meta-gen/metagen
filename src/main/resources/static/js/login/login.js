document.addEventListener("DOMContentLoaded", function () {
    function fetchProjects(username) {
        if (!username) {
            console.warn("사용자 아이디가 입력되지 않았습니다.");
            return $.Deferred().resolve([]); // 빈 배열 반환 (Promise와 동일한 기능)
        }

        return $.ajax({
            url: `/api/checkProjectList/${username}`,
            type: "GET",
            dataType: "json",
            error: function (xhr, status, error) {
                console.error("프로젝트 리스트를 가져오는 데 실패했습니다.", error);
            }
        });
    }

    function openProjectDialog() {
        const username = document.getElementById("username").value.trim();

        if (!username) {
            openAlert("아이디를 입력하세요.");
            return;
        }

        fetchProjects(username).then(projects => {
            if (projects.length === 0) {
                openAlert("해당 아이디로 접근 가능한 프로젝트가 없습니다.");
                return;
            }

            if(projects.length === 1){
                openConfirm(`${projects[0].projectName} 프로젝트로 로그인됩니다.`, () => {
                    $("#project_id").val(projects[0].id);
                    $("#loginForm").submit(); // ✅ 자동 로그인 (폼 제출)
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
                document.getElementById("projectId").value = this.value;
            });

            const dialogContent = document.createElement("div");
            dialogContent.appendChild(select);

            openDialog("div", { title: "프로젝트 선택", content: dialogContent }, () => {
                $("#project-btn").hide();
                $("#login-btn").css("visibility", "visible");
                document.getElementById("project_id").value = document.getElementById("projectSelect").value;
            });
        });
    }

    window.openProjectDialog = openProjectDialog;

    function beforeLogin() {
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
