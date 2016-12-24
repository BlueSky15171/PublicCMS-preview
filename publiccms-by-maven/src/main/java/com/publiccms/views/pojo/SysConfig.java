package com.publiccms.views.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SysConfig implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String code;
    private String description;
    private Map<String, SysConfigItem> configItemMap;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, SysConfigItem> getConfigItemMap() {
        return configItemMap;
    }

    public void setConfigItemMap(Map<String, SysConfigItem> configItemMap) {
        this.configItemMap = configItemMap;
    }
}
