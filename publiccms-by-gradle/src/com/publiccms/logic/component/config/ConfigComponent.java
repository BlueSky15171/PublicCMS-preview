package com.publiccms.logic.component.config;

import static com.publiccms.common.tools.ExtendUtils.getExtendMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.publiccms.common.spi.Config;
import com.publiccms.common.spi.SiteCache;
import com.publiccms.entities.sys.SysConfigData;
import com.publiccms.entities.sys.SysConfigDataId;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.site.SiteComponent;
import com.publiccms.logic.service.sys.SysConfigDataService;
import com.publiccms.views.pojo.ExtendField;
import com.publiccms.views.pojo.SysConfig;
import com.publiccms.views.pojo.SysConfigItem;
import com.sanluan.common.api.Json;
import com.sanluan.common.base.Base;
import com.sanluan.common.cache.CacheEntity;
import com.sanluan.common.cache.CacheEntityFactory;

/**
 * 
 * ConfigComponent 配置组件
 *
 */
@Component
public class ConfigComponent extends Base implements SiteCache, Json {
    @Autowired
    private SysConfigDataService service;
    @Autowired
    private List<Config> configList;
    private CacheEntity<Integer, Map<String, Map<String, String>>> cache;

    public Collection<ConfigItemInfo> getConfigCodeList(SysSite site, Locale locale) {
        Map<String, ConfigItemInfo> configItemMap = new LinkedHashMap<String, ConfigItemInfo>();
        for (Config config : configList) {
            String code = config.getCode(site);
            String description = config.getCodeDescription(site, config.getCode(site), locale);
            ConfigItemInfo configItemInfo = new ConfigItemInfo(code, description);
            configItemMap.put(code, configItemInfo);
        }
        for (Entry<String, SysConfig> entry : getMap(site).entrySet()) {
            ConfigItemInfo configItemInfo = new ConfigItemInfo(entry.getValue().getCode(), entry.getValue().getDescription());
            configItemInfo.setCustomed(true);
            configItemMap.put(entry.getKey(), configItemInfo);
        }
        return configItemMap.values();
    }

    private Map<String, List<Config>> getConfigMap(SysSite site) {
        Map<String, List<Config>> configMap = new HashMap<String, List<Config>>();
        for (Config config : configList) {
            List<Config> configList = configMap.get(config.getCode(site));
            if (empty(configList)) {
                configList = new ArrayList<Config>();
            }
            configList.add(config);
            configMap.put(config.getCode(site), configList);
        }
        return configMap;
    }

    public List<ConfigItemInfo> getConfigItemList(SysSite site, String code, Locale locale) {
        List<ConfigItemInfo> configItemList = new ArrayList<ConfigItemInfo>();
        List<Config> configList = getConfigMap(site).get(code);
        if (notEmpty(configList)) {
            for (Config config : configList) {
                for (String itemCode : config.getItemCode(site)) {
                    ConfigItemInfo configItemInfo = new ConfigItemInfo(itemCode,
                            config.getItemDescription(site, itemCode, locale));
                    configItemList.add(configItemInfo);
                }
            }
        }
        SysConfig sysConfig = getMap(site).get(code);
        if (notEmpty(sysConfig) && notEmpty(sysConfig.getConfigItemMap())) {
            for (SysConfigItem configItem : sysConfig.getConfigItemMap().values()) {
                ConfigItemInfo configItemInfo = new ConfigItemInfo(configItem.getItemCode(), configItem.getDescription());
                configItemInfo.setCustomed(true);
                configItemList.add(configItemInfo);
            }
        }
        return configItemList;
    }

    public List<ExtendField> getExtendFieldList(SysSite site, String code, String itemCode, Locale locale) {
        List<Config> configList = getConfigMap(site).get(code);
        if (notEmpty(configList)) {
            for (Config config : configList) {
                if (config.getItemCode(site).contains(itemCode)) {
                    return config.getExtendFieldList(site, itemCode, locale);
                }
            }
        }
        SysConfig sysConfig = getMap(site).get(code);
        if (notEmpty(sysConfig) && notEmpty(sysConfig.getConfigItemMap())) {
            SysConfigItem configItem = sysConfig.getConfigItemMap().get(itemCode);
            if (notEmpty(configItem)) {
                return configItem.getExtendList();
            }
        }
        return new ArrayList<ExtendField>();
    }

    public Map<String, String> getConfigData(int siteId, String code, String itemCode) {
        Map<String, Map<String, String>> siteMap = cache.get(siteId);
        if (empty(siteMap)) {
            siteMap = new HashMap<String, Map<String, String>>();
        }
        Map<String, String> configMap = siteMap.get(getKey(code, itemCode));
        if (empty(configMap)) {
            SysConfigData entity = service.getEntity(new SysConfigDataId(siteId, code, itemCode));
            if (notEmpty(entity) && notEmpty(entity.getData())) {
                configMap = getExtendMap(entity.getData());
            } else {
                configMap = new HashMap<String, String>();
            }
            siteMap.put(code, configMap);
            cache.put(siteId, siteMap);
        }
        return configMap;
    }

    @Autowired
    private SiteComponent siteComponent;

    public Map<String, SysConfig> getMap(SysSite site) {
        Map<String, SysConfig> modelMap;
        File file = new File(siteComponent.getConfigFilePath(site));
        if (notEmpty(file)) {
            try {
                modelMap = objectMapper.readValue(file, new TypeReference<Map<String, SysConfig>>() {
                });
            } catch (IOException | ClassCastException e) {
                modelMap = new HashMap<String, SysConfig>();
            }
        } else {
            modelMap = new HashMap<String, SysConfig>();
        }
        return modelMap;
    }

    /**
     * 保存配置
     * 
     * @param site
     * @param modelMap
     * @return
     */
    public boolean save(SysSite site, Map<String, SysConfig> modelMap) {
        File file = new File(siteComponent.getConfigFilePath(site));
        if (empty(file)) {
            file.getParentFile().mkdirs();
        }
        try {
            objectMapper.writeValue(file, modelMap);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private String getKey(String code, String itemCode) {
        StringBuilder sb = new StringBuilder();
        sb.append(code);
        sb.append(".");
        sb.append(itemCode);
        return sb.toString();
    }

    public void removeCache(int siteId, String code, String itemCode) {
        Map<String, Map<String, String>> map = cache.get(siteId);
        if (notEmpty(map)) {
            map.remove(getKey(code, itemCode));
        }
    }

    @Override
    public void clear(int siteId) {
        cache.remove(siteId);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Autowired
    public void initCache(CacheEntityFactory cacheEntityFactory) {
        cache = cacheEntityFactory.createCacheEntity("config");
    }

    public class ConfigItemInfo implements java.io.Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private String code;
        private String description;
        private boolean customed;

        public ConfigItemInfo(String code, String description) {
            this.code = code;
            this.description = description;
        }

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

        public boolean isCustomed() {
            return customed;
        }

        public void setCustomed(boolean customed) {
            this.customed = customed;
        }
    }
}
