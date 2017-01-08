package com.publiccms.common.view;

import static com.publiccms.logic.component.site.SiteComponent.expose;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.publiccms.logic.component.site.SiteComponent;

/**
 * 
 * InitializeFreeMarkerView
 *
 */
public class InitializeFreeMarkerView extends FreeMarkerView {
    protected static final String CONTEXT_ADMIN = "admin";
    protected static final String CONTEXT_USER = "user";
    public static final String CONTEXT_BASE = "base";
    public static SiteComponent siteComponent;

    @Override
    protected void exposeHelpers(Map<String, Object> model, HttpServletRequest request) throws Exception {
        model.put(CONTEXT_BASE,
                getBasePath(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath()));
        expose(model, siteComponent.getSite(request.getServerName()));
        super.exposeHelpers(model, request);
    }

    public static String getBasePath(String scheme, String serverName, int serverPort, String contextPath) {
        if (80 == serverPort) {
            return new StringBuilder(scheme).append("://").append(serverName).append(contextPath).toString();
        } else {
            return new StringBuilder(scheme).append("://").append(serverName).append(":").append(serverPort).append(contextPath)
                    .toString();
        }
    }

    protected void exposeParamters(Map<String, Object> model, HttpServletRequest request) {
        Enumeration<String> parameters = request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String paramterName = parameters.nextElement();
            String[] values = request.getParameterValues(paramterName);
            if (isNotEmpty(values)) {
                if (1 < values.length) {
                    model.put(paramterName, values);
                } else {
                    model.put(paramterName, values[0]);
                }
            }
        }
    }
}