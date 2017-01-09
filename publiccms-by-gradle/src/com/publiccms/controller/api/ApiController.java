package com.publiccms.controller.api;

import static org.springframework.util.StringUtils.uncapitalize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.publiccms.common.base.AbstractAppDirective;
import com.publiccms.common.base.AbstractController;
import com.publiccms.logic.component.template.TemplateComponent;
import com.sanluan.common.handler.HttpParameterHandler;

/**
 * 
 * AppController 接口指令统一分发
 *
 */
@Controller
public class ApiController extends AbstractController {
    private Map<String, AbstractAppDirective> appMap = new LinkedHashMap<String, AbstractAppDirective>();
    private List<Map<String, String>> appList = new ArrayList<Map<String, String>>();
    private MediaType mediaType = new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET);
    public final static String INTERFACE_NOT_FOUND = "interfaceNotFound";
    public static final Map<String, String> NOT_FOUND_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("error", INTERFACE_NOT_FOUND);
        }
    };

    /**
     * 接口请求统一分发
     * 
     * @param callback
     * @return
     */
    @RequestMapping({ SEPARATOR, "/**" })
    @ResponseBody
    public MappingJacksonValue api(String callback) {
        return getMappingJacksonValue(NOT_FOUND_MAP, callback);
    }

    /**
     * 接口指令统一分发
     * 
     * @param action
     * @param callback
     * @param request
     * @param response
     */
    @RequestMapping("{api}")
    public void api(@PathVariable String api, String callback, HttpServletRequest request, HttpServletResponse response) {
        try {
            AbstractAppDirective directive = appMap.get(api);
            if (notEmpty(directive)) {
                directive.execute(mappingJackson2HttpMessageConverter, mediaType, request, callback, response);
            } else {
                HttpParameterHandler handler = new HttpParameterHandler(mappingJackson2HttpMessageConverter, mediaType, request,
                        callback, response);
                handler.put("error", INTERFACE_NOT_FOUND);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 接口列表
     * 
     * @param callback
     * @return
     */
    @RequestMapping("apis")
    @ResponseBody
    public MappingJacksonValue apis(String callback) {
        return getMappingJacksonValue(appList, callback);
    }

    /**
     * @param directiveMap
     *            接口初始化
     */
    @Autowired(required = false)
    public void setAppMap(Map<String, AbstractAppDirective> directiveMap) {
        StringBuilder directives = new StringBuilder();
        int size = 0;
        for (Entry<String, AbstractAppDirective> entry : directiveMap.entrySet()) {
            String directiveName = uncapitalize(entry.getKey().replaceAll(templateComponent.getDirectiveRemoveRegex(), BLANK));
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", directiveName);
            map.put("needAppToken", String.valueOf(entry.getValue().needAppToken()));
            map.put("needUserToken", String.valueOf(entry.getValue().needUserToken()));
            appList.add(map);
            appMap.put(directiveName, entry.getValue());
            directives.append(directiveName).append(BLANK_SPACE).append(COMMA_DELIMITED);
            size++;
        }
        if (directives.length() > 0) {
            directives.setLength(directives.length() - 1);
        }
        log.info(new StringBuilder().append(size).append(" app directives created:[").append(directives).append("];").toString());
    }

    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;
    @Autowired
    private TemplateComponent templateComponent;
}
