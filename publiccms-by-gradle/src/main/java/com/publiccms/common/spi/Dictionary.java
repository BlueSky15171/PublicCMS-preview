package com.publiccms.common.spi;

import java.util.List;

import com.publiccms.views.pojo.DictionaryData;

public interface Dictionary {
    public String getName();

    public String getCode();

    public List<DictionaryData> getDataList();
}