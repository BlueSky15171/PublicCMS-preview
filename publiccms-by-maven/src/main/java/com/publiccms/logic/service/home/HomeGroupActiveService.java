package com.publiccms.logic.service.home;

// Generated 2016-11-19 9:58:46 by com.sanluan.common.source.SourceMaker


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.entities.home.HomeGroupActive;
import com.publiccms.logic.dao.home.HomeGroupActiveDao;
import com.sanluan.common.base.BaseService;
import com.sanluan.common.handler.PageHandler;

@Service
@Transactional
public class HomeGroupActiveService extends BaseService<HomeGroupActive> {

    @Transactional(readOnly = true)
    public PageHandler getPage(Long groupId, String itemType, 
                Long userId, 
                Integer pageIndex, Integer pageSize) {
        return dao.getPage(groupId, itemType, 
                userId, 
                pageIndex, pageSize);
    }
    
    @Autowired
    private HomeGroupActiveDao dao;
}