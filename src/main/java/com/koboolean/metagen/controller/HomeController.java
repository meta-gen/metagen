package com.koboolean.metagen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

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

    @GetMapping("/manage")
    public String manage() {
        return "pages/manage";
    }

    @GetMapping("/systemLog")
    public String systemLog() {
        return "pages/system_log";
    }

    @GetMapping("/dataDictionary")
    public String dataDictionary() {
        return "pages/data_dictionary";
    }

    @GetMapping("/accessControl")
    public String accessControl() {
        return "pages/access_control";
    }

    @GetMapping("/help")
    public String help() {
        return "pages/help";
    }
}
