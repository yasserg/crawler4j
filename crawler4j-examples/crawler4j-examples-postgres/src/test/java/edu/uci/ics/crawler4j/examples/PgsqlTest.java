package edu.uci.ics.crawler4j.examples;

import java.beans.PropertyVetoException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.ResultSet;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.connection.DockerMachine;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

public class PgsqlTest {

    private static final int port = probablyFreePort();

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .machine(DockerMachine
                    .localMachine()
                    .withAdditionalEnvironmentVariable("POSTGRES_PORT", String.valueOf(port)).build())
            .projectName(ProjectName.fromString("crawler4j"))
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("db", HealthChecks.toHaveAllPortsOpen())
            .removeConflictingContainersOnStartup(true)
            .build();

    @Test
    public void testPostgresRepository() throws Exception {
        String jdbcurl = String.format("jdbc:postgresql://%s:%d/crawler4j",
                docker.containers().container("db").port(5432).getIp(), port);

        String maxPagesToFetch = "20";
        String numberOfCrawler = "1";
        SampleLauncher.main(new String[] {maxPagesToFetch, jdbcurl, numberOfCrawler});

        ComboPooledDataSource pool = getTestPool(jdbcurl);

        int count = 0;
        try (Connection connection = pool.getConnection()) {
            ResultSet rs = connection.prepareStatement("SELECT COUNT (1) FROM webpage;").executeQuery();
            rs.next();
            count = rs.getInt(1);
            rs.close();
        }

        pool.close();

        Assert.assertThat(count, Matchers.greaterThan(0));
    }

    private ComboPooledDataSource getTestPool(String jdbcurl) throws PropertyVetoException {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass("org.postgresql.Driver");
        comboPooledDataSource.setJdbcUrl(jdbcurl);
        comboPooledDataSource.setUser("crawler4j");
        comboPooledDataSource.setPassword("crawler4j");
        comboPooledDataSource.setMaxPoolSize(1);
        comboPooledDataSource.setMinPoolSize(1);
        comboPooledDataSource.setInitialPoolSize(1);
        return comboPooledDataSource;
    }

    private static int probablyFreePort() {
        int p = 5432;
        try (ServerSocket socket1 = new ServerSocket(0)) {
            p = socket1.getLocalPort();
        } finally {
            return p;
        }
    }

}
