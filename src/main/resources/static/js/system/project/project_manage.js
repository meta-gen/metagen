$(document).ready(function () {
    const tableId = 'projectMemberGrid';
    const crudActions = 'and';
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

        $('#' + tableId + ' thead tr').empty();
        window.grid(tableId, dataUrl, crudActions, initCallback);
    });

    // 초기 세팅
    const isActive = updateProjectSelectorStyle();
    applyUIBasedOnActiveStatus(isActive);
});
