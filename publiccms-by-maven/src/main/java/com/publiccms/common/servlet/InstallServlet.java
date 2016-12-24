package com.publiccms.common.servlet;

import static com.publiccms.common.tools.DatabaseUtils.getDataSource;
import static config.initializer.InstallationInitializer.CMS_CONFIG_FILE;
import static config.initializer.InstallationInitializer.PROPERTY_NAME_DATABASE;
import static org.springframework.core.io.support.PropertiesLoaderUtils.loadAllProperties;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.publiccms.common.constants.CmsVersion;
import com.publiccms.common.datasource.CmsDataSource;
import com.publiccms.logic.mapper.tools.SqlMapper;
import com.sanluan.common.base.Base;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class InstallServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final String STEP_CHECKDATABASE = "checkDataBase";
    public static final String STEP_DATABASECONFIG = "dataBaseConfig";
    public static final String STEP_INITDATABASE = "initDatabase";
    public static final String STEP_START = "start";
    private ServletContext servletcontext;
    private SqlSessionFactory sqlSessionFactory;
    private ComboPooledDataSource dataSource;
    private freemarker.template.Configuration freemarkerConfiguration;

    public InstallServlet(ServletContext servletcontext) {
        this.servletcontext = servletcontext;
        try {
            this.freemarkerConfiguration = new freemarker.template.Configuration(freemarker.template.Configuration.getVersion());
            freemarkerConfiguration.setDirectoryForTemplateLoading(new File(servletcontext.getRealPath("/WEB-INF/install/")));
            freemarkerConfiguration.setDefaultEncoding("utf-8");
        } catch (IOException e) {
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (CmsVersion.isInitialized()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String step = request.getParameter("step");
            Map<String, Object> map = new HashMap<String, Object>();
            if (null != step) {
                switch (step) {
                case STEP_DATABASECONFIG:
                    try {
                        Properties properties = loadAllProperties(CMS_CONFIG_FILE);
                        String databaseConfiFile = properties.getProperty(PROPERTY_NAME_DATABASE);
                        saveDataBaseConfig(databaseConfiFile, request.getParameter("host"), request.getParameter("port"),
                                request.getParameter("database"), request.getParameter("username"),
                                request.getParameter(" password"));
                        checkDataBaseConfig(databaseConfiFile);
                        map.put("message", "success");
                    } catch (PropertyVetoException | SQLException e) {
                        if (null != dataSource) {
                            dataSource.close();
                        }
                        map.put("error", e.getMessage());
                    }
                    break;
                case STEP_INITDATABASE:
                    StringWriter stringWriter = new StringWriter();
                    if (null != dataSource && null != sqlSessionFactory) {
                        SqlSession session = sqlSessionFactory.openSession();
                        try {
                            ScriptRunner runner = new ScriptRunner(dataSource.getConnection());
                            runner.setLogWriter(null);
                            runner.setErrorLogWriter(new PrintWriter(stringWriter));
                            runner.setAutoCommit(true);
                            try (FileInputStream fis = new FileInputStream(
                                    servletcontext.getRealPath("/WEB-INF/install/createtables.sql"));) {
                                runner.runScript(new InputStreamReader(fis, "UTF-8"));
                                if (null != request.getParameter("useSimple")) {
                                    runner.runScript(
                                            new FileReader(servletcontext.getRealPath("/WEB-INF/install/simpledata.sql")));
                                }
                            }
                            map.put("message", "success");
                            map.put("history", stringWriter.toString());
                        } catch (Exception e) {
                            map.put("message", "failed");
                            map.put("error", e.getMessage());
                        } finally {
                            session.close();
                        }
                    } else {
                        map.put("message", "failed");
                        map.put("error", "sqlSessionFactory is null");
                    }
                    break;
                case STEP_START:
                    if (null != dataSource && null != sqlSessionFactory) {
                        try {
                            start();
                            dataSource.close();
                            dataSource = null;
                            sqlSessionFactory = null;
                            response.sendRedirect("../");
                        } catch (PropertyVetoException e) {
                            CmsVersion.setInitialized(false);
                            map.put("error", e.getMessage());
                        }
                    }
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
            render(step, map, response);
        }
    }

    private void saveDataBaseConfig(String databaseConfiFile, String host, String port, String database, String username,
            String password) throws IOException {
        Properties dbconfigProperties = loadAllProperties(databaseConfiFile);
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:mysql://");
        sb.append(host);
        if (Base.notEmpty(port) && !"3306".equals(port)) {
            sb.append(":");
            sb.append(port);
        }
        sb.append("/");
        sb.append(database);
        sb.append("?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=round");
        dbconfigProperties.setProperty("jdbc.url", sb.toString());
        dbconfigProperties.setProperty("jdbc.username", username);
        dbconfigProperties.setProperty("jdbc.password", password);
        try (FileOutputStream fos = new FileOutputStream(
                new File(servletcontext.getRealPath("/WEB-INF/classes/" + databaseConfiFile)))) {
            dbconfigProperties.store(fos, null);
        }
    }

    private void checkDataBaseConfig(String databaseConfiFile) throws SQLException, IOException, PropertyVetoException {
        if (null != dataSource) {
            dataSource.close();
            dataSource = null;
        }
        if (null != sqlSessionFactory) {
            sqlSessionFactory = null;
        }
        dataSource = getDataSource(databaseConfiFile);
        dataSource.getConnection();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("publiccms", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(SqlMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    private void start() throws IOException, PropertyVetoException {
        CmsVersion.setInitialized(true);
        CmsDataSource.initDataSource();
    }

    private void render(String step, Map<String, Object> model, HttpServletResponse response) {
        if (!response.isCommitted()) {
            try {
                Template template = freemarkerConfiguration.getTemplate(null == step ? "index.html" : step + ".html");
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/html");
                template.process(model, response.getWriter());
            } catch (TemplateException | IOException e) {
            }
        }
    }
}
