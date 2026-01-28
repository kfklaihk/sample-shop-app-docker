package com.docker.atsea.configuration;

import java.util.Properties;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.jdbc.DataSourceBuilder;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableJpaRepositories(basePackages = "com.docker.atsea.repositories",
		entityManagerFactoryRef = "entityManagerFactory",
		transactionManagerRef = "transactionManager")
@EnableTransactionManagement
public class JpaConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(JpaConfiguration.class);
	private static final String POSTGRES_PASSWORD_SECRET = "/run/secrets/postgres_password";

	@Autowired
	private Environment environment;


	/*
	 * Populate SpringBoot DataSourceProperties from application.yml 
	 */
	@Bean
	@Primary
	@ConfigurationProperties(prefix = "datasource.atsea")
	public DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}

	/*
	 * Configure HikariCP pooled DataSource.
	 */
	@Bean
	public DataSource dataSource() {
		DataSourceProperties dataSourceProperties = dataSourceProperties();
		applyRailwayOverrides(dataSourceProperties);
		applySecretPasswordOverride(dataSourceProperties);
		logResolvedDatasource(dataSourceProperties);
			HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder
					.create(dataSourceProperties.getClassLoader())
					.driverClassName(dataSourceProperties.getDriverClassName())
					.url(dataSourceProperties.getUrl())
					.username(dataSourceProperties.getUsername())
					.password(dataSourceProperties.getPassword())
					.type(HikariDataSource.class)
					.build();
			return dataSource;
	}

	private void applyRailwayOverrides(DataSourceProperties dataSourceProperties) {
		String explicitUrl = getEnv("DATASOURCE_ATSEA_URL");
		if (StringUtils.isNotBlank(explicitUrl)) {
			dataSourceProperties.setUrl(explicitUrl);
			applyExplicitCredentials(dataSourceProperties);
			return;
		}

		String databaseUrl = getEnv("DATABASE_URL");
		if (StringUtils.isNotBlank(databaseUrl)) {
			applyDatabaseUrl(dataSourceProperties, databaseUrl);
			return;
		}

		String host = getEnv("PGHOST");
		String database = getEnv("PGDATABASE");
		if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(database)) {
			String port = defaultIfBlank(getEnv("PGPORT"), "5432");
			String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
			String sslMode = getEnv("PGSSLMODE");
			if (StringUtils.isNotBlank(sslMode)) {
				jdbcUrl = jdbcUrl + "?sslmode=" + sslMode;
			}
			dataSourceProperties.setUrl(jdbcUrl);
			String username = getEnv("PGUSER");
			if (StringUtils.isNotBlank(username)) {
				dataSourceProperties.setUsername(username);
			}
			String password = getEnv("PGPASSWORD");
			if (StringUtils.isNotBlank(password)) {
				dataSourceProperties.setPassword(password);
			}
			dataSourceProperties.setDriverClassName("org.postgresql.Driver");
			logger.info("Configured datasource from PG* environment variables.");
		}
	}

	private void applyDatabaseUrl(DataSourceProperties dataSourceProperties, String databaseUrl) {
		try {
			URI uri = new URI(databaseUrl);
			String scheme = uri.getScheme();
			if (!"postgres".equalsIgnoreCase(scheme) && !"postgresql".equalsIgnoreCase(scheme)) {
				logger.warn("DATABASE_URL scheme '{}' is not PostgreSQL, skipping override.", scheme);
				return;
			}
			String host = uri.getHost();
			int port = uri.getPort() > 0 ? uri.getPort() : 5432;
			String path = uri.getPath();
			String database = StringUtils.removeStart(path, "/");
			if (StringUtils.isBlank(host) || StringUtils.isBlank(database)) {
				logger.warn("DATABASE_URL missing host or database name, skipping override.");
				return;
			}

			String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
			String query = uri.getQuery();
			if (StringUtils.isNotBlank(query)) {
				jdbcUrl = jdbcUrl + "?" + query;
			}
			dataSourceProperties.setUrl(jdbcUrl);
			dataSourceProperties.setDriverClassName("org.postgresql.Driver");

			String userInfo = uri.getUserInfo();
			if (StringUtils.isNotBlank(userInfo)) {
				String[] parts = userInfo.split(":", 2);
				dataSourceProperties.setUsername(decodeUrlComponent(parts[0]));
				if (parts.length > 1) {
					dataSourceProperties.setPassword(decodeUrlComponent(parts[1]));
				}
			}

			logger.info("Configured datasource from DATABASE_URL.");
		} catch (URISyntaxException e) {
			logger.warn("DATABASE_URL is not a valid URI, skipping override.");
		}
	}

	private void applyExplicitCredentials(DataSourceProperties dataSourceProperties) {
		String username = getEnv("DATASOURCE_ATSEA_USERNAME");
		if (StringUtils.isNotBlank(username)) {
			dataSourceProperties.setUsername(username);
		}
		String password = getEnv("DATASOURCE_ATSEA_PASSWORD");
		if (StringUtils.isNotBlank(password)) {
			dataSourceProperties.setPassword(password);
		}
		String driverClassName = getEnv("DATASOURCE_ATSEA_DRIVERCLASSNAME");
		if (StringUtils.isNotBlank(driverClassName)) {
			dataSourceProperties.setDriverClassName(driverClassName);
		}
	}

	private void applySecretPasswordOverride(DataSourceProperties dataSourceProperties) {
		Path secretPath = Paths.get(POSTGRES_PASSWORD_SECRET);
		if (!Files.exists(secretPath)) {
			return;
		}
		try {
			String secret = Files.readString(secretPath, StandardCharsets.UTF_8).trim();
			if (StringUtils.isNotBlank(secret)) {
				dataSourceProperties.setPassword(secret);
				logger.info("Loaded database password from Docker secret file.");
			}
		} catch (IOException e) {
			logger.warn("Failed to read DB password secret file.", e);
		}
	}

	private void logResolvedDatasource(DataSourceProperties dataSourceProperties) {
		String url = dataSourceProperties.getUrl();
		if (StringUtils.isNotBlank(url)) {
			String sanitized = sanitizeJdbcUrl(url);
			logger.info("Datasource URL resolved to {}", sanitized);
		} else {
			logger.warn("Datasource URL is empty after configuration.");
		}
	}

	private String sanitizeJdbcUrl(String url) {
		int queryStart = url.indexOf('?');
		if (queryStart > -1) {
			return url.substring(0, queryStart) + "?...";
		}
		return url;
	}

	private String decodeUrlComponent(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private String defaultIfBlank(String value, String defaultValue) {
		return StringUtils.isNotBlank(value) ? value : defaultValue;
	}

	private String getEnv(String key) {
		return System.getenv(key);
	}

	/*
	 * Entity Manager Factory setup.
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws NamingException {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource());
		factoryBean.setPackagesToScan(new String[] { "com.docker.atsea.model" });
		factoryBean.setJpaVendorAdapter(jpaVendorAdapter());
		factoryBean.setJpaProperties(jpaProperties());
		return factoryBean;
	}

	/*
	 * Provider specific adapter.
	 */
	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
		return hibernateJpaVendorAdapter;
	}

	/*
	 * Provider specific properties.
	 */
	private Properties jpaProperties() {
		Properties properties = new Properties();
		properties.put("hibernate.dialect", environment.getRequiredProperty("datasource.atsea.hibernate.dialect"));
		properties.put("hibernate.hbm2ddl.auto", environment.getRequiredProperty("datasource.atsea.hibernate.hbm2ddl.method"));
		properties.put("hibernate.show_sql", environment.getRequiredProperty("datasource.atsea.hibernate.show_sql"));
		properties.put("hibernate.format_sql", environment.getRequiredProperty("datasource.atsea.hibernate.format_sql"));
		if(StringUtils.isNotEmpty(environment.getRequiredProperty("datasource.atsea.defaultSchema"))){
			properties.put("hibernate.default_schema", environment.getRequiredProperty("datasource.atsea.defaultSchema"));
		}
		return properties;
	}

	@Bean
	@Autowired
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(emf);
		return txManager;
	}

}
