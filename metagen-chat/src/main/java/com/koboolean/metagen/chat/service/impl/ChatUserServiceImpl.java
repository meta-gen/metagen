package com.koboolean.metagen.chat.service.impl;

import com.koboolean.metagen.chat.service.ChatUserService;
import com.koboolean.metagen.chat.service.MessageSaveService;
import com.koboolean.metagen.core.login.service.LoginUserCache;
import com.koboolean.metagen.core.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatUserServiceImpl implements ChatUserService {

    private final UserService userService;
    private final LoginUserCache loginUserCache;
    private final MessageSaveService messageSaveService;

    @Override
    public Map<String, Object> getActiveUser(AccountDto accountDto) {

        Map<String, Object> result = new HashMap<>();

        List<AccountDto> accountDtos = userService.getAccountList(); // 전체 사용자
        Map<String, Map<String, String>> loginUserMap = loginUserCache.getAll(); // 로그인된 사용자 (key : 사용자 id)

        List<Map<String, String>> loggedIn = new ArrayList<>();
        List<Map<String, String>> loggedOut = new ArrayList<>();

        for (AccountDto dto : accountDtos) {
            Map<String, String> loginUser = loginUserMap.get(dto.getId());

            if (loginUser != null) {
                Long projectId = Long.parseLong(loginUser.get("projectId"));

                String username = dto.getUsername();
                String projectName = userService.getProjectName(projectId);
                ProjectMemberDto projectRole = userService.getProjectRoleName(projectId, username);

                Map<String, String> user = new HashMap<>();

                if ("O".equals(projectRole.getProjectManagerYn())) {
                    user.put("role", "매니저");
                } else {
                    user.put("role", "사용자");
                }

                user.put("isMyData", dto.getId().equals(accountDto.getId()) ? "true" : "false");
                user.put("id", dto.getId());
                user.put("name", dto.getName());
                user.put("project", projectName);
                user.put("status", "active");
                user.put("badge", messageSaveService.findByBadge(dto.getId(), accountDto.getId()));

                loggedIn.add(user);
            } else {
                Map<String, String> user = new HashMap<>();
                user.put("id", dto.getId());
                user.put("username", dto.getUsername());
                user.put("name", dto.getName());
                user.put("status", "inactive");
                user.put("isMyData", "false");
                user.put("badge", messageSaveService.findByBadge(dto.getId(), accountDto.getId()));

                loggedOut.add(user);
            }
        }
        result.put("loggedIn", loggedIn);
        result.put("loggedOut", loggedOut);

        return result;
    }
}
