$(document).ready(function () {
    const params = JSON.parse(window.name);
    $("#term_word_text").val(params.standardTermName);


    let domainList = []; // 전역 선언
    let resultAbbr = ""; // 결과 저장용

    $("#search-btn").on("click", function (e) {
        e.preventDefault();

        const termWordText = $("#term_word_text").val();
        const words = termWordText.trim().split(/\s+/); // 띄어쓰기 기준 분리

        $.ajax({
            url: `/api/getStandardTerms/popup/${termWordText}`,
            type: "GET",
            success: (response) => {
                const standardWords = response.list || [];
                domainList = response.standardDomains || []; // 전역 변수에 저장

                let abbreviationList = [];
                let domainAbbreviation = "";
                let hasUnmatched = false;

                $("#description-box").remove();
                const $descBox = $("<div id='description-box'>").css({ marginTop: "20px" });

                words.forEach((word, index) => {
                    const matched = standardWords.find(w => w.commonStandardWordName === word);

                    const $block = $("<div>").css({
                        marginBottom: "15px",
                        borderBottom: "1px solid #ddd",
                        paddingBottom: "10px"
                    });

                    if (!matched) {
                        hasUnmatched = true;
                        $block.append(`<p><strong>${word}</strong></p>`);
                        $block.append(`<p style="color: red; font-size: 0.9rem;">X 해당 단어는 매핑된 표준 단어를 찾을 수 없습니다.</p>`);
                        $descBox.append($block);
                        return;
                    }

                    const abbr = matched.commonStandardWordAbbreviation;
                    const isDomain = matched.commonStandardDomainCategory && matched.commonStandardDomainCategory !== "-";

                    if (isDomain && index === words.length - 1) {
                        domainAbbreviation = abbr;

                        // 기본 선택용 도메인명 저장
                        if(matched.synonyms !== "-" && matched.synonyms !== ""){
                            const data = matched.synonyms.split(",");

                            let standard = [];

                            words.forEach((i, e) => {
                                standard[e] = i;
                            });

                            standard[standard.length -1] = "";
                            const domainNonData = standard.join(" ");

                            let synonyms = [];

                            for(let d = 0; d < data.length; d++){
                                synonyms[d] = domainNonData+ data[d].trim();
                            }

                            $("#hidden-synonyms").val(synonyms.join());
                        }

                    } else {
                        abbreviationList.push(abbr);
                        $("#hidden-synonyms").val("");
                    }

                    const title = isDomain
                        ? `<strong>[도메인]</strong> ${matched.commonStandardWordName}`
                        : `<strong>${matched.commonStandardWordName}</strong>`;
                    $block.append(`<p>${title}</p>`);

                    if (matched.commonStandardWordDescription && matched.commonStandardWordDescription !== "-") {
                        $block.append(`<p style="font-size: 0.9rem; color: #333;">${matched.commonStandardWordDescription}</p>`);
                    }

                    if (matched.synonyms && matched.synonyms !== "-") {
                        $block.append(`<p style="font-size: 0.85rem; color: #555;">
                            <strong>이음동의어:</strong> ${matched.synonyms}
                        </p>`);
                    }

                    $descBox.append($block);
                });

                if (domainAbbreviation) {
                    abbreviationList.push(domainAbbreviation);
                }

                resultAbbr = abbreviationList.join("_");
                $("#standard_term_result_text").val(resultAbbr);
                $("#hidden-abbreviation").val(resultAbbr);
                $(".popup-container").append($descBox);

                populateDomainOptions(domainList);

                if (hasUnmatched) {
                    $("#confirm-btn").css("display", "none");
                } else {
                    // 도메인을 선택하지 않아도 기본적으로 활성화 가능하게 하려면 여기서 true 설정
                    $("#confirm-btn").css("display", "inline-block");
                    $("#confirm-btn").prop("disabled", true);
                }
            }
        });
    });

    $("#domain-select").on("change", function () {
        const selectedDomainName = $(this).val();
        const selectedDomain = domainList.find(d => d.commonStandardDomainName === selectedDomainName);

        if (selectedDomainName && selectedDomain) {
            // 도메인이 선택된 경우 → 버튼 활성화
            $("#confirm-btn").prop("disabled", false);

            $("#hidden-domain-name").val(selectedDomain.commonStandardDomainName);
            $("#hidden-allowed-values").val(selectedDomain.allowedValues);
            $("#hidden-storage-format").val(selectedDomain.storageFormat);
            $("#hidden-display-format").val(selectedDomain.displayFormat);
        } else {
            // 아무것도 선택되지 않은 경우 → 버튼 비활성화
            $("#confirm-btn").prop("disabled", true);
        }
    });

    $("#confirm-btn").on("click", function () {

        if (!resultAbbr) {
            alert("선택된 용어가 없습니다.");
            return;
        }

        const resultData = {
            termWordText : $("#term_word_text").val(),
            abbreviation: $("#hidden-abbreviation").val(),
            domainName: $("#hidden-domain-name").val(),
            allowedValues: $("#hidden-allowed-values").val(),
            storageFormat: $("#hidden-storage-format").val(),
            displayFormat: $("#hidden-display-format").val(),
            synonyms: $("#hidden-synonyms").val()
        };

        const missingKey = Object.entries(resultData).find(([key, value]) => !value?.trim());
        if (missingKey) {
            if(missingKey[0] !== "synonyms"){
                alert(`필수값이 누락되었습니다.`);
                return;
            }
        }

        // 부모로 데이터 전달
        if (window.opener && typeof window.opener.popupFunction?.receiveTermAbbreviation === 'function') {
            window.opener.popupFunction.receiveTermAbbreviation(resultData);
        } else {
            alert("부모 창과의 통신이 불가능합니다.");
        }

        window.close();
    });

    function populateDomainOptions(domains) {
        const $select = $("#domain-select");
        $select.empty();
        $select.append(`<option value="">도메인을 선택하세요</option>`);

        domains.forEach(domain => {
            const domainName = domain.commonStandardDomainName;
            const display = `${domainName} (${domain.storageFormat || "-"})`;
            $select.append(`<option value="${domainName}">${display}</option>`);
        });
    }

    $("#term_word_text").on("input", function(){
        $("#confirm-btn").css("display", "none").prop("disabled", true);
    });
});
