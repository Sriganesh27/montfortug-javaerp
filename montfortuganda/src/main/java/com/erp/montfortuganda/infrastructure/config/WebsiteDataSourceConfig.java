package com.erp.montfortuganda.infrastructure.config;

import com.erp.montfortuganda.scholarship.entity.WebDonation;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipHistoryRepository;
import com.erp.montfortuganda.scholarship.repository.ErpBranchFundAllocationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipAllocationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipApplicationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipSiblingRepository;
import com.erp.montfortuganda.scholarship.repository.WebDonationRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = WebDonationRepository.class,
        entityManagerFactoryRef = "websiteEntityManagerFactory",
        transactionManagerRef = "websiteTransactionManager",
        /*
         * WebDonationRepository shares its package with ERP Scholarship
         * repositories. The website datasource must never create beans for
         * ERP repositories because those belong to the ERP datasource.
         */
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ErpBranchFundAllocationRepository.class,
                        ErpScholarshipAllocationRepository.class,
                        ErpScholarshipApplicationRepository.class,
                        ErpScholarshipHistoryRepository.class,
                        ErpScholarshipSiblingRepository.class
                }
        )
)
public class WebsiteDataSourceConfig {

    @Bean
    @ConfigurationProperties("app.datasource.website")
    public DataSourceProperties websiteDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.website.hikari")
    public HikariDataSource websiteDataSource(
            @Qualifier("websiteDataSourceProperties")
            DataSourceProperties properties
    ) {
        HikariDataSource dataSource =
                properties
                        .initializeDataSourceBuilder()
                        .type(HikariDataSource.class)
                        .build();

        /*
         * Defense-in-depth:
         *
         * The database account used by ERP for the website database must
         * already have SELECT-only privileges. This application-level
         * setting additionally marks every website connection read-only.
         *
         * ERP writes must NEVER target the website database. ERP writes
         * belong to the ERP datasource/database only.
         */
        dataSource.setReadOnly(true);

        return dataSource;
    }

    @Bean
    public PersistenceManagedTypes websiteManagedTypes() {
        return PersistenceManagedTypes.of(
                WebDonation.class.getName()
        );
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean websiteEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("websiteDataSource")
            DataSource dataSource,
            @Qualifier("websiteManagedTypes")
            PersistenceManagedTypes managedTypes
    ) {
        return builder
                .dataSource(dataSource)
                .managedTypes(managedTypes)
                .persistenceUnit("website")
                .properties(jpaProperties())
                .build();
    }

    @Bean
    public PlatformTransactionManager websiteTransactionManager(
            @Qualifier("websiteEntityManagerFactory")
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(
                entityManagerFactory
        );
    }

    private Map<String, Object> jpaProperties() {
        return Map.of(
                "hibernate.hbm2ddl.auto",
                "none",
                "hibernate.show_sql",
                "false",
                "hibernate.format_sql",
                "false",
                "hibernate.dialect",
                "org.hibernate.dialect.MySQLDialect",
                "hibernate.boot.allow_jdbc_metadata_access",
                "false",
                "hibernate.jdbc.time_zone",
                "UTC"
        );
    }
}
