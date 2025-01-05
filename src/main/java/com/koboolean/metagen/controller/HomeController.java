package com.koboolean.metagen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping(value="/")
    public String dashboard() {
        return "pages/dashboard";
    }

    @GetMapping("/meta")
    public String meta() {
        return "pages/meta";
    }

    @GetMapping("/codeRule")
    public String codeRule() {
        return "pages/code_rule";
    }

    @GetMapping("/designTestManage")
    public String designTestManage() {
        return "pages/design_test_manage";
    }

    @GetMapping("/manage")
    public String manage() {
        return "pages/manage";
    }

    @GetMapping("/systemLog")
    public String systemLog() {
        return "pages/system_log";
    }

    @GetMapping("/help")
    public String help() {
        return "pages/help";
    }
}
