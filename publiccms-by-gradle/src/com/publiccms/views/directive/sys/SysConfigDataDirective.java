package com.publiccms.views.directive.sys;

import static com.publiccms.common.tools.ExtendUtils.getExtendMap;

// Generated 2016-7-16 11:54:15 by com.sanluan.common.source.SourceMaker

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.entities.sys.SysConfigData;
import com.publiccms.entities.sys.SysConfigDataId;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.service.sys.SysConfigDataService;
import com.sanluan.common.handler.RenderHandler;

@Component
public class SysConfigDataDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, Exception {
        String code = handler.getString("code");
        String subcode = handler.getString("subcode");
        String[] subcodes = handler.getStringArray("subcodes");
        SysSite site = getSite(handler);
        if (notEmpty(code)) {
            if (notEmpty(subcode)) {
                SysConfigData entity = service.getEntity(new SysConfigDataId(site.getId(), code, subcode));
                if (notEmpty(entity)) {
                    handler.put("object", getExtendMap(entity.getData())).render();
                }
            } else if (notEmpty(subcodes)) {
                SysConfigDataId[] ids = new SysConfigDataId[subcodes.length];
                int i = 0;
                for (String s : subcodes) {
                    if (notEmpty(s)) {
                        ids[i++] = new SysConfigDataId(site.getId(), code, s);
                    }
                }
                Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
                for (SysConfigData entity : service.getEntitys(ids)) {
                    map.put(entity.getId().getSubcode(), getExtendMap(entity.getData()));
                }
                handler.put("map", map).render();
            }
        }
    }

    @Autowired
    private SysConfigDataService service;

}
