package com.publiccms.controller.web.cms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.publiccms.common.base.AbstractController;
import com.publiccms.entities.cms.CmsLottery;
import com.publiccms.entities.cms.CmsLotteryUser;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.service.cms.CmsLotteryService;
import com.publiccms.logic.service.cms.CmsLotteryUserService;
import com.sanluan.common.handler.PageHandler;

@Controller
@RequestMapping("lottery")
@ResponseBody
public class LotteryController extends AbstractController {
    @Autowired
    private CmsLotteryUserService lotteryUserService;
    @Autowired
    private CmsLotteryService lotteryService;

    @RequestMapping("lottery")
    public MappingJacksonValue lottery(CmsLotteryUser entity, String callback, HttpServletRequest request, HttpSession session,
            ModelMap model) {
        SysSite site = getSite(request);
        SysUser user = getAdminFromSession(session);
        CmsLottery lottery = lotteryService.getEntity(entity.getLotteryId());
        if (empty(lottery) || verifyNotEquals("siteId", site.getId(), lottery.getSiteId(), model)
                || verifyNotEquals("siteId", site.getId(), lottery.getSiteId(), model)) {
            return getMappingJacksonValue(model, callback);
        }
        if (notEmpty(lottery) && !lottery.isDisabled()) {
            if (lotteryUserService.getPage(entity.getLotteryId(), entity.getUserId(), true, null, null, null, null, null)
                    .getTotalCount() == 0) {
                if (lottery.getLotteryCount() - lotteryUserService
                        .getPage(entity.getLotteryId(), entity.getUserId(), null, null, null, null, null, null)
                        .getTotalCount() > 0) {
                    entity.setUserId(user.getId());
                    if (lottery.getFractions() > r.nextInt(lottery.getNumerator()) && lottery.getLastGift() > 0
                            && lottery.getLotteryCount() > lotteryUserService
                                    .getPage(entity.getLotteryId(), null, true, null, null, null, null, null).getTotalCount()) {
                        entity.setWinning(true);
                    }
                    lotteryUserService.save(entity);
                    model.addAttribute("result", "success");
                    model.addAttribute("winning", entity.isWinning());
                }
            }
        }
        return getMappingJacksonValue(model, callback);
    }

    @RequestMapping("check")
    public MappingJacksonValue check(Long lotteryId, String callback, HttpServletRequest request, HttpSession session,
            ModelMap model) {
        SysSite site = getSite(request);
        SysUser user = getAdminFromSession(session);
        CmsLottery lottery = lotteryService.getEntity(lotteryId);
        if (empty(lottery) || verifyNotEquals("siteId", site.getId(), lottery.getSiteId(), model)
                || verifyNotEquals("siteId", site.getId(), lottery.getSiteId(), model)) {
            return getMappingJacksonValue(model, callback);
        }
        if (lotteryUserService.getPage(lotteryId, user.getId(), true, null, null, null, null, null).getTotalCount() == 0) {
            model.addAttribute("winning", false);
            PageHandler page = lotteryUserService.getPage(lotteryId, user.getId(), null, null, null, null, null, null);
            model.addAttribute("lastCount", lottery.getLotteryCount() - page.getTotalCount());
        } else {
            model.addAttribute("winning", true);
        }
        model.addAttribute("result", "success");
        model.addAttribute("result", "error");
        return getMappingJacksonValue(model, callback);
    }
}
