package com.publiccms.logic.service.cms;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.entities.cms.CmsContentAttribute;
import com.sanluan.common.base.BaseService;

@Service
@Transactional
public class CmsContentAttributeService extends BaseService<CmsContentAttribute> {
    private String[] ignoreProperties = new String[] { "contentId" };

    public void updateAttribute(Long contentId, CmsContentAttribute entity) {
        CmsContentAttribute attribute = getEntity(contentId);
        if (notEmpty(attribute)) {
            if (notEmpty(entity)) {
                update(attribute.getContentId(), entity, ignoreProperties);
            } else {
                delete(attribute.getContentId());
            }
        } else {
            if (notEmpty(entity)) {
                entity.setContentId(contentId);
                save(entity);
            }
        }
    }
}