package com.publiccms.controller.web;

import static com.publiccms.common.constants.CommonConstants.getCookiesUser;
import static com.publiccms.common.constants.CommonConstants.getCookiesUserSplit;
import static com.publiccms.logic.service.log.LogLoginService.CHANNEL_WEB;
import static com.sanluan.common.tools.RequestUtils.addCookie;
import static com.sanluan.common.tools.RequestUtils.getCookie;
import static com.sanluan.common.tools.RequestUtils.getIpAddress;
import static com.sanluan.common.tools.VerificationUtils.encode;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publiccms.common.base.AbstractController;
import com.publiccms.entities.log.LogLogin;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysUserToken;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.sys.SysUserService;
import com.publiccms.logic.service.sys.SysUserTokenService;

/**
 * 
 * LoginController 登陆逻辑
 *
 */
@RestController
public class LoginController extends AbstractController {
    @Autowired
    private SysUserService service;
    @Autowired
    private SysUserTokenService sysUserTokenService;
    @Autowired
    private LogLoginService logLoginService;

    /**
     * @param username
     * @param password
     * @param callback
     * @param request
     * @param session
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "doLogin")
    public MappingJacksonValue login(String username, String password, String callback, HttpServletRequest request,
            HttpServletResponse response, ModelMap model) {
        SysSite site = getSite(request);
        username = trim(username);
        password = trim(password);
        if (verifyNotEmpty("username", username, model) || verifyNotEmpty("password", password, model)) {
            return getMappingJacksonValue(model, callback);
        }
        SysUser user;
        if (verifyNotEMail(username)) {
            user = service.findByName(site.getId(), username);
        } else {
            user = service.findByEmail(site.getId(), username);
        }
        String ip = getIpAddress(request);
        if (verifyNotExist("username", user, model) || verifyNotEquals("password", encode(password), user.getPassword(), model)
                || verifyNotEnablie(user, model)) {
            Long userId = null;
            if (notEmpty(user)) {
                userId = user.getId();
            }
            logLoginService.save(new LogLogin(site.getId(), username, userId, ip, CHANNEL_WEB, false, getDate(), password));
            return getMappingJacksonValue(model, callback);
        }
        user.setPassword(null);
        setUserToSession(request.getSession(), user);
        String authToken = UUID.randomUUID().toString();
        sysUserTokenService.save(new SysUserToken(authToken, site.getId(), user.getId(), CHANNEL_WEB, getDate(), ip));
        try {
            String loginInfo = user.getId() + getCookiesUserSplit() + authToken + getCookiesUserSplit() + user.isSuperuserAccess()
                    + getCookiesUserSplit() + URLEncoder.encode(user.getNickName(), DEFAULT_CHARSET_NAME);
            model.addAttribute("loginInfo", loginInfo);
            addCookie(request.getContextPath(), response, getCookiesUser(), loginInfo, Integer.MAX_VALUE, null);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }
        service.updateLoginStatus(user.getId(), ip);
        logLoginService.save(new LogLogin(site.getId(), username, user.getId(), ip, CHANNEL_WEB, true, getDate(), null));
        return getMappingJacksonValue(model, callback);
    }

    /**
     * @param callback
     * @param request
     * @param session
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "loginStatus")
    public MappingJacksonValue loginStatus(String callback, HttpServletRequest request, HttpSession session,
            HttpServletResponse response, ModelMap model) {
        SysUser user = getUserFromSession(session);
        if (notEmpty(user)) {
            model.addAttribute("id", user.getId());
            model.addAttribute("name", user.getName());
            model.addAttribute("nickname", user.getNickName());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("emailChecked", user.isEmailChecked());
            model.addAttribute("superuserAccess", user.isSuperuserAccess());
        }
        return getMappingJacksonValue(model, callback);
    }

    /**
     * @param entity
     * @param repassword
     * @param callback
     * @param request
     * @param session
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "doRegister")
    public MappingJacksonValue register(SysUser entity, String repassword, String callback, HttpServletRequest request,
            HttpServletResponse response, ModelMap model) {
        SysSite site = getSite(request);
        entity.setName(trim(entity.getName()));
        entity.setNickName(trim(entity.getNickName()));
        entity.setPassword(trim(entity.getPassword()));
        repassword = trim(repassword);

        if (verifyNotEmpty("username", entity.getName(), model) || verifyNotEmpty("nickname", entity.getNickName(), model)
                || verifyNotEmpty("password", entity.getPassword(), model)
                || verifyNotUserName("username", entity.getName(), model)
                || verifyNotNickName("nickname", entity.getNickName(), model)
                || verifyNotEquals("repassword", entity.getPassword(), repassword, model)
                || verifyHasExist("username", service.findByName(site.getId(), entity.getName()), model)
                || verifyHasExist("nickname", service.findByNickName(site.getId(), entity.getNickName()), model)) {
            return getMappingJacksonValue(model, callback);
        }
        String ip = getIpAddress(request);
        entity.setPassword(encode(entity.getPassword()));
        entity.setLastLoginIp(ip);
        entity.setSiteId(site.getId());
        service.save(entity);
        entity.setPassword(null);
        setUserToSession(request.getSession(), entity);
        String authToken = UUID.randomUUID().toString();
        sysUserTokenService.save(new SysUserToken(authToken, site.getId(), entity.getId(), CHANNEL_WEB, getDate(), ip));
        String loginInfo = entity.getId() + getCookiesUserSplit() + authToken + getCookiesUserSplit() + entity.getNickName();
        model.addAttribute("loginInfo", loginInfo);
        addCookie(request.getContextPath(), response, getCookiesUser(), loginInfo, Integer.MAX_VALUE, null);
        return getMappingJacksonValue(model, callback);
    }

    /**
     * @param callback
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "logout")
    public MappingJacksonValue logout(String callback, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        Cookie userCookie = getCookie(request.getCookies(), getCookiesUser());
        if (null != userCookie && notEmpty(userCookie.getValue())) {
            String value = userCookie.getValue();
            if (null != value) {
                String[] userData = value.split(getCookiesUserSplit());
                if (userData.length > 1) {
                    sysUserTokenService.delete(userData[1]);
                }
            }
        }
        clearUserToSession(request.getContextPath(), request.getSession(), response);
        return getMappingJacksonValue(model, callback);
    }

    protected boolean verifyNotEnablie(SysUser user, ModelMap model) {
        if (user.isDisabled()) {
            model.addAttribute(ERROR, "verify.user.notEnablie");
            return true;
        }
        return false;
    }
}
