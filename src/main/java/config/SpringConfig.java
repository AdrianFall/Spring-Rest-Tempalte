package config;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Created by Adrian on 30/03/2016.
 */
@Configuration
@PropertySource({ "classpath:properties/mail.properties" })
public class SpringConfig {

    @Autowired
    private Environment env;

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource  messageSource = new ResourceBundleMessageSource ();
        messageSource.setBasename("properties/messages");
        messageSource.setCacheSeconds(5);
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

    /*https://www.google.com/settings/u/2/security/lesssecureapps*/
    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(env.getProperty("mail.sender.host"));
        mailSender.setPort(Integer.parseInt(env.getProperty("mail.sender.port")));
        mailSender.setUsername(env.getProperty("mail.sender.username"));
        mailSender.setPassword(env.getProperty("mail.sender.password"));

        mailSender.setJavaMailProperties(mailProperties());

        return mailSender;
    }

    Properties mailProperties() {
        return new Properties() {
            {
                setProperty("mail.transport.protocol", Preconditions.checkNotNull(env.getProperty("mail.sender.transport.protocol")));
                setProperty("mail.smtp.auth", Preconditions.checkNotNull(env.getProperty("mail.sender.smtp.auth")));
                setProperty("mail.smtp.starttls.enable", env.getProperty("mail.sender.smtp.starttls.enable"));
                setProperty("mail.debug", env.getProperty("mail.sender.debug"));
            }
        };
    }

}
