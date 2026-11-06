package com.koboolean.metagen.security.details;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Getter
public class FormWebAuthenticationDetails extends WebAuthenticationDetails {

    private final Long projectId;
    private final HttpServletRequest request;

    public FormWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.request = request;
        String projectIdParam = request.getParameter("project_id");
        projectId = (projectIdParam == null || projectIdParam.isBlank()) ? null : Long.parseLong(projectIdParam);
    }
}
