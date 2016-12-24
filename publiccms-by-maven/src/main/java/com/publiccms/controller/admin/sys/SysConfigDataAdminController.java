package com.publiccms.controller.admin.sys;

import static com.publiccms.common.tools.ExtendUtils.getExtendString;
import static com.publiccms.common.tools.ExtendUtils.getExtentDataMap;
import static com.sanluan.common.tools.JsonUtils.getString;
import static com.sanluan.common.tools.RequestUtils.getIpAddress;
import static org.springframework.web.servlet.support.RequestContextUtils.getLocale;
// Generated 2016-7-16 11:54:16 by com.sanluan.common.source.SourceMaker

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.publiccms.common.base.AbstractController;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.sys.SysConfigData;
import com.publiccms.entities.sys.SysConfigDataId;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.config.ConfigComponent;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.sys.SysConfigDataService;
import com.publiccms.views.pojo.SysConfigParamters;

@Controller
@RequestMapping("sysConfigData")
public class SysConfigDataAdminController extends AbstractController {

    private String[] ignoreProperties = new String[] { "id", "siteId", "code", "itemCode" };

    @RequestMapping("save")
    public String save(SysConfigData entity, @ModelAttribute SysConfigParamters sysConfigParamters, HttpServletRequest request,
            HttpSession session, ModelMap model) {
        SysSite site = getSite(request);
        if (notEmpty(entity.getId())) {
            entity.getId().setSiteId(site.getId());
            SysConfigData oldEntity = service.getEntity(entity.getId());
            if (notEmpty(oldEntity) && verifyNotEquals("siteId", site.getId(), oldEntity.getId().getSiteId(), model)) {
                return TEMPLATE_ERROR;
            }
            Map<String, String> map = getExtentDataMap(sysConfigParamters.getExtendDataList(), configComponent
                    .getExtendFieldList(site, entity.getId().getCode(), entity.getId().getItemCode(), getLocale(request)));
            entity.setData(getExtendString(map));
            if (notEmpty(oldEntity)) {
                entity = service.update(oldEntity.getId(), entity, ignoreProperties);
                if (notEmpty(entity)) {
                    logOperateService.save(new LogOperate(site.getId(), getAdminFromSession(session).getId(),
                            LogLoginService.CHANNEL_WEB_MANAGER, "update.configData", getIpAddress(request), getDate(),
                            getString(entity)));
                    configComponent.removeCache(site.getId(), entity.getId().getCode(), entity.getId().getItemCode());
                }
            } else {
                entity.getId().setSiteId(site.getId());
                service.save(entity);
                logOperateService.save(new LogOperate(site.getId(), getAdminFromSession(session).getId(),
                        LogLoginService.CHANNEL_WEB_MANAGER, "save.configData", getIpAddress(request), getDate(), getString(entity)));
                configComponent.removeCache(site.getId(), entity.getId().getCode(), entity.getId().getItemCode());
            }
        }
        return TEMPLATE_DONE;
    }

    @RequestMapping("delete")
    public String delete(String code, String itemCode, HttpServletRequest request, HttpSession session) {
        SysSite site = getSite(request);
        SysConfigData entity = service.getEntity(new SysConfigDataId(site.getId(), code, itemCode));
        if (notEmpty(entity)) {
            service.delete(entity.getId());
            logOperateService.save(new LogOperate(site.getId(), getAdminFromSession(session).getId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "delete.configData", getIpAddress(request), getDate(), getString(entity)));
            configComponent.removeCache(site.getId(), entity.getId().getCode(), entity.getId().getItemCode());
        }
        return TEMPLATE_DONE;
    }

    @Autowired
    private ConfigComponent configComponent;
    @Autowired
    private SysConfigDataService service;
}