package com.publiccms.logic.dao.cms;

// Generated 2015-5-8 16:50:23 by com.sanluan.common.source.SourceMaker

import org.springframework.stereotype.Repository;

import com.publiccms.entities.cms.CmsContentAttribute;
import com.sanluan.common.base.BaseDao;

@Repository
public class CmsContentAttributeDao extends BaseDao<CmsContentAttribute> {

    @Override
    protected CmsContentAttribute init(CmsContentAttribute entity) {
        if (empty(entity.getSource())) {
            entity.setSource(null);
        }
        if (empty(entity.getSourceUrl())) {
            entity.setSourceUrl(null);
        }
        return entity;
    }

}