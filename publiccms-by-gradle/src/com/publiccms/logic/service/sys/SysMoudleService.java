package com.publiccms.logic.service.sys;

// Generated 2015-7-22 13:48:39 by com.sanluan.common.source.SourceMaker

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.entities.sys.SysMoudle;
import com.publiccms.logic.dao.sys.SysMoudleDao;
import com.sanluan.common.base.BaseService;
import com.sanluan.common.handler.PageHandler;

@Service
@Transactional
public class SysMoudleService extends BaseService<SysMoudle> {

    @Autowired
    private SysMoudleDao dao;

    @Transactional(readOnly = true)
    public PageHandler getPage(Integer parentId, Integer pageIndex, Integer pageSize) {
        return dao.getPage(parentId, pageIndex, pageSize);
    }
}