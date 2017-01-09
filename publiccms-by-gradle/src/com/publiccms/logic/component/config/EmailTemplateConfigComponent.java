package com.publiccms.logic.component.config;

import static com.publiccms.logic.component.site.EmailComponent.CONFIG_CODE;
import static com.sanluan.common.tools.LanguagesUtils.getMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.spi.Config;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.site.EmailComponent;
import com.publiccms.views.pojo.ExtendField;
import com.sanluan.common.base.Base;

/**
 *
 * EmailTemplateConfigComponent 邮件模板配置组件
 *
 */
@Component
public class EmailTemplateConfigComponent extends Base implements Config {
    public static final String CONFIG_SUBCODE = "template";
    public static final String CONFIG_EMAIL_TITLE = "email_title";
    public static final String CONFIG_EMAIL_PATH = "email_path";
    public static final String CONFIG_CODE_DESCRIPTION = CONFIGPREFIX + CONFIG_CODE;
    public static final String CONFIG_SUBCODE_DESCRIPTION = CONFIG_CODE_DESCRIPTION + "." + CONFIG_SUBCODE;

    @Autowired
    private ConfigComponent configComponent;

    @Override
    public String getCode(SysSite site) {
        return CONFIG_CODE;
    }

    @Override
    public String getCodeDescription(SysSite site, String code, Locale locale) {
        return getMessage(locale, CONFIG_CODE_DESCRIPTION);
    }

    @Override
    public List<String> getItemCode(SysSite site) {
        List<String> itemCodeList = new ArrayList<String>();
        Map<String, String> config = configComponent.getConfigData(site.getId(), CONFIG_CODE, EmailComponent.CONFIG_SUBCODE);
        if (notEmpty(config)) {
            itemCodeList.add(CONFIG_SUBCODE);
        }
        return itemCodeList;
    }

    @Override
    public String getItemDescription(SysSite site, String itemCode, Locale locale) {
        return getMessage(locale, CONFIG_CODE_DESCRIPTION);
    }

    @Override
    public List<ExtendField> getExtendFieldList(SysSite site, String itemCode, Locale locale) {
        List<ExtendField> extendFieldList = new ArrayList<ExtendField>();
        if (CONFIG_SUBCODE.equals(itemCode)) {
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_TITLE, INPUTTYPE_TEXT, false,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_TITLE), null, null));
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_PATH, INPUTTYPE_TEMPLATE, false,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_PATH), null, null));
        }
        return extendFieldList;
    }
}
