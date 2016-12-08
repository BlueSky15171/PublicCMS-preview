package com.publiccms.controller.web.cms;

import static com.publiccms.common.tools.ExtendUtils.getExtendString;
import static com.publiccms.common.tools.ExtendUtils.getExtentDataMap;
import static com.publiccms.logic.component.template.TemplateComponent.INCLUDE_DIRECTORY;
import static com.sanluan.common.tools.RequestUtils.getIpAddress;
import static org.apache.commons.lang3.ArrayUtils.contains;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.publiccms.common.base.AbstractController;
import com.publiccms.entities.cms.CmsPlace;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.component.site.StatisticsComponent;
import com.publiccms.logic.component.template.MetadataComponent;
import com.publiccms.logic.service.cms.CmsPlaceAttributeService;
import com.publiccms.logic.service.cms.CmsPlaceService;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.views.pojo.CmsPlaceMetadata;
import com.publiccms.views.pojo.CmsPlaceParamters;
import com.publiccms.views.pojo.CmsPlaceStatistics;

@Controller
@RequestMapping("place")
public class PlaceController extends AbstractController {
    @Autowired
    private CmsPlaceService service;
    @Autowired
    private StatisticsComponent statisticsComponent;
    @Autowired
    private CmsPlaceAttributeService attributeService;
    @Autowired
    private MetadataComponent metadataComponent;

    private String[] ignoreProperties = new String[] { "id", "siteId", "type", "path", "createDate", "userId", "disabled" };

    /**
     * @param entity
     * @param callback
     * @param placeParamters
     * @param request
     * @param session
     * @param model
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public MappingJacksonValue save(CmsPlace entity, String callback, @ModelAttribute CmsPlaceParamters placeParamters,
            HttpServletRequest request, HttpSession session, ModelMap model) {
        if (notEmpty(entity) && notEmpty(entity.getPath())) {
            SysSite site = getSite(request);
            entity.setPath(entity.getPath().replace("//", SEPARATOR));
            String placePath = INCLUDE_DIRECTORY + entity.getPath();
            String filePath = siteComponent.getWebTemplateFilePath(site, placePath);
            CmsPlaceMetadata metadata = metadataComponent.getPlaceMetadata(filePath);
            SysUser user = getUserFromSession(session);
            if (verifyCustom("contribute",
                    empty(user) || empty(metadata) || !metadata.isAllowContribute() || 0 >= metadata.getSize(), model)) {
                return getMappingJacksonValue(model, callback);
            }
            if (notEmpty(entity.getId())) {
                CmsPlace oldEntity = service.getEntity(entity.getId());
                if (empty(oldEntity) || empty(oldEntity.getUserId()) || empty(user)
                        || verifyNotEquals("siteId", site.getId(), oldEntity.getSiteId(), model)
                        || verifyNotEquals("siteId", user.getId(), oldEntity.getUserId(), model)) {
                    return getMappingJacksonValue(model, callback);
                }
                entity = service.update(entity.getId(), entity, ignoreProperties);
                logOperateService.save(new LogOperate(site.getId(), user.getId(), LogLoginService.CHANNEL_WEB, "update.place",
                        getIpAddress(request), getDate(), entity.getPath()));
            } else {
                entity.setSiteId(site.getId());
                Long userId = null;
                if (notEmpty(user)) {
                    userId = user.getId();
                    entity.setUserId(user.getId());
                }
                service.save(entity);
                logOperateService.save(new LogOperate(site.getId(), userId, LogLoginService.CHANNEL_WEB, "save.place",
                        getIpAddress(request), getDate(), entity.getPath()));
            }
            Map<String, String> map = getExtentDataMap(placeParamters.getExtendDataList(),
                    metadataComponent.getPlaceMetadata(filePath).getExtendList());
            String extentString = getExtendString(map);
            attributeService.updateAttribute(entity.getId(), extentString);
        }
        return getMappingJacksonValue(model, callback);
    }

    /**
     * @param id
     * @param callback
     * @param request
     * @param session
     * @param model
     * @return
     */
    @RequestMapping("delete")
    @ResponseBody
    public MappingJacksonValue delete(Long id, String callback, HttpServletRequest request, HttpSession session, ModelMap model) {
        CmsPlace entity = service.getEntity(id);
        SysSite site = getSite(request);
        SysUser user = getUserFromSession(session);
        CmsPlaceMetadata metadata = metadataComponent.getPlaceMetadata(INCLUDE_DIRECTORY + entity.getPath());
        if (verifyCustom("manage",
                empty(entity) || empty(user) || empty(metadata.getAdminIds()) || !contains(metadata.getAdminIds(), user.getId()),
                model) || verifyNotEquals("siteId", site.getId(), entity.getSiteId(), model)) {
            return getMappingJacksonValue(model, callback);
        }
        service.delete(id);
        logOperateService.save(new LogOperate(site.getId(), getAdminFromSession(session).getId(), LogLoginService.CHANNEL_WEB,
                "delete.place", getIpAddress(request), getDate(), id.toString()));
        return getMappingJacksonValue(model, callback);
    }

    /**
     * @param ids
     * @param request
     * @param session
     * @param model
     * @return
     * @return
     */
    @RequestMapping("check")
    @ResponseBody
    public MappingJacksonValue check(Long id, String callback, HttpServletRequest request, HttpSession session, ModelMap model) {
        CmsPlace entity = service.getEntity(id);
        SysSite site = getSite(request);
        SysUser user = getUserFromSession(session);
        CmsPlaceMetadata metadata = metadataComponent.getPlaceMetadata(INCLUDE_DIRECTORY + entity.getPath());
        if (verifyCustom("manage",
                empty(entity) || empty(user) || empty(metadata.getAdminIds()) || !contains(metadata.getAdminIds(), user.getId()),
                model) || verifyNotEquals("siteId", site.getId(), entity.getSiteId(), model)) {
            return getMappingJacksonValue(model, callback);
        }
        service.check(id);
        logOperateService.save(new LogOperate(site.getId(), getAdminFromSession(session).getId(), LogLoginService.CHANNEL_WEB,
                "check.place", getIpAddress(request), getDate(), id.toString()));
        return getMappingJacksonValue(model, callback);
    }

    /**
     * 推荐位链接重定向并计数
     * 
     * @param id
     * @return
     */
    @RequestMapping("redirect")
    public void clicks(Long id, HttpServletRequest request, HttpServletResponse response) {
        SysSite site = getSite(request);
        CmsPlaceStatistics placeStatistics = statisticsComponent.placeClicks(id);
        if (notEmpty(placeStatistics.getEntity()) && site.getId() == placeStatistics.getEntity().getSiteId()) {
            redirectPermanently(response, placeStatistics.getEntity().getUrl());
        } else {
            redirectPermanently(response, site.getSitePath());
        }
    }
}
