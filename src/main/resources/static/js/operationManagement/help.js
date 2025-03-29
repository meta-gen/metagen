
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


