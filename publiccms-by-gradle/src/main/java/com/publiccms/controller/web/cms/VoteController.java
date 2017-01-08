package com.publiccms.controller.web.cms;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.publiccms.common.base.AbstractController;
import com.publiccms.entities.cms.CmsVoteUser;
import com.publiccms.logic.service.cms.CmsVoteUserService;

@Controller
@RequestMapping("vote")
@ResponseBody
public class VoteController extends AbstractController {
    @Autowired
    private CmsVoteUserService voteUserService;

    /**
     * 
     * @param action
     * @param callback
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("save")
    public MappingJacksonValue join(CmsVoteUser entity, String callback, HttpServletRequest request, ModelMap model) {
        if (notEmpty(entity.getItemIds())) {
            voteUserService.save(entity);
        }
        return getMappingJacksonValue(model, callback);
    }
}
