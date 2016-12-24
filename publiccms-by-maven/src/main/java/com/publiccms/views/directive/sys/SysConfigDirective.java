package com.publiccms.views.directive.sys;

// Generated 2015-5-10 17:54:56 by com.sanluan.common.source.SourceMaker

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.logic.component.config.ConfigComponent;
import com.publiccms.views.pojo.SysConfig;
import com.sanluan.common.handler.RenderHandler;

@Component
public class SysConfigDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, Exception {
        String code = handler.getString("code");
        if (notEmpty(code)) {
            Map<String, SysConfig> map = configComponent.getMap(getSite(handler));
            SysConfig entity = map.get(code);
            if (notEmpty(entity)) {
                handler.put("object", entity).render();
            }
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Autowired
    private ConfigComponent configComponent;
}