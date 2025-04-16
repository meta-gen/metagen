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

        item.questions.forEach(q => {
            const questionDiv = document.createElement("div");
            questionDiv.style.cursor = "pointer";
            questionDiv.style.fontWeight = "bold";
            questionDiv.style.marginTop = "15px";
            questionDiv.textContent = q.question;

            const answerDiv = document.createElement("div");
            answerDiv.textContent = q.answer;
            answerDiv.style.display = "none";
            answerDiv.style.marginLeft = "15px";
            answerDiv.style.marginTop = "5px";
            answerDiv.style.color = "#555";

            questionDiv.addEventListener("click", () => {
                answerDiv.style.display = (answerDiv.style.display === "none") ? "block" : "none";
            });

            div.appendChild(questionDiv);
            div.appendChild(answerDiv);
        });

        tabContent.appendChild(div);
    });
}

