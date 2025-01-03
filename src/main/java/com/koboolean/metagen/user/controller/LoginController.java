package com.koboolean.metagen.user.controller;

import com.koboolean.metagen.domain.dto.AccountDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginController {

    @GetMapping(value="/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception, Model model){
        model.addAttribute("error",error);
        model.addAttribute("exception",exception);
        return "login/login";
    }

    @GetMapping(value="/signup")
    public String signup() {
        return "login/signup";
    }

    @GetMapping(value = "/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:/login";
    }

    @GetMapping(value="/denied")
    public String accessDenied(@RequestParam(value = "exception", required = false) String exception, @AuthenticationPrincipal AccountDto accountDto, Model model) {

        model.addAttribute("username", accountDto.getUsername());
        model.addAttribute("exception", exception);

        return "login/denied";
    }

    @GetMapping(value = "/account")
    public String account(@AuthenticationPrincipal AccountDto accountDto, Authentication authentication, Model model) {
        accountDto.setPassword(null);
        model.addAttribute("account", accountDto);
        accountDto.setRole(((List)authentication.getAuthorities()).get(0).toString());

        return "login/account";
    }
}
