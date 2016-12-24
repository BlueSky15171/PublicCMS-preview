package config.spring;

import static config.initializer.InstallationInitializer.CMS_CONFIG_FILE;
import static freemarker.core.Configurable.AUTO_IMPORT_KEY;
import static freemarker.core.Configurable.AUTO_INCLUDE_KEY;
import static freemarker.core.Configurable.BOOLEAN_FORMAT_KEY;
import static freemarker.core.Configurable.DATETIME_FORMAT_KEY;
import static freemarker.core.Configurable.DATE_FORMAT_KEY;
import static freemarker.core.Configurable.LAZY_AUTO_IMPORTS_KEY;
import static freemarker.core.Configurable.LOCALE_KEY;
import static freemarker.core.Configurable.NEW_BUILTIN_CLASS_RESOLVER_KEY;
import static freemarker.core.Configurable.NUMBER_FORMAT_KEY;
import static freemarker.core.Configurable.TEMPLATE_EXCEPTION_HANDLER_KEY;
import static freemarker.core.Configurable.TIME_FORMAT_KEY;
import static freemarker.core.Configurable.URL_ESCAPING_CHARSET_KEY;
import static freemarker.template.Configuration.DEFAULT_ENCODING_KEY;
import static freemarker.template.Configuration.OUTPUT_FORMAT_KEY;
import static freemarker.template.Configuration.TEMPLATE_UPDATE_DELAY_KEY;
import static java.lang.Integer.parseInt;
import static org.springframework.core.io.support.PropertiesLoaderUtils.loadAllProperties;
import static org.springframework.scheduling.quartz.SchedulerFactoryBean.PROP_THREAD_COUNT;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.publiccms.common.analyzer.MultiTokenizerFactory;
import com.publiccms.common.datasource.CmsDataSource;
import com.publiccms.common.servlet.MultiSiteWebHttpRequestHandler;
import com.publiccms.common.view.InitializeFreeMarkerView;
import com.publiccms.logic.component.site.SiteComponent;
import com.publiccms.logic.component.template.TemplateComponent;
import com.sanluan.common.base.Base;
import com.sanluan.common.cache.CacheEntityFactory;

/**
 * <h1>ApplicationConfig</h1>
 * <p>
 * Spring配置类
 * </p>
 * <p>
 * Spring Config Class
 * </p>
 *
 */
@Configuration
@ComponentScan(basePackages = "com.publiccms", excludeFilters = { @ComponentScan.Filter(value = { Controller.class }) })
@MapperScan(basePackages = "com.publiccms.logic.mapper")
@PropertySource({ "classpath:config/properties/freemarker.properties", "classpath:" + CMS_CONFIG_FILE })
@EnableTransactionManagement
@EnableScheduling
public class ApplicationConfig extends Base {
    @Autowired
    private Environment env;
    public static String basePath;
    public static WebApplicationContext webApplicationContext;

    /**
     * <p>
     * 资源处理器
     * </p>
     * <p>
     * DefaultServletHttpRequestHandler
     * </p>
     * 
     * @return
     */
    @Bean
    public HttpRequestHandler defaultServlet() {
        DefaultServletHttpRequestHandler bean = new DefaultServletHttpRequestHandler();
        return bean;
    }

    /**
     * <p>
     * 站点静态页面处理器
     * </p>
     * <p>
     * DefaultServletHttpRequestHandler
     * </p>
     * 
     * @return
     */
    @Bean
    public HttpRequestHandler webServlet(SiteComponent siteComponent) {
        MultiSiteWebHttpRequestHandler bean = new MultiSiteWebHttpRequestHandler(siteComponent);
        return bean;
    }

