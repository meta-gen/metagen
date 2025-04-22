package com.koboolean.metagen.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/popup")
public class PopupController {

    @GetMapping("/standardTermSearch")
    public String standardTermSearch(){
        return "popup/standard_term_search";
    }

    @GetMapping("/columTableSearch")
    public String columTableSearch(){
        return "popup/column_table_search";
    }

    @GetMapping("/columTableSearch/detail")
    public String columTableSearchDetail(){
        return "popup/column_table_detail";
    }

    @GetMapping("/codeRulePopup")
    public String codeRulePopup(){
        return "popup/code_rule_popup";
    }

    @GetMapping("/codeRuleDetailPopup")
    public String codeRuleDetailPopup(){
        return "popup/code_rule_detail_popup";
    }
}
