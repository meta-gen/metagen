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
                questions: item.questions.filter(q => q.toLowerCase().includes(keyword))
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

        const div = document.createElement("div");
        div.className = `tab-pane fade ${index === 0 ? 'show active' : ''}`;
        div.id = tabId;

        const ul = document.createElement("ul");
        item.questions.forEach(q => {
            const li = document.createElement("li");
            li.textContent = q;
            li.className = "mb-2";
            ul.appendChild(li);
        });

        div.appendChild(ul);
        tabContent.appendChild(div);
    });
}
