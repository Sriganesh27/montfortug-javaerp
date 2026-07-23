package com.erp.montfortuganda.infrastructure.config;

import com.erp.montfortuganda.scholarship.entity.WebDonation;
import com.erp.montfortuganda.scholarship.repository.WebDonationRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.erp.montfortuganda",
        entityManagerFactoryRef = "erpEntityManagerFactory",
        transactionManagerRef = "erpTransactionManager",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebDonationRepository.class
        )
)
public class ErpDataSourceConfig
        implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(
            ResourceLoader resourceLoader
    ) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.erp")
    public DataSourceProperties erpDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = {"erpDataSource", "dataSource"})
    @Primary
    @ConfigurationProperties("app.datasource.erp.hikari")
    public HikariDataSource erpDataSource(
            @Qualifier("erpDataSourceProperties")
            DataSourceProperties properties
    ) {
        return properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public PersistenceManagedTypes erpManagedTypes() {
        return new PersistenceManagedTypesScanner(
                resourceLoader,
                className -> !WebDonation.class
                        .getName()
                        .equals(className)
        ).scan("com.erp.montfortuganda");
    }

    @Bean(name = {
            "erpEntityManagerFactory",
            "entityManagerFactory"
    })
    @Primary
    public LocalContainerEntityManagerFactoryBean
    erpEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("erpDataSource")
            DataSource dataSource,
            @Qualifier("erpManagedTypes")
            PersistenceManagedTypes managedTypes
    ) {
        return builder
                .dataSource(dataSource)
                .managedTypes(managedTypes)
                .persistenceUnit("erp")
                .properties(jpaProperties())
                .build();
    }

    @Bean(name = {
            "erpTransactionManager",
            "transactionManager"
    })
    @Primary
    public PlatformTransactionManager erpTransactionManager(
            @Qualifier("erpEntityManagerFactory")
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
