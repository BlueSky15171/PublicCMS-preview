package com.publiccms.views.directive.cms;

// Generated 2015-12-24 10:49:03 by com.sanluan.common.source.SourceMaker

import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.logic.service.cms.CmsPlaceService;
import com.sanluan.common.handler.PageHandler;
import com.sanluan.common.handler.RenderHandler;

@Component
public class CmsPlaceListDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, Exception {
        Date endPublishDate = handler.getDate("endPublishDate");
        Boolean disabled = handler.getBoolean("disabled", false);
        Integer status = handler.getInteger("status");
        String path = handler.getString("path");
        if (!handler.getBoolean("admin", false)) {
            Date now = getDate();
            if (empty(endPublishDate) || endPublishDate.after(now)) {
                endPublishDate = now;
            }
            disabled = false;
            status = CmsPlaceService.STATUS_NORMAL;
        }
        if (notEmpty(path)) {
            path = path.replace("//", SEPARATOR);
        }
        PageHandler page = service.getPage(getSite(handler).getId(), handler.getInteger("userId"), path,
                handler.getString("itemType"), handler.getInteger("itemId"), handler.getDate("startPublishDate"),
                handler.getDate("endPublishDate"), status, disabled, handler.getString("orderField"),
                handler.getString("orderType"), handler.getInteger("pageIndex", 1), handler.getInteger("count", 30));
        handler.put("page", page).put("path", path).render();
    }

    @Autowired
    private CmsPlaceService service;

}