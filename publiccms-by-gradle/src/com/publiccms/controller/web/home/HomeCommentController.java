package com.publiccms.controller.web.home;

// Generated 2016-11-19 11:25:19 by com.sanluan.common.source.SourceMaker

import static com.sanluan.common.tools.RequestUtils.getIpAddress;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.publiccms.common.base.AbstractController;
import com.publiccms.entities.home.HomeComment;
import com.publiccms.entities.home.HomeCommentContent;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.service.home.HomeCommentContentService;
import com.publiccms.logic.service.home.HomeCommentService;
import com.publiccms.logic.service.log.LogLoginService;

/**
 * 
 * @author wangyadong
 *
 */
@Controller
@RequestMapping("homeComment")
public class HomeCommentController extends AbstractController {

    private String[] ignoreProperties = new String[] { "id" };

    /**
     * 查询文章对应的评论
     * 
     * @param id
     * @param request
     * @param session
     * @param orderType
     * @param pageIndex
     * @param pageSize
     * @return
     * @author wangyadong
     */
    @RequestMapping("getCommentListByAid")
    @ResponseBody
    public Object getCommentListByAid(Long id, HttpServletRequest request, HttpSession session, String orderType,
            Integer pageIndex, Integer pageSize, String flag) {
        if (null == flag || "".equals(flag))// 查询评论状态 1 = 正常 0=禁用
            return "";
        boolean b = true;
        if ("0".equals(flag))
            b = false;
        SysSite site = getSite(request);
        if (notEmpty(id)) {
            // 文章id ,站点id,是否禁用
            return service.getPage(site.getId(), null, null, id, b, orderType, pageIndex, pageSize);
        }
        return "";
    }

    /**
     * 
     * @param entity
     * @param request
     * @param session
     * @return
     * @author wangyadong
     */
    @RequestMapping("save")
    @ResponseBody
    public String save(HomeComment entity, HttpServletRequest request, HttpSession session, String content) {
        // 等于null则返回
        if (null == getUserFromSession(session))
            return "";
        if (null == content)
            return "content";
        SysUser sysuser = getUserFromSession(session);// 取得当前用户
        SysSite site = getSite(request);// 取得当前站点
        try {// 理論上不會報錯 以防萬一
            if (notEmpty(entity.getId())) {
                entity = service.update(entity.getId(), entity, ignoreProperties);
                // 日誌
            } else {
                entity.setCreateDate(new Date());
                entity.setUserId(sysuser.getId());
                entity.setSiteId(site.getId());
                entity.setDisabled(true);
                service.save(entity);// 返回主键
                // 日誌
                // 保存评论内容
                HomeCommentContent hcc = new HomeCommentContent();
                hcc.setCommentId(entity.getId());
                hcc.setContent(content);
                contentService.save(hcc);
                // 日誌
                // 評論數
                // HomeUser hUser =new HomeUser();

            }
        } catch (Exception e) {
            // 日誌
            e.printStackTrace();
            return ERROR;
        }

        return SUCCESS;
    }

    @RequestMapping("delete")
    public String delete(Integer[] ids, HttpServletRequest request, HttpSession session) {
        SysSite site = getSite(request);
        if (notEmpty(ids)) {
            service.delete(ids);
            logOperateService.save(new LogOperate(site.getId(), getAdminFromSession(session).getId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "delete.homeComment", getIpAddress(request), getDate(), join(ids, ',')));
        }
        return TEMPLATE_DONE;
    }

    @Autowired
    private HomeCommentService service;
    @Autowired
    private HomeCommentContentService contentService;
}