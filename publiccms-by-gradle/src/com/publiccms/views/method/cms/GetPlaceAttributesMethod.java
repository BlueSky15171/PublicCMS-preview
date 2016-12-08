package com.publiccms.views.method.cms;

import static com.publiccms.common.tools.ExtendUtils.getExtendMap;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.entities.cms.CmsPlaceAttribute;
import com.publiccms.logic.service.cms.CmsPlaceAttributeService;
import com.sanluan.common.base.BaseMethod;

import freemarker.template.TemplateModelException;

@Component
public class GetPlaceAttributesMethod extends BaseMethod {

    /*
     * (non-Javadoc)
     * 
     * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        Long id = getLong(0, arguments);
        if (notEmpty(id)) {
            CmsPlaceAttribute entity = service.getEntity(id);
            if (notEmpty(entity)) {
                return getExtendMap(entity.getData());
            }
        }
        return null;
    }
    
    
    @Override
    public boolean needAppToken() {
        return true;
    }

    @Autowired
    private CmsPlaceAttributeService service;
}
