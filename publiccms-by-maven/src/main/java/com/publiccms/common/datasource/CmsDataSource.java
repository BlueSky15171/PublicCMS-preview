package com.publiccms.common.datasource;

import static java.lang.Integer.parseInt;
import static org.springframework.core.io.support.PropertiesLoaderUtils.loadAllProperties;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.publiccms.common.constants.CmsVersion;
import com.sanluan.common.datasource.MultiDataSource;

public class CmsDataSource extends MultiDataSource {
    private static CmsDataSource cmsDataSource;
    private String dbconfigFilePath;

    private Map<Object, Object> dataSources = new HashMap<Object, Object>();

    public CmsDataSource(String filePath) {
        dbconfigFilePath = filePath;
        cmsDataSource = this;
        if (CmsVersion.isInitialized()) {
            try {
                CmsDataSource.initDataSource();
            } catch (IOException | PropertyVetoException e) {
                CmsVersion.setInitialized(false);
            }
        }
    }

    public static void initDataSource() throws IOException, PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        Properties properties = loadAllProperties(cmsDataSource.dbconfigFilePath);
        dataSource.setDriverClass(properties.getProperty("jdbc.driverClassName"));
        dataSource.setJdbcUrl(properties.getProperty("jdbc.url"));
        dataSource.setUser(properties.getProperty("jdbc.username"));
        dataSource.setPassword(properties.getProperty("jdbc.password"));
        dataSource.setAutoCommitOnClose(Boolean.parseBoolean(properties.getProperty("cpool.autoCommitOnClose")));
        dataSource.setCheckoutTimeout(parseInt(properties.getProperty("cpool.checkoutTimeout")));
        dataSource.setInitialPoolSize(parseInt(properties.getProperty("cpool.minPoolSize")));
        dataSource.setMinPoolSize(parseInt(properties.getProperty("cpool.minPoolSize")));
        dataSource.setMaxPoolSize(parseInt(properties.getProperty("cpool.maxPoolSize")));
        dataSource.setMaxIdleTime(parseInt(properties.getProperty("cpool.maxIdleTime")));
        dataSource.setAcquireIncrement(parseInt(properties.getProperty("cpool.acquireIncrement")));
        dataSource.setMaxIdleTimeExcessConnections(parseInt(properties.getProperty("cpool.maxIdleTimeExcessConnections")));
        cmsDataSource.getDataSources().put("default", dataSource);
        cmsDataSource.setTargetDataSources(cmsDataSource.getDataSources(), dataSource);
        cmsDataSource.init();
    }

    @Override
    public void afterPropertiesSet() {

    }

    public void init() {
        super.afterPropertiesSet();
    }

    public void put(Object name, Object dataSource) {
        dataSources.put(name, dataSource);
    }

    public Map<Object, Object> getDataSources() {
        return dataSources;
    }
}
