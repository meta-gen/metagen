package com.koboolean.metagen.home.controller;

import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping(value="/")
    public String dashboard(Model model, @AuthenticationPrincipal AccountDto accountDto) {

        DashboardDto dashboard = userService.selectDashboardData(accountDto);

        model.addAttribute("pendingStandardCount", dashboard.getPendingStandardCount());
        model.addAttribute("pendingTableCount", dashboard.getPendingTableCount());
        model.addAttribute("pendingColumnCount", dashboard.getPendingColumnCount());
        model.addAttribute("notices", dashboard.getNotices());
        model.addAttribute("recentChanges", dashboard.getRecentChanges());

        return "pages/dashboard";
    }

    /**
     * 공지사항 리스트
     * @return
     */
    @GetMapping(value="/notice")
    public String notice(Model model) {
    	
    	// 사용자의 선택가능 프로젝트 목록을 조회한다.
    	List<ProjectDto> projectDtos = userService.selectAllProjects();

        model.addAttribute("projects", projectDtos);
    	
        return "pages/operation/notice/notice_list";
    }

    @GetMapping("/tableManage")
    public String tableManage() {
        return "pages/data/table-manage";
    }

    @GetMapping("/columnManage")
    public String columnManage() {
        return "pages/data/column_manage";
    }

    @GetMapping("/codeRule")
    public String codeRule() {
        return "pages/design/code_rule";
    }

    @GetMapping("/codeRuleManage")
    public String codeRuleManage(Model model, @AuthenticationPrincipal AccountDto accountDto) {

        ProjectDto projectDto = userService.selectProjects(accountDto);

        model.addAttribute("project", projectDto);

        return "pages/design/code_rule_manage";
    }

    @GetMapping("/designManage")
    public String designTestManage() {
        return "pages/design/design_manage";
    }

    @GetMapping("/testManage")
    public String testManage() {
        return "pages/design/test_manage";
    }

    @GetMapping("/userManage")
    public String manage() {
        return "pages/system/user_manage";
    }

    @GetMapping("/systemLog")
    public String systemLog() {
        return "pages/system/system_log";
    }

    @GetMapping("/dataDictionary")
    public String dataDictionary() {
        return "pages/data/data_dictionary";
    }

    @GetMapping("/accessManage")
    public String accessControl() {
        return "pages/system/access_manage";
    }

    @GetMapping("/help")
    public String help() {
        return "pages/operation/help";
    }

    @GetMapping("/projectManage")
    public String projectManage(Model model) {

        List<ProjectDto> projectDtos = userService.selectAllProjects();

        model.addAttribute("projects", projectDtos);
        model.addAttribute("crudActions", projectDtos.get(0).getIsAutoActive()? "cd" : "ancd");

        return "pages/system/project_manage";
    }
}
