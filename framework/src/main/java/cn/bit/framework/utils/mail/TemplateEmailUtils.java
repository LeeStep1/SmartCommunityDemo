package cn.bit.framework.utils.mail;

import cn.bit.framework.param.MailParam;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Created by terry on 2016/7/8.
 */
public class TemplateEmailUtils {


    private static Configuration freeMarkerConfiguration;

    private static JavaMailSender sender;

    private static String from;

    public static Configuration getFreeMarkerConfiguration() {
        return freeMarkerConfiguration;
    }

    public void setFreeMarkerConfiguration(Configuration freeMarkerConfiguration) {
        TemplateEmailUtils.freeMarkerConfiguration = freeMarkerConfiguration;
    }

    public static JavaMailSender getSender() {
        return sender;
    }

    public void setSender(JavaMailSender sender) {
        TemplateEmailUtils.sender = sender;
    }

    public static String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        TemplateEmailUtils.from = from;
    }

    /**
     * @param templateName 模板名称
     * @param localeName   locale
     * @param data         填充数据
     * @param mailParam    邮件参数
     * @return
     */
    public static boolean sendMail(String templateName, String localeName, Map<String, Object> data, MailParam
            mailParam) {

        MimeMessage msg = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");//由于是html邮件，不是mulitpart类型
            helper.setFrom(StringUtils.isBlank(from) ? mailParam.getFrom() : from);
            helper.setTo(mailParam.getTo());
            helper.setSubject(mailParam.getSubject());
            String htmlText = getMailText(data, LocaleUtils.toLocale(localeName), templateName);//使用模板生成html邮件内容
            helper.setText(htmlText, true);
            sender.send(msg);
            return true;
        } catch (MailException e) {
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        } catch (TemplateException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private static String getMailText(Map<String, Object> root, Locale locale, String templateName) throws
            IOException, TemplateException {
        String htmlText = "";
        Template tpl = freeMarkerConfiguration.getTemplate(templateName, locale);
        htmlText = FreeMarkerTemplateUtils.processTemplateIntoString(tpl, root);

        return htmlText;
    }
}
