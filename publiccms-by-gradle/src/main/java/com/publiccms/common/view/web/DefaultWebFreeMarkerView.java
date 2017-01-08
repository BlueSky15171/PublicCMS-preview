package com.publiccms.common.view.web;

import static com.publiccms.common.base.AbstractController.getUserFromSession;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.publiccms.common.view.InitializeFreeMarkerView;

/**
 * 
 * WebFreeMarkerView 视图类
 *
 */
public class DefaultWebFreeMarkerView extends InitializeFreeMarkerView {
    @Override
    protected void exposeHelpers(Map<String, Object> model, HttpServletRequest request) throws Exception {
        exposeParamters(model,request);
        model.put(CONTEXT_USER, getUserFromSession(request.getSession()));
        super.exposeHelpers(model, request);
    }
}