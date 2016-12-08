package com.publiccms.logic.component.site;

import static com.sanluan.common.tools.LanguagesUtils.getMessage;
import static org.apache.commons.logging.LogFactory.getLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.publiccms.common.spi.Config;
import com.publiccms.common.spi.SiteCache;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.config.ConfigComponent;
import com.publiccms.views.pojo.ExtendField;
import com.sanluan.common.base.Base;
import com.sanluan.common.cache.CacheEntity;
import com.sanluan.common.cache.CacheEntityFactory;

/**
 * 
 * MailComponent 邮件发送组件
 *
 */
@Component
public class EmailComponent extends Base implements SiteCache, Config {
    public static final String CONFIG_CODE = "email";
    public static final String CONFIG_SUBCODE = "emailserver";
    public static final String CONFIG_EMAIL_SMTP_DEFAULTENCODING = "defaultEncoding";
    public static final String CONFIG_EMAIL_SMTP_HOST = "host";
    public static final String CONFIG_EMAIL_SMTP_PORT = "port";
    public static final String CONFIG_EMAIL_SMTP_USERNAME = "username";
    public static final String CONFIG_EMAIL_SMTP_PASSWORD = "password";
    public static final String CONFIG_EMAIL_SMTP_TIMEOUT = "timeout";
    public static final String CONFIG_EMAIL_SMTP_AUTH = "auth";
    public static final String CONFIG_EMAIL_SMTP_FROMADDRESS = "fromAddress";
    public static final String CONFIG_CODE_DESCRIPTION = CONFIGPREFIX + CONFIG_CODE;
    public static final String CONFIG_SUBCODE_DESCRIPTION = CONFIG_CODE_DESCRIPTION + "." + CONFIG_SUBCODE;

    @Autowired
    private ConfigComponent configComponent;

    private CacheEntity<Integer, JavaMailSenderImpl> cache;

    private static ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public JavaMailSender getMailSender(int siteId, Map<String, String> config) {
        JavaMailSenderImpl javaMailSender = cache.get(siteId);
        if (empty(javaMailSender)) {
            javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setDefaultEncoding(config.get(CONFIG_EMAIL_SMTP_DEFAULTENCODING));
            javaMailSender.setHost(config.get(CONFIG_EMAIL_SMTP_HOST));
            javaMailSender.setPort(Integer.parseInt(config.get(CONFIG_EMAIL_SMTP_PORT)));
            javaMailSender.setUsername(config.get(CONFIG_EMAIL_SMTP_USERNAME));
            javaMailSender.setPassword(config.get(CONFIG_EMAIL_SMTP_PASSWORD));
            Properties properties = new Properties();
            properties.setProperty("mail.smtp.auth", config.get(CONFIG_EMAIL_SMTP_AUTH));
            properties.setProperty("mail.smtp.timeout", config.get(CONFIG_EMAIL_SMTP_TIMEOUT));
            javaMailSender.setJavaMailProperties(properties);
            cache.put(siteId, javaMailSender);
        }
        return javaMailSender;
    }

    /**
     * @param toAddress
     * @param title
     * @param content
     * @return
     * @throws MessagingException
     */
    public boolean send(int siteId, String toAddress, String title, String content) throws MessagingException {
        return send(siteId, toAddress, title, content, false);
    }

    /**
     * @param toAddress
     * @param title
     * @param html
     * @return
     * @throws MessagingException
     */
    public boolean sendHtml(int siteId, String toAddress, String title, String html) throws MessagingException {
        return send(siteId, toAddress, title, html, true);
    }

    /**
     * @param toAddress
     * @param fromAddress
     * @param title
     * @param content
     * @param isHtml
     * @return
     * @throws MessagingException
     */
    private boolean send(int siteId, String toAddress, String title, String content, boolean isHtml) throws MessagingException {
        Map<String, String> config = configComponent.getConfigData(siteId, CONFIG_CODE, CONFIG_SUBCODE);
        if (notEmpty(config) && notEmpty(config.get(CONFIG_EMAIL_SMTP_FROMADDRESS))) {
            JavaMailSender mailSender = getMailSender(siteId, config);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, false, DEFAULT_CHARSET_NAME);
            messageHelper.setTo(toAddress);
            messageHelper.setFrom(config.get(CONFIG_EMAIL_SMTP_FROMADDRESS));
            messageHelper.setSubject(title);
            messageHelper.setText(content, isHtml);
            pool.execute(new SendTask(mailSender, message));
            return true;
        }
        return false;
    }

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
        itemCodeList.add(CONFIG_SUBCODE);
        return itemCodeList;
    }

    @Override
    public String getItemDescription(SysSite site, String itemCode, Locale locale) {
        return getMessage(locale, CONFIG_SUBCODE_DESCRIPTION);
    }

    @Override
    public List<ExtendField> getExtendFieldList(SysSite site, String itemCode, Locale locale) {
        List<ExtendField> extendFieldList = new ArrayList<ExtendField>();
        if (CONFIG_SUBCODE.equals(itemCode)) {
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_SMTP_DEFAULTENCODING, INPUTTYPE_TEXT, true,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_SMTP_DEFAULTENCODING), null,
                    DEFAULT_CHARSET_NAME));
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_SMTP_HOST, INPUTTYPE_TEXT, true,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_SMTP_HOST), null, null));
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_SMTP_PORT, INPUTTYPE_NUMBER, true,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_SMTP_PORT), null, String.valueOf(25)));
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_SMTP_USERNAME, INPUTTYPE_TEXT, true,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_SMTP_USERNAME), null, null));
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_SMTP_PASSWORD, INPUTTYPE_PASSWORD, true,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_SMTP_PASSWORD), null, null));
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_SMTP_TIMEOUT, INPUTTYPE_NUMBER, true,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_SMTP_TIMEOUT), null,
                    String.valueOf(3000)));
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_SMTP_AUTH, INPUTTYPE_BOOLEAN, true,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_SMTP_AUTH), null, null));
            extendFieldList.add(new ExtendField(CONFIG_EMAIL_SMTP_FROMADDRESS, INPUTTYPE_EMAIL, true,
                    getMessage(locale, CONFIG_SUBCODE_DESCRIPTION + "." + CONFIG_EMAIL_SMTP_FROMADDRESS), null, null));
        }
        return extendFieldList;
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
        cache = cacheEntityFactory.createCacheEntity(CONFIG_SUBCODE, CacheEntityFactory.MEMORY_CACHE_ENTITY);
    }
}

/**
 * 
 * SendTask 邮件发送线程
 *
 */
class SendTask implements Runnable {
    private JavaMailSender mailSender;
    private MimeMessage message;
    private final Log log = getLog(getClass());

    public SendTask(JavaMailSender mailSender, MimeMessage message) {
        this.message = message;
        this.mailSender = mailSender;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < 3) {
            try {
                mailSender.send(message);
                break;
            } catch (Exception e) {
                i++;
                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage());
                }
            }
        }
    }
}
