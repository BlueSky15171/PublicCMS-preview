package config.initializer;

import static com.publiccms.common.tools.DatabaseUtils.getDataSource;
import static org.springframework.core.io.support.PropertiesLoaderUtils.loadAllProperties;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.publiccms.common.constants.CmsVersion;
import com.publiccms.common.servlet.InstallServlet;

public class InstallationInitializer implements WebApplicationInitializer {
    public static final String CMS_CONFIG_FILE = "config/cms.properties";
    public static final String PROPERTY_NAME_DATABASE = "cms.database.configFilePath";

    @Override
    public void onStartup(ServletContext servletcontext) throws ServletException {
        ComboPooledDataSource dataSource = null;
        try {
            Properties properties = loadAllProperties(CMS_CONFIG_FILE);
            dataSource = getDataSource(properties.getProperty(PROPERTY_NAME_DATABASE));
            dataSource.getConnection();
            dataSource.close();
            CmsVersion.setInitialized(true);
        } catch (PropertyVetoException | SQLException | IOException e) {
            if (null != dataSource) {
                dataSource.close();
            }
            createInstallServlet(servletcontext);
        }
    }

    private void createInstallServlet(ServletContext servletcontext) {
        Dynamic registration = servletcontext.addServlet("install", new InstallServlet(servletcontext));
        registration.setLoadOnStartup(1);
        registration.addMapping(new String[] { "/install/*" });
    }

}
