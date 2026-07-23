package com.erp.montfortuganda.config;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Initializes and verifies the ERP datasource during application startup.
 *
 * <p>This prevents the first browser API request from paying the complete
 * SSH-tunnel and Hikari pool startup cost.</p>
 */
@Component
public class ErpDataSourceWarmup implements ApplicationRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ErpDataSourceWarmup.class
            );

    private static final String HEALTH_CHECK_SQL =
            "SELECT 1";

    private final DataSource erpDataSource;

    public ErpDataSourceWarmup(
            @Qualifier("erpDataSource")
            DataSource erpDataSource
    ) {
        this.erpDataSource = erpDataSource;
    }

    @Override
    public void run(
            @NonNull ApplicationArguments arguments
    ) throws Exception {

        long startedAt =
                System.currentTimeMillis();

        /*
         * SELECT 1 is intentionally database-neutral and is used only to
         * verify that the configured ERP datasource is reachable.
         */
        //noinspection SqlNoDataSourceInspection
        try (
                Connection connection =
                        erpDataSource.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                HEALTH_CHECK_SQL
                        );

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            if (
                    !resultSet.next()
                            || resultSet.getInt(1) != 1
            ) {
                throw new IllegalStateException(
                        "ERP datasource warm-up returned an invalid result."
                );
            }
        }

        LOGGER.info(
                "ERP datasource warm-up completed in {} ms.",
                System.currentTimeMillis() - startedAt
        );
    }
}