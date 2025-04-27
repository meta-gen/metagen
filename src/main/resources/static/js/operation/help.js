$(document).ready(function () {
    $.getJSON('/jsons/faq.json', function (data) {
        renderTabs(data);
        renderTabContents(data);

        // 검색 이벤트
        $('#faq-form').on('submit', function (e) {
            e.preventDefault();
            const keyword = $('#faq-search').val().toLowerCase().trim();

            const filtered = data.map(item => ({
                ...item,
                questions: item.questions.filter(q =>
                    q.question.toLowerCase().includes(keyword) ||
                    q.answer.toLowerCase().includes(keyword)
                )
            })).filter(item => item.questions.length > 0);

            renderTabs(filtered);
            renderTabContents(filtered);
        });
    });
});

function renderTabs(data) {
    const tabMenu = document.getElementById("faqTabMenu");
    tabMenu.innerHTML = '';

    data.forEach((item, index) => {
        const tabId = 'tab-' + index;

        const li = document.createElement("li");
        li.className = 'nav-item';
        li.innerHTML = `
            <a class="nav-link ${index === 0 ? 'active' : ''}" data-bs-toggle="tab" href="#${tabId}">
                ${item.category}
            </a>
        `;
        tabMenu.appendChild(li);
    });
}

function renderTabContents(data) {
    const tabContent = document.getElementById("faqTabContent");
    tabContent.innerHTML = '';

    data.forEach((item, index) => {
        const tabId = 'tab-' + index;

        // 1. 각 tab-pane 생성
        const div = document.createElement("div");
        div.className = `tab-pane fade ${index === 0 ? 'show active' : ''}`;
        div.id = tabId;

        // 2. 탭 안에 들어갈 accordion 컨테이너 생성
        const accordionId = `faqAccordion-${index}`;
        const accordion = document.createElement("div");
        accordion.className = "accordion";
        accordion.id = accordionId;

        // 3. 질문 목록 렌더링
        item.questions.forEach((q, qIndex) => {
            const itemId = `${accordionId}-item-${qIndex}`;

            const accordionItem = document.createElement('div');
            accordionItem.className = 'accordion-item';

            accordionItem.innerHTML = `
                <h2 class="accordion-header" id="heading-${itemId}">
                    <button class="accordion-button collapsed" type="button"
                            aria-expanded="false"
                            aria-controls="collapse-${itemId}">
                        <span class="question-label">Q.</span> ${q.question}
                    </button>
                </h2>
                <div id="collapse-${itemId}" class="accordion-collapse collapse"
                     aria-labelledby="heading-${itemId}"
                     data-bs-parent="#${accordionId}">
                    <div class="accordion-body">
                        ${q.answer}
                    </div>
                </div>
            `;

            accordion.appendChild(accordionItem);

            const btn = accordionItem.querySelector('.accordion-button');
            const collapseEl = accordionItem.querySelector('.accordion-collapse');
            const bsCollapse = new bootstrap.Collapse(collapseEl, { toggle: false });

            btn.addEventListener('click', () => {
                if (collapseEl.classList.contains('show')) {
                    bsCollapse.hide();
                    btn.classList.add('collapsed'); // 닫히니까 collapsed 클래스 추가
                } else {
                    bsCollapse.show();
                    btn.classList.remove('collapsed'); // 열리니까 collapsed 제거
                }
            });

            btn.addEventListener('click', () => {
                if (collapseEl.classList.contains('show')) {
                    bsCollapse.hide();
                } else {
                    bsCollapse.show();
                }
            });

            accordion.appendChild(accordionItem);
        });

        // 4. 탭에 accordion 추가 후 tabContent에 추가
        div.appendChild(accordion);
        tabContent.appendChild(div);
    });
}
