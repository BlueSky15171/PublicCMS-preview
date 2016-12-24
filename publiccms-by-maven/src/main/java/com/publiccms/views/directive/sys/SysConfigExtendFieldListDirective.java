package com.publiccms.views.directive.sys;

// Generated 2016-7-16 11:54:15 by com.sanluan.common.source.SourceMaker

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.logic.component.config.ConfigComponent;
import com.sanluan.common.handler.RenderHandler;

@Component
public class SysConfigExtendFieldListDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, Exception {
        String code = handler.getString("code");
        String itemCode = handler.getString("itemCode");
        if (notEmpty(code) && notEmpty(itemCode)) {
            handler.put("list", configComponent.getExtendFieldList(getSite(handler), code, itemCode, handler.getLocale()))
                    .render();
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Autowired
    private ConfigComponent configComponent;

}
