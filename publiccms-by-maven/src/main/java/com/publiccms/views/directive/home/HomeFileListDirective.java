package com.publiccms.views.directive.home;

// Generated 2016-11-13 11:38:14 by com.sanluan.common.source.SourceMaker

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.logic.service.home.HomeFileService;
import com.sanluan.common.handler.RenderHandler;
import com.sanluan.common.handler.PageHandler;

@Component
public class HomeFileListDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, Exception {
        PageHandler page = service.getPage(getSite(handler).getId(), handler.getLong("userId"), 
                handler.getLong("directoryId"), handler.getString("title"), handler.getString("filePath"), 
                handler.getBoolean("image"), handler.getBoolean("disabled"), 
                handler.getString("orderField"), handler.getString("orderType"), handler.getInteger("pageIndex",1), handler.getInteger("count",30));
        handler.put("page", page).render();
    }

    @Autowired
    private HomeFileService service;

}