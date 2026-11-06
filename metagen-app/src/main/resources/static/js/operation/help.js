/**
 * 도움말 (FAQ)
 * - /jsons/faq.json : [{ category, questions: [{ question, answer(HTML) }] }]
 * - 분류 탭 + 펼침 목록(<details>) 으로 표시한다. 답변은 관리자가 작성한 정적 HTML 이다.
 */
let faqData = [];
let activeIndex = 0;

$(document).ready(function () {
    $.getJSON('/jsons/faq.json', function (data) {
        faqData = Array.isArray(data) ? data : [];
        render(faqData);
    }).fail(() => {
        renderBlank("도움말을 불러오지 못했습니다.");
    });

    // 검색 : 질문/답변에 검색어가 포함된 항목만 표시
    $('#faq-form').on('submit', function (e) {
        e.preventDefault();
        const keyword = $('#faq-search').val().toLowerCase().trim();

        const filtered = !keyword ? faqData : faqData.map(item => ({
            ...item,
            questions: item.questions.filter(q =>
                q.question.toLowerCase().includes(keyword) ||
                q.answer.toLowerCase().includes(keyword)
            )
        })).filter(item => item.questions.length > 0);

        activeIndex = 0;
        render(filtered, keyword);
    });
});

function render(data, keyword = "") {
    if (data.length === 0) {
        $('#faqTabMenu').empty();
        renderBlank(keyword ? `"${keyword}"에 대한 검색 결과가 없습니다.` : "등록된 도움말이 없습니다.");
        return;
    }

    renderTabs(data);
    renderQuestions(data[activeIndex] ?? data[0]);
}

function renderTabs(data) {
    const $menu = $('#faqTabMenu').empty();

    data.forEach((item, index) => {
        const $link = $('<a class="nav-link" role="tab" href="#"></a>')
            .toggleClass('active', index === activeIndex)
            .attr('aria-selected', index === activeIndex);

        $link.append($('<span></span>').text(item.category));
        $link.append($('<span class="faq-counter"></span>').text(item.questions.length));

        $link.on('click', (e) => {
            e.preventDefault();
            activeIndex = index;
            renderTabs(data);
            renderQuestions(item);
        });

        $menu.append($('<li class="nav-item" role="presentation"></li>').append($link));
    });
}

function renderQuestions(item) {
    const $content = $('#faqTabContent').empty();
    const $list = $('<div class="faq-list"></div>');

    item.questions.forEach(q => {
        const $details = $('<details class="faq-item"></details>');
        const $summary = $('<summary></summary>');
        $summary.append('<span class="faq-q">Q</span>');
        $summary.append($('<span class="faq-question"></span>').text(q.question));
        $summary.append('<svg class="faq-chevron" viewBox="0 0 16 16" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M12.78 5.22a.749.749 0 0 1 0 1.06l-4.25 4.25a.749.749 0 0 1-1.06 0L3.22 6.28a.749.749 0 1 1 1.06-1.06L8 8.939l3.72-3.719a.749.749 0 0 1 1.06 0Z"/></svg>');

        $details.append($summary);
        $details.append($('<div class="faq-answer"></div>').html(q.answer));
        $list.append($details);
    });

    $content.append($list);
}

function renderBlank(message) {
    $('#faqTabContent').empty().append(
        $('<div class="faq-blank"></div>').append($('<p></p>').text(message))
    );
}
