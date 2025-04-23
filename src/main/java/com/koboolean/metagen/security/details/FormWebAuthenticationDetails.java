package com.koboolean.metagen.security.details;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Getter
public class FormWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String secretKey;
    private final Long projectId;
    private final HttpServletRequest request;

    public FormWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.request = request;
        secretKey = request.getParameter("secret_key");
        projectId = Long.parseLong(request.getParameter("project_id"));
    }
}
