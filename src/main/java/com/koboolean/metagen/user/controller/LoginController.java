package com.koboolean.metagen.user.controller;

import com.koboolean.metagen.domain.dto.AccountDto;
import com.koboolean.metagen.domain.entity.Account;
import com.koboolean.metagen.security.exception.CustomFormException;
import com.koboolean.metagen.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    /**
     * 로그인
     * @param error
     * @param exception
     * @param model
     * @return
     */
    @GetMapping(value="/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception, Model model){
        model.addAttribute("error",error);
        model.addAttribute("exception",exception);

        return "pages/login/login";
    }

    @GetMapping(value="/signup")
    public String signup() {
        return "pages/login/signup";
    }

    /**
     * 로그아웃
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:/login";
    }

    /**
     * 권한정보 없음
     * @param exception
     * @param accountDto
     * @param model
     * @return
     */
    @GetMapping(value="/denied")
    public String accessDenied(@RequestParam(value = "exception", required = false) String exception, @AuthenticationPrincipal AccountDto accountDto, Model model) {

        model.addAttribute("name", accountDto.getName());
        model.addAttribute("exception", exception);

        return "pages/login/denied";
    }

    @GetMapping(value = "/account")
    public String account(@AuthenticationPrincipal AccountDto accountDto, Authentication authentication, Model model) {

        accountDto = userService.updateAccount(accountDto);
        accountDto.setPassword(null);

        updateNewAccount(accountDto);

        accountDto.setRoleName(userService.getRoleName(authentication));

        model.addAttribute("account", accountDto);
        return "pages/login/account";
    }

    /**
     * AccountDTO 변경사항 적용하기
     * */
    private void updateNewAccount(AccountDto accountDto) {
        // 현재 SecurityContext에서 Authentication 가져오기
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        // 새로운 AccountDto로 Principal 교체
        UsernamePasswordAuthenticationToken newAuthentication =
                new UsernamePasswordAuthenticationToken(
                        accountDto, // 변경된 계정 정보
                        authentication.getCredentials(), // 기존 인증 정보
                        authentication.getAuthorities() // 기존 권한 정보
                );

        // SecurityContext에 새 Authentication 설정
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }

    @PostMapping(value="/signup")
    public String signup(AccountDto accountDto, Model model) {

        ModelMapper mapper = new ModelMapper();
        Account account = mapper.map(accountDto, Account.class);
        account.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        account.setPasswdCheck(true);

        try {
            userService.createUser(account);
        }catch(CustomFormException e){
            model.addAttribute("errorMessage", e.getErrorCode().getMessage());
            return "login/signup";
        }

        return "redirect:/";
    }
}
