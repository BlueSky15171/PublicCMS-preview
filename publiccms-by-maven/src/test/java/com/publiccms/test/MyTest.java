package com.publiccms.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.entities.sys.SysExtend;
import com.publiccms.entities.sys.SysExtendField;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.template.ModelComponent;
import com.publiccms.logic.mapper.tools.SqlMapper;
import com.publiccms.logic.service.sys.SysExtendFieldService;
import com.publiccms.logic.service.sys.SysExtendService;
import com.publiccms.logic.service.sys.SysSiteService;
import com.publiccms.views.pojo.CmsModel;
import com.publiccms.views.pojo.ExtendField;
import com.sanluan.common.base.Base;

import config.spring.ApplicationConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfig.class)
@Transactional
public class MyTest extends Base {
    @Autowired
    private SqlMapper toolsMapper;
    @Autowired
    private ModelComponent modelComponent;
    @Autowired
    private SysSiteService siteService;
    @Autowired
    private SysExtendService extendService;
    @Autowired
    private SysExtendFieldService extendFieldService;

    @Test
    @Commit
    public void UpdateTo2017() {
        List<Map<String, Object>> modelList = toolsMapper.select("select * from cms_model");
        for (Map<String, Object> model : modelList) {
            SysSite site = siteService.getEntity((Integer) model.get("site_id"));
            Map<String, CmsModel> modelMap = modelComponent.getMap(site);
            modelMap.remove(String.valueOf(model.get("id")));
            CmsModel entity = new CmsModel();
            entity.setId(String.valueOf(model.get("id")));
            entity.setHasChild((Boolean) model.get("has_child"));
            entity.setHasFiles((Boolean) model.get("has_files"));
            entity.setHasImages((Boolean) model.get("has_images"));
            entity.setName((String) model.get("name"));
            entity.setOnlyUrl((Boolean) model.get("only_url"));
            if (notEmpty(model.get("parent_id"))) {
                entity.setParentId(String.valueOf(model.get("parent_id")));
            }
            entity.setTemplatePath((String) model.get("template_path"));
            if (notEmpty(model.get("extend_id"))) {
                SysExtend extend = extendService.getEntity((Integer) model.get("extend_id"));
                if (notEmpty(extend)) {
                    List<ExtendField> extendList = new ArrayList<ExtendField>();
                    List<SysExtendField> list = extendFieldService.getList(extend.getId());
                    for (SysExtendField ef : list) {
                        ExtendField e = new ExtendField(ef.getId().getCode(), ef.getInputType(), ef.isRequired(), ef.getName(),
                                ef.getDescription(), ef.getDefaultValue());
                        extendList.add(e);
                        extendFieldService.delete(ef.getId());
                    }
                    extendService.delete(extend.getId());
                    entity.setExtendList(extendList);
                }
            }
            modelMap.put(entity.getId(), entity);
            modelComponent.save(site, modelMap);
        }
        toolsMapper.delete("drop table cms_model");
    }
}