package com.koboolean.metagen.home.controller;

import com.koboolean.metagen.system.access.controller.AccessRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/popup")
public class PopupController {

    private final AccessRestController accessRestController;

    PopupController(AccessRestController accessRestController) {
        this.accessRestController = accessRestController;
    }

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
    
    /**
     * 공지사항 상세보기 팝업
     * @return
     */
    @GetMapping("/noticePopupDetail/{projectId}/{id}")
    public String noticePopupDetail(@PathVariable(name="projectId") String projectId, @PathVariable(name="id") String id){
    	
        return "popup/notice_popup_detail";
    }
    
    /**
     * 공지사항 등록 팝업
     * @return
     */
    @GetMapping("/noticePopupSave")
    public String noticePopupSave(){
    	
        return "popup/notice_popup_save";
    }
}
