package com.koboolean.metagen.home.controller;

import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.project.domain.dto.ProjectDto;
import com.koboolean.metagen.core.user.service.UserService;
import com.koboolean.metagen.home.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final DashboardService dashboardService;

    @PreAuthorize("hasRole('NOT_APPROVE')")
    @GetMapping(value="/")
    public String dashboard(Model model, @AuthenticationPrincipal AccountDto accountDto) {

        DashboardDto dashboard = dashboardService.selectDashboardData(accountDto);

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
    @PreAuthorize("hasRole('NOT_APPROVE')")
    @GetMapping(value="/notice")
    public String notice(Model model, @AuthenticationPrincipal AccountDto accountDto) {

        List<ProjectDto> projectDtos = userService.selectAllProjectsByUsernameProjectManager(accountDto);

        for(ProjectDto projectDto : projectDtos){
            if(projectDto.getId().equals(accountDto.getProjectId())){
                // 프로젝트 ID가 포함되어있을 경우에만 공지사항 추가, 삭제가 가능하다.
                model.addAttribute("isProjectManager", true);
            }
        }


        return "pages/operation/notice_list";
    }

    @PreAuthorize("hasRole('VIEWER')")
    @GetMapping("/tableManage")
    public String tableManage() {
        return "pages/data/table-manage";
    }

    @PreAuthorize("hasRole('USER')") // 화면의 그리드 API 권한(USER)과 동일 (MenuAuthorization 참고)
    @GetMapping("/columnManage")
    public String columnManage() {
        return "pages/data/column_manage";
    }

    @PreAuthorize("hasRole('USER')") // 화면의 그리드 API 권한(USER)과 동일 (MenuAuthorization 참고)
    @GetMapping("/codeRule")
    public String codeRule() {
        return "pages/design/code_rule";
    }

    @PreAuthorize("hasRole('VIEWER')")
    @GetMapping("/codeRuleManage")
    public String codeRuleManage(Model model, @AuthenticationPrincipal AccountDto accountDto) {

        ProjectDto projectDto = userService.selectProjects(accountDto);

        model.addAttribute("project", projectDto);

        return "pages/design/code_rule_manage";
    }

    @PreAuthorize("hasRole('USER')") // 화면의 그리드 API 권한(USER)과 동일 (MenuAuthorization 참고)
    @GetMapping("/designManage")
    public String designTestManage() {
        return "pages/design/design_manage";
    }

    @PreAuthorize("hasRole('USER')") // 화면의 그리드 API 권한(USER)과 동일 (MenuAuthorization 참고)
    @GetMapping("/testManage")
    public String testManage() {
        return "pages/design/test_manage";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/userManage")
    public String manage() {
        return "pages/system/user_manage";
    }

    @PreAuthorize("hasRole('DBA')")
    @GetMapping("/systemLog")
    public String systemLog() {
        return "pages/system/system_log";
    }

    @PreAuthorize("hasRole('VIEWER')")
    @GetMapping("/dataDictionary")
    public String dataDictionary() {
        return "pages/data/data_dictionary";
    }

    @GetMapping("/help")
    public String help() {
        return "pages/operation/help";
    }

    @PreAuthorize("hasRole('DBA')")
    @GetMapping("/projectManage")
    public String projectManage(Model model, @AuthenticationPrincipal AccountDto accountDto) {

        List<ProjectDto> projectDtos = userService.selectAllProjectsByUsernameProjectManager(accountDto);

        model.addAttribute("projects", projectDtos);

        if(!projectDtos.isEmpty()) {
            model.addAttribute("crudActions", projectDtos.get(0).getIsAutoActive()? "cd" : "ancd");
        }else{
            // 프로젝트 관리자가 아닌경우 권한 없음 페이지로 이동
            String errorMsg = URLEncoder.encode("프로젝트 관리자만 접근 가능합니다", StandardCharsets.UTF_8);
            String exception = URLEncoder.encode("접근 불가 페이지", StandardCharsets.UTF_8);
            return "redirect:/denied?msg=" + errorMsg + "&exception=" + exception;
        }



        return "pages/system/project_manage";
    }
}
