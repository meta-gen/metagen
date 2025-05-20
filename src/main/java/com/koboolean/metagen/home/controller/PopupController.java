package com.koboolean.metagen.home.controller;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.operation.notice.service.NoticeService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.access.controller.AccessRestController;
import com.koboolean.metagen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/popup")
@RequiredArgsConstructor
public class PopupController {

    private final UserService userService;
    private final NoticeService noticeService;

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
    @GetMapping("/noticePopupDetail/{id}")
    public String noticePopupDetail(@PathVariable(name="id") Long id, Model model, @AuthenticationPrincipal AccountDto accountDto){
        model.addAttribute("notice", noticeService.noticePopupMain(id, accountDto));
        return "popup/notice_popup_detail";
    }

    
    /**
     * 공지사항 등록 팝업
     * @return
     */
    @GetMapping("/noticePopupSave")
    public String noticePopupSave(@AuthenticationPrincipal AccountDto accountDto, Model model, @RequestParam(value = "type") String type){

        if(type.equals("add")){
            model.addAttribute("projects", userService.selectAllProjectsByUsernameProjectManager(accountDto));
        }

        return "popup/notice_popup_save";
    }
}
