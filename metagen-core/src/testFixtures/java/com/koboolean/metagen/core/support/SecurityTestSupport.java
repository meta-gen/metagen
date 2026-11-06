package com.koboolean.metagen.core.support;

import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.util.AuthUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

/**
 * 서비스 단위 테스트용 로그인 컨텍스트.
 * <p>
 * {@link AuthUtil} 은 SecurityContext 의 principal({@link AccountDto})과 static {@link ProjectRepository} 를 사용하므로,
 * 승인 권한 분기({@code AuthUtil.isApprovalAvailable()})를 타는 서비스를 테스트할 때 이 클래스로 로그인 상태를 만든다.
 * 테스트 종료 시 {@link #clear()} 를 호출한다.
 */
public final class SecurityTestSupport {

    private SecurityTestSupport() {
    }

    /**
     * 지정한 권한으로 로그인한다. 프로젝트 관리자 여부는 mock ProjectRepository(빈 결과)로 false 처리된다.
     *
     * @param roles ROLE_ 접두사를 포함한 권한명 (예: ROLE_ADMIN, ROLE_USER)
     */
    public static AccountDto loginAs(Long projectId, String accountId, String... roles) {
        return loginAs(mock(ProjectRepository.class), projectId, accountId, roles);
    }

    /**
     * 프로젝트 관리자 판정에 사용할 ProjectRepository 를 지정하여 로그인한다.
     */
    public static AccountDto loginAs(ProjectRepository projectRepository, Long projectId, String accountId, String... roles) {
        new AuthUtil(projectRepository);

        AccountDto accountDto = AccountDto.builder()
                .id(accountId)
                .username("user" + accountId)
                .name("사용자" + accountId)
                .projectId(projectId)
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                accountDto, null, Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return accountDto;
    }

    /**
     * 승인 권한자(ADMIN)로 로그인
     */
    public static AccountDto loginAsAdmin(Long projectId) {
        return loginAs(projectId, "1", "ROLE_ADMIN");
    }

    /**
     * 일반 사용자(승인 권한 없음)로 로그인
     */
    public static AccountDto loginAsUser(Long projectId) {
        return loginAs(projectId, "2", "ROLE_USER");
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
