package com.koboolean.metagen.home.controller;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.operation.notice.service.NoticeService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/popup")
@RequiredArgsConstructor
public class PopupController {

    private final UserService userService;
    private final NoticeService noticeService;

    @PreAuthorize("hasRole('VIEWER')")
    @GetMapping("/standardTermSearch")
    public String standardTermSearch(){
        return "popup/standard_term_search";
    }

    @PreAuthorize("hasRole('VIEWER')")
    @GetMapping("/columTableSearch")
    public String columTableSearch(){
        return "popup/column_table_search";
    }

    @PreAuthorize("hasRole('VIEWER')")
    @GetMapping("/columTableSearch/detail")
    public String columTableSearchDetail(){
        return "popup/column_table_detail";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/codeRulePopup")
    public String codeRulePopup(){
        return "popup/code_rule_popup";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/codeRuleDetailPopup")
    public String codeRuleDetailPopup(){
        return "popup/code_rule_detail_popup";
    }
    
    /**
     * 공지사항 상세보기 팝업
     * @return
     */
    @PreAuthorize("hasRole('NOT_APPROVE')")
    @GetMapping("/noticePopupDetail/{id}")
    public String noticePopupDetail(@PathVariable(name="id") Long id, Model model, @AuthenticationPrincipal AccountDto accountDto){
        model.addAttribute("notice", noticeService.noticePopupMain(id, accountDto));
        return "popup/notice_popup_detail";
    }

    
    /**
     * 공지사항 등록 팝업
     * @return
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/noticePopupSave")
    public String noticePopupSave(@AuthenticationPrincipal AccountDto accountDto, Model model, @RequestParam(value = "type") String type, @RequestParam(value = "id", required = false) Long id){

        if(type.equals("add")){
            model.addAttribute("projects", userService.selectAllProjectsByUsernameProjectManager(accountDto));
        }else{
            model.addAttribute("projects", userService.selectAllProjectsByUsernameProjectManagerChecked(id, accountDto));
        }

        return "popup/notice_popup_save";
    }
}
