package com.publiccms.views.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sanluan.common.base.Base;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SysConfigItem extends Base implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String code;
    private String itemCode;
    private String description;
    private List<ExtendField> extendList;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ExtendField> getExtendList() {
        return extendList;
    }

    public void setExtendList(List<ExtendField> extendList) {
        this.extendList = extendList;
    }
}
