package com.publiccms.controller.api;

import static com.publiccms.controller.api.ApiController.NOT_FOUND_MAP;
import static org.springframework.util.StringUtils.uncapitalize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publiccms.common.base.AbstractController;
import com.publiccms.entities.sys.SysApp;
import com.publiccms.entities.sys.SysAppToken;
import com.publiccms.logic.component.template.TemplateComponent;
import com.publiccms.logic.service.sys.SysAppService;
import com.publiccms.logic.service.sys.SysAppTokenService;
import com.sanluan.common.base.BaseMethod;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * 
 * MethodController 方法统一分发
 *
 */
@RestController
public class MethodController extends AbstractController {
    private Map<String, BaseMethod> methodMap = new HashMap<String, BaseMethod>();
    private List<Map<String, String>> methodList = new ArrayList<Map<String, String>>();
    private ObjectWrapper objectWrapper;

    /**
     * 接口指令统一分发
     * 
     * @param method
     * @param callback
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("method/{name}")
    public MappingJacksonValue method(@PathVariable String name, String appToken, String callback, HttpServletRequest request,
            HttpServletResponse response) {
        BaseMethod method = methodMap.get(name);
        if (notEmpty(method)) {
            try {
                if (method.needAppToken()) {
                    SysAppToken token = appTokenService.getEntity(appToken);
                    if (empty(token)) {
                        return getMappingJacksonValue(NOT_FOUND_MAP, callback);
                    }
                    SysApp app = appService.getEntity(token.getAppId());
                    if (empty(app)) {
                        return getMappingJacksonValue(NOT_FOUND_MAP, callback);
                    }
                }
                String[] paramters = request.getParameterValues("paramters");
                if (notEmpty(paramters)) {
                    List<TemplateModel> list = new ArrayList<TemplateModel>();
                    for (String paramter : paramters) {
                        list.add(getObjectWrapper().wrap(paramter));
                    }
                    return getMappingJacksonValue(method.exec(list), callback);
                } else {
                    return getMappingJacksonValue(method.exec(null), callback);
                }
            } catch (TemplateModelException e) {
                return getMappingJacksonValue(null, callback);
            }
        } else {
            return getMappingJacksonValue(NOT_FOUND_MAP, callback);
        }

    }

    /**
     * 接口列表
     * 
     * @param callback
     * @return
     */
    @RequestMapping("methods")
    public MappingJacksonValue methods(String callback) {
        return getMappingJacksonValue(methodList, callback);
    }

    /**
     * @param methodMap
     *            接口初始化
     */
    @Autowired
    public void setActionMap(Map<String, BaseMethod> map) {
        for (Entry<String, BaseMethod> entry : map.entrySet()) {
            if (entry.getValue().httpEnabled()) {
                String methodName = uncapitalize(entry.getKey().replaceAll(templateComponent.getMethodRemoveRegex(), BLANK));
                methodMap.put(methodName, entry.getValue());
                Map<String, String> resultMap = new HashMap<String, String>();
                resultMap.put("name", methodName);
                resultMap.put("needAppToken", String.valueOf(entry.getValue().needAppToken()));
                resultMap.put("needUserToken", String.valueOf(false));
                methodList.add(resultMap);
            }
        }
    }

    private ObjectWrapper getObjectWrapper() {
        if (null == objectWrapper) {
            objectWrapper = templateComponent.getWebConfiguration().getObjectWrapper();
        }
        return objectWrapper;
    }

    @Autowired
    private SysAppTokenService appTokenService;
    @Autowired
    private SysAppService appService;
    @Autowired
    private TemplateComponent templateComponent;
}
