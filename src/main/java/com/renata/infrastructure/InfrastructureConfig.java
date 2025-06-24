package com.renata.infrastructure;

import com.renata.infrastructure.file.FileStorageService;
import com.renata.infrastructure.file.impl.FileStorageServiceImpl;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.ConnectionPool.PoolConfig;
import jakarta.mail.Session;
import jakarta.validation.Validator;
import java.util.Properties;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/** Основний конфігураційний клас інфраструктури додатку. */
@Configuration
@ComponentScan("com.renata")
@PropertySource({"classpath:application.properties", "classpath:application-secrets.properties"})
public class InfrastructureConfig {

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.pool.size}")
    private int dbPoolSize;

    @Value("${db.auto.commit}")
    private boolean dbAutoCommit;

    @Value("${file.storage.root}")
    private String storageRootPath;

    @Getter
    @Value("${file.storage.reports}")
    private String reportsDirectory;

    @Value("${file.storage.allowed-extensions}")
    private String[] allowedExtensions;

    @Value("${file.storage.max-size}")
    private long maxFileSize;

    @Bean
    public ConnectionPool connectionPool() {
        PoolConfig poolConfig =
                new PoolConfig.Builder()
                        .withUrl(dbUrl)
                        .withUser(dbUsername)
                        .withPassword(dbPassword)
                        .withMaxConnections(dbPoolSize)
                        .withAutoCommit(dbAutoCommit)
                        .build();
        return new ConnectionPool(poolConfig);
    }

    @Bean
    public FileStorageService fileStorageService() {
        return new FileStorageServiceImpl(storageRootPath, allowedExtensions, maxFileSize);
    }

    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String starttlsEnable;

    @Value("${mail.username}")
    private String appUsername;

    @Value("${mail.password}")
    private String appPassword;

    @Bean
    public Session mailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", mailPort);

        return Session.getInstance(
                props,
                new jakarta.mail.Authenticator() {
                    @Override
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(appUsername, appPassword);
                    }
                });
    }
}
