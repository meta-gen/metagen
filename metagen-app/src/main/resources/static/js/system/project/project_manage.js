import {getCsrfToken, setupAjaxCsrf} from "../../common/csrf.js";

$(document).ready(function () {
    setupAjaxCsrf();

    const tableId = 'projectMemberGrid';
    const initCallback = null;

    const $selector = $('#projectSelector');
    const $addBtn = $('#btn-project-add');
    const $editBtn = $('#btn-project-edit');
    const $deleteBtn = $('#btn-project-delete');
    const $gridContainer = $('#gridContainer');

    function updateProjectSelectorStyle() {
        const $selectedOption = $selector.find('option:selected');
        const isActive = $selectedOption.data('active');

        const text = $selectedOption.text();
        if (isActive === false || isActive === "false") {
            if (!text.includes('(비활성)')) {
                $selectedOption.text(text + ' (비활성)');
            }
        } else {
            $selectedOption.text(text.replace(' (비활성)', ''));
        }

        return isActive;
    }

    function applyUIBasedOnActiveStatus(isActive) {
        const isInactive = isActive === false || isActive === "false";

        // 등록, 삭제 비활성화 여부
        $addBtn.prop('disabled', isInactive);
        $deleteBtn.prop('disabled', isInactive);

        // 수정 버튼은 항상 활성화
        $editBtn.prop('disabled', false);

        // 그리드 상태
        $gridContainer.css({
            'pointer-events': isInactive ? 'none' : 'auto',
            'opacity': isInactive ? '0.5' : '1'
        });
    }

    /**
     * 프로젝트 멤버 추가 Dialog를 동적으로 추가한다.
     */
    function selectProjectMemberDialog(){

        const selectedId = $("#projectSelector").val();

        $.ajax({
            url : `/api/getProject/projectMember/${selectedId}`,
            type : "GET",
            success : (response) => {
                if(response.result){
                    const userList = response.projectMember;

                    const $select = $('<select class="form-control" id="addProjectMemberSelect">');
                    $select.append('<option value="" disabled selected>추가할 사용자를 선택하세요</option>');

                    userList.forEach(user => {
                        const option = $('<option>')
                            .val(user.id)
                            .text(user.name);
                        $select.append(option);
                    });

                    // 저장 버튼 생성
                    const $saveBtn = $('<button type="button" class="btn btn-primary mt-3">저장</button>');

                    // 저장 버튼 이벤트
                    $saveBtn.on('click', function () {
                        const selectedId = $select.val();
                        saveProjectMember(selectedId);
                    });

                    const $container = $('<div>')
                        .append($select)
                        .append($saveBtn);

                    window.openDialog("div", {
                        title: "프로젝트 멤버 추가",
                        content: $container
                    });
                }
            }
        });
    }

    /**
     * 프로젝트 멤버를 추가한다.
     * @param selectedId
     */
    function saveProjectMember(selectedId){

        if (!selectedId) {
            openAlert("추가할 사용자를 선택해주세요.");
            return;
        }

        const projectId = $("#projectSelector").val();

        const url = `/api/saveProject/projectMember/${projectId}`;

        $.ajax({
            url : url,
            type : "POST",
            data : JSON.stringify({accountId : selectedId}),
            success : (response) => {
                if(response.result){
                    openAlert("프로젝트 멤버가 정상적으로 등록되었습니다.", () => {
                        window.closeDialog("div");
                        window.searchGrid(tableId);
                    });
                }
            }
        })


    }

    /**
     * 프로젝트를 저장한다.
     */
    function saveProject(){
        const $form = $('#editProjectForm');
        const type = $form.find('[name="type"]').val();

        const formData = {
            id: $form.find('[name="id"]').val(),
            projectName: $form.find('[name="projectName"]').val(),
            isActive: $form.find('[name="isActive"]').is(':checked'),
            isAutoActive: $form.find('[name="isAutoActive"]').is(':checked'),
            isDicAbbrUsed : $form.find('[name="isDicAbbrUsed"]').is(':checked'),
            templateType: $('#templateTypes').val()
        };

        if (type === 'U') {
            formData.projectManagerId = $form.find('[name="projectManagerId"]').val();
        }

        const url = `/api/saveProject/project`;
        const ajaxType = type === "C" ? "POST" : "PUT"

        $.ajax({
            url : url,
            type : ajaxType,
            data : JSON.stringify(formData),
            success : (response) => {
                if(response.result){
                    const typeText = type === "C" ? "등록" : "수정";

                    openAlert(`정상적으로 ${typeText}되었습니다.`, () => {
                        location.reload();
                    });
                }
            }
        });
    }

    /**
     * 선택 리스트를 기준으로 승인/승인취소한다.
     * @param isActive
     */
    function saveIsActive(isActive){
        const checkedData = getCheckedDataIsNonNull(tableId);

        openConfirm(`선택한 정보를 ${isActive ? '승인' : '승인취소'} 하시겠습니까?`, () => {
            $.ajax({
                url : `/api/saveProject/active/${isActive}`,
                type : "POST",
                data : JSON.stringify(checkedData),
                success : (response) => {
                    if(response.result){
                        openAlert(`정상적으로 ${isActive ? "승인되어 활성화" : "승인취소되어 비활성화"}되었습니다.`, () => {
                            window.closeDialog("div");
                            window.searchGrid(tableId);
                        });
                    }
                }
            })
        })
    }

    /**
     * 프로젝트 맴버를 삭제처리한다.
     */
    function deleteProjectMember(){
        const checkedData = getCheckedDataIsNonNull(tableId);

        openConfirm('선택한 정보를 삭제하시겠습니까?', () => {
            $.ajax({
                url: "/api/deleteProject/projectMember",
                type: "DELETE",
                data: JSON.stringify(checkedData),
                success: (response) => {
                    if(response.result){
                        openAlert("정상적으로 삭제처리 되었습니다.",() => {
                            window.searchGrid(tableId);
                        });
                    }
                }
            });
        });
    }

    /**
     * 프로젝트 등록/수정 버튼을 클릭 시 다이알로그를 open한다.
     * @param type
     * @param projectData
     * @param saveProject
     */
    function openDialog(type, projectData, saveProject) {
        let managerSelectHtml = '';
        let templateTypeHtml = '';

        if (type === 'U') {
            if(Array.isArray(projectData.projectMembers)){
                const options = projectData.projectMembers.filter(p => p.isActive === 'Y').map(member => {
                    const isSelected = member.username === projectData.projectManagerName ? 'selected' : '';
                    return `<option value="${member.accountId}" ${isSelected}>${member.name}</option>`;
                }).join('');

                managerSelectHtml = `<div class="form-group">
                                    <label for="project-manager">프로젝트 관리자</label>
                                    <select id="project-manager" name="projectManagerId" class="form-control">
                                        ${options}
                                    </select>
                                 </div>`;
            }
        }

        const formHtml = `
        <form id="editProjectForm" class="edit-form">
            <input type="hidden" name="type" value="${type}" />
            <input type="hidden" name="id" value="${projectData.id ?? ''}" />

            <div class="form-group">
                <label for="project-name">프로젝트 명</label>
                <input type="text" id="project-name" name="projectName" class="form-control" value="${projectData.projectName ?? ''}" required/>
            </div>

            ${managerSelectHtml}
            
            <div class="form-check" style="margin-top: 10px; margin-bottom: 10px;">
                <input type="checkbox" id="project-active" name="isActive" class="form-check-input" ${projectData.isActive ? 'checked' : ''} />
                <label class="form-check-label" for="project-active">활성화 여부</label>
            </div>

            <div class="form-check" style="margin-top: 10px; margin-bottom: 10px;">
                <input type="checkbox" id="project-autoactive" name="isAutoActive" class="form-check-input" ${projectData.isAutoActive ? 'checked' : ''} />
                <label class="form-check-label" for="project-autoactive">사용자 자동 승인 여부</label>
            </div>
            
            <div class="form-check" style="margin-top: 10px; margin-bottom: 10px;">
                <input type="checkbox" id="project-dicAbbrUsed" name="isDicAbbrUsed" class="form-check-input" ${projectData.isDicAbbrUsed ? 'checked' : ''} />
                <label class="form-check-label" for="project-dicAbbrUsed">데이터 사전 약어 사용 여부</label>
            </div>

            ${templateTypeHtml}

            ${type === 'C' || projectData.isModified ? '<input type="submit" id="btn-save-project" class="btn btn-primary"/>' : ''}
        </form>
    `;

        window.openDialog('div', { title: type === 'C' ? '프로젝트 등록' : '프로젝트 수정', content: formHtml });

        // 저장 버튼 이벤트
        $("#btn-save-project").on('click', function (e) {
            e.preventDefault();

            if($("#project-name").val() === ""){
                window.openAlert("프로젝트 명은 필수값입니다.");
                return;
            }

            saveProject();
        });
    }

    // 초기 세팅
    const isActive = updateProjectSelectorStyle();
    applyUIBasedOnActiveStatus(isActive);

    $selector.on('change', function () {
        const selectedId = $(this).val();
        if (!selectedId) return;

        const isActive = updateProjectSelectorStyle();
        applyUIBasedOnActiveStatus(isActive);

        const dataUrl = '/api/getProject/' + selectedId;

        if (window.tableInstances[tableId]) {
            window.tableInstances[tableId].destroy();
            delete window.tableInstances[tableId];
        }

        const $selectedOption = $('#projectSelector option:selected');
        const isAutoActive = $selectedOption.data('autoactive');

        let crudActions = 'ancd';

        if(isAutoActive || isAutoActive === "true"){
            crudActions = "cd";
        }

        $('#' + tableId + ' thead tr').empty();
        window.grid(tableId, dataUrl, crudActions, initCallback);
    });

    $("#btn-project-add").on("click", function (e){
        openDialog("C", {}, saveProject, null);
    });

    /**
     * 프로젝트 수정버튼 클릭 시
     */
    $("#btn-project-edit").on("click", function(e){
        e.preventDefault();

        const selectedId = $("#projectSelector").val();

        $.ajax({
            url : `/api/getProject/${selectedId}/getEditProjectData`,
            type : "GET",
            success : (response) => {
                if(response.result){
                    const projectData = response.project;

                    const type = "U"

                    openDialog(type, projectData, saveProject);
                }
            }
        })
    });

    $("#btn-project-delete").on("click", function(){
       window.openConfirm("프로젝트 삭제 시 복구가 불가합니다.\n해당 프로젝트를 삭제하시겠습니까?", function(){
           const selectedId = $("#projectSelector").val();

           if(selectedId === 0){
               openAlert("기본 프로젝트는 삭제가 불가능합니다.");
               return;
           }

           $.ajax({
               url : `/api/deleteProject/project/${selectedId}`,
               type : "DELETE",
               success : (response) => {
                   if(response.result){
                       openAlert("정상적으로 삭제 되었습니다.", () => {
                           location.reload();
                       })
                   }
               }
           })
       });
    });

    /**
     * 승인버튼 클릭 이벤트
     */
    $("#grd-active-projectMemberGrid").on("click", function(){
        saveIsActive(true);
    });

    /**
     * 승인취소 버튼 클릭 이벤트
     */
    $("#grd-unactive-projectMemberGrid").on("click", function(){
        saveIsActive(false);
    });

    /**
     * 삭제버튼 클릭 이벤트
     */
    $("#grd-delete-projectMemberGrid").on("click", function(){
        deleteProjectMember();
    });

    /**
     * 추가버튼 클릭 이벤트
     */
    $("#grd-add-projectMemberGrid").on("click", function(){
        selectProjectMemberDialog();
    });


});