    /**
     * <p>
     * 数据源
     * </P>
     * <p>
     * data source
     * </p>
     *
     * @return
     * @throws PropertyVetoException
     */
    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        CmsDataSource bean = new CmsDataSource(env.getProperty("cms.database.configFilePath"));
        return bean;
    }

    /**
     * <p>
     * Hibernate 事务管理
     * </p>
     * <p>
     * Hibernate Transaction Manager
     * </p>
     *
     * @return
     */
    @Bean
    public HibernateTransactionManager hibernateTransactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager bean = new HibernateTransactionManager();
        bean.setSessionFactory(sessionFactory);
        return bean;
    }

    /**
     * <p>
     * Mybatis 会话工厂
     * </p>
     * <p>
     * Mybatis Session Factory
     * </p>
     * 
     * @return
     * @throws IOException
     */
    @Bean
    public SqlSessionFactoryBean mybatisSqlSessionFactoryBean(DataSource dataSource) throws IOException {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(false);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        bean.setMapperLocations(resolver.getResources("classpath*:com/publiccms/logic/mapper/**/*Mapper.xml"));
        bean.setConfiguration(configuration);
        return bean;
    }

    /**
     * <p>
     * Hibernate 会话工厂类
     * </p>
     * <p>
     * Hibernate Session Factory
     * </p>
     *
     * @return
     * @throws PropertyVetoException
     * @throws IOException
     */
    @Bean
    public FactoryBean<SessionFactory> hibernateSessionFactory(DataSource dataSource) throws PropertyVetoException, IOException {
        LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setPackagesToScan(new String[] { "com.publiccms.entities" });
        MultiTokenizerFactory.setName(env.getProperty("cms.tokenizerFactory"));
        bean.setHibernateProperties(getHibernateConfig(env.getProperty("cms.hibernate.configFilePath")));
        return bean;
    }

    private Properties getHibernateConfig(String configFilePath) throws IOException {
        Properties properties = loadAllProperties(configFilePath);
        properties.setProperty("hibernate.search.default.indexBase", getDirPath("/indexes/"));
        return properties;
    }

    /**
     * <p>
     * 缓存工厂
     * </p>
     * <p>
     * Cache Factory
     * </p>
     * 
     * @return
     * @throws IOException
     */
    @Bean
    public CacheEntityFactory cacheEntityFactory() throws IOException {
        CacheEntityFactory bean = new CacheEntityFactory(env.getProperty("cms.cache.configFilePath"));
        return bean;
    }

    /**
     * <p>
     * 国际化处理
     * </p>
     * <p>
     * Internationalization
     * </p>
     *
     * @return
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource bean = new ResourceBundleMessageSource();
        bean.setBasenames(new String[] { "config.language.message", "config.language.config", "config.language.operate" });
        bean.setCacheSeconds(300);
        bean.setUseCodeAsDefaultMessage(true);
        return bean;
    }

    /**
     * <p>
     * 模板组件
     * </p>
     * <p>
     * Template Component
     * </p>
     *
     * @return
     */
    @Bean
    public TemplateComponent templateComponent() {
        TemplateComponent bean = new TemplateComponent();
        bean.setDirectivePrefix(env.getProperty("freeMarkerExtendHandler.directivePrefix"));
        bean.setDirectiveRemoveRegex(env.getProperty("freeMarkerExtendHandler.directiveRemoveRegex"));
        bean.setMethodRemoveRegex(env.getProperty("freeMarkerExtendHandler.methodRemoveRegex"));
        return bean;
    }

    /**
     * <p>
     * 站点组件
     * </p>
     * <p>
     * Site Component
     * </p>
     *
     * @return
     */
    @Bean
    public SiteComponent siteComponent() {
        SiteComponent bean = new SiteComponent();
        bean.setRootPath(getDirPath(""));
        bean.setSiteMasters(env.getProperty("cms.masterSiteIds"));
        bean.setDefaultSiteId(parseInt(env.getProperty("cms.defaultSiteId")));
        return InitializeFreeMarkerView.siteComponent = bean;
    }

    /**
     * <p>
     * FreeMarker配置工厂
     * </p>
     * <p>
     * FreeMarker Configuration Factory
     * </p>
     * 
     * @return
     */
    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer bean = new FreeMarkerConfigurer();
        bean.setTemplateLoaderPath("/WEB-INF/");
        Properties properties = new Properties();
        properties.setProperty(NEW_BUILTIN_CLASS_RESOLVER_KEY, env.getProperty("freemarkerSettings.newBuiltinClassResolver"));
        properties.setProperty(TEMPLATE_UPDATE_DELAY_KEY, env.getProperty("freemarkerSettings.templateUpdateDelay"));
        properties.setProperty(DEFAULT_ENCODING_KEY, env.getProperty("freemarkerSettings.defaultEncoding"));
        properties.setProperty(URL_ESCAPING_CHARSET_KEY, env.getProperty("freemarkerSettings.urlEscapingCharset"));
        properties.setProperty(LOCALE_KEY, env.getProperty("freemarkerSettings.locale"));
        properties.setProperty(BOOLEAN_FORMAT_KEY, env.getProperty("freemarkerSettings.booleanFormat"));
        properties.setProperty(DATETIME_FORMAT_KEY, env.getProperty("freemarkerSettings.datetimeFormat"));
        properties.setProperty(DATE_FORMAT_KEY, env.getProperty("freemarkerSettings.dateFormat"));
        properties.setProperty(TIME_FORMAT_KEY, env.getProperty("freemarkerSettings.timeFormat"));
        properties.setProperty(NUMBER_FORMAT_KEY, env.getProperty("freemarkerSettings.numberFormat"));
        properties.setProperty(OUTPUT_FORMAT_KEY, env.getProperty("freemarkerSettings.outputFormat"));
        properties.setProperty(AUTO_IMPORT_KEY, env.getProperty("freemarkerSettings.autoImport"));
        properties.setProperty(LAZY_AUTO_IMPORTS_KEY, env.getProperty("freemarkerSettings.lazyAutoImports"));
        properties.setProperty(AUTO_INCLUDE_KEY, env.getProperty("freemarkerSettings.autoInclude"));
        properties.setProperty(TEMPLATE_EXCEPTION_HANDLER_KEY, env.getProperty("freemarkerSettings.templateExceptionHandler"));
        bean.setFreemarkerSettings(properties);
        return bean;
    }

    /**
     * <p>
     * 文件上传解决方案
     * </p>
     * <p>
     * File Upload Resolver
     * </p>
     * 
     * @return
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver bean = new CommonsMultipartResolver();
        bean.setDefaultEncoding(DEFAULT_CHARSET_NAME);
        bean.setMaxUploadSize(Long.parseLong(env.getProperty("cms.multipart.maxUploadSize")));
        return bean;
    }

    /**
     * <p>
     * 任务计划工厂
     * </p>
     * <p>
     * Task Scheduler Factory
     * </p>
     * 
     * @return
     */
    @Bean
    public SchedulerFactoryBean scheduler() {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        Properties properties = new Properties();
        properties.setProperty(PROP_THREAD_COUNT, env.getProperty("cms.task.threadCount"));
        bean.setQuartzProperties(properties);
        return bean;
    }

    /**
     * <p>
     * json、Jsonp消息转换适配器，用于支持RequestBody、ResponseBody
     * </p>
     * <p>
     * Json、Jsonp Message Converter , Support For RequestBody、ResponseBody
     * </p>
     * 
     * @return
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

    private String getDirPath(String path) {
        String filePath = env.getProperty("cms.filePath", basePath) + path;
        File dir = new File(filePath);
        dir.mkdirs();
        return dir.getAbsolutePath();
    }
}