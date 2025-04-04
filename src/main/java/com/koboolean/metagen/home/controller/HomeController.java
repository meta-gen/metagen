package com.koboolean.metagen.home.controller;

import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping(value="/")
    public String dashboard() {
        return "pages/dashboard";
    }

    @GetMapping(value="/notice")
    public String notice() {
        return "pages/notice";
    }

    @GetMapping("/meta")
    public String meta() {
        return "pages/meta";
    }

    @GetMapping("/codeRule")
    public String codeRule() {
        return "pages/code_rule";
    }

    @GetMapping("/designManage")
    public String designTestManage() {
        return "pages/design_manage";
    }

    @GetMapping("/testManage")
    public String testManage() {
        return "pages/test_manage";
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

    @GetMapping("/accessControl")
    public String accessControl() {
        return "pages/access_control";
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
