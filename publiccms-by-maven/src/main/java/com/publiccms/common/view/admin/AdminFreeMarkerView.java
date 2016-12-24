package com.publiccms.common.view.admin;

import static com.publiccms.common.base.AbstractController.getAdminFromSession;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.publiccms.common.view.InitializeFreeMarkerView;

public class AdminFreeMarkerView extends InitializeFreeMarkerView {
    @Override
    protected void exposeHelpers(Map<String, Object> model, HttpServletRequest request) throws Exception {
        exposeParamters(model,request);
        model.put(CONTEXT_ADMIN, getAdminFromSession(request.getSession()));
        super.exposeHelpers(model, request);
    }
}