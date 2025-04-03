
$(document).ready(function () {
    getFaqData();

})

function getFaqData(){
    $.ajax({
        url: `/jsons/faq.json`,
        type: "GET",
        dataType: "json",
        success : function (response){

            renderFAQList(response);


        }
    })

}

// 초기 렌더링
function renderFAQList(data) {
    const listContainer = document.getElementById("faq-list");
    listContainer.innerHTML = '';

    data.forEach(item => {
        const questionDiv = document.createElement("div");
        questionDiv.className = "faq-item";
        questionDiv.style.borderBottom = "1px solid #ddd";
        questionDiv.style.padding = "10px 0";

        const questionTitle = document.createElement("div");
        questionTitle.innerHTML = `<strong>${item.question}</strong>`;
        questionTitle.style.cursor = "pointer";

        const answerDiv = document.createElement("div");
        answerDiv.textContent = item.answer;
        answerDiv.style.display = "none";
        answerDiv.style.marginTop = "5px";
        answerDiv.style.color = "#555";

        questionTitle.addEventListener("click", () => {
            answerDiv.style.display = (answerDiv.style.display === "none") ? "block" : "none";
        });

        questionDiv.appendChild(questionTitle);
        questionDiv.appendChild(answerDiv);
        listContainer.appendChild(questionDiv);
    });
}

$('#faq-search-btn').on('click', function () {
    const keyword = $('#faq-search').val().toLowerCase().trim(); // 검색어 입력

    $.getJSON('/jsons/faq.json', function (data) {
        // keyword가 question 또는 answer에 포함된 것만 필터링
        const filtered = data.filter(item =>
            item.question.toLowerCase().includes(keyword) ||
            item.answer.toLowerCase().includes(keyword)
        );

        renderFAQList(filtered); // 화면에 다시 그리기
    });
});
