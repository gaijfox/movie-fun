package org.superbiz.moviefun;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}
)
public class Application {
    Map<String, String> env = System.getenv();

    public Application() {
    }

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, new String[]{"/moviefun/*"});
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials() {
        String vCapServices = (String)this.env.get("VCAP_SERVICES");
        System.out.println("----> " + vCapServices);
        return new DatabaseServiceCredentials(vCapServices);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql"));
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql"));
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localDBFactoryBeanForAlbums(DataSource albumsDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(albumsDataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan(new String[]{"org.superbiz.moviefun.albums"});
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("albums-unit");
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localDBFactoryBeanForMovies(DataSource moviesDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(moviesDataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan(new String[]{"org.superbiz.moviefun.movies"});
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("movies-unit");
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager albumsTransactionManager(EntityManagerFactory localDBFactoryBeanForAlbums) {
        JpaTransactionManager platformTransactionManager = new JpaTransactionManager();
        platformTransactionManager.setEntityManagerFactory(localDBFactoryBeanForAlbums);
        return platformTransactionManager;
    }

    @Bean
    public PlatformTransactionManager moviesTransactionManager(EntityManagerFactory localDBFactoryBeanForMovies) {
        JpaTransactionManager platformTransactionManager = new JpaTransactionManager();
        platformTransactionManager.setEntityManagerFactory(localDBFactoryBeanForMovies);
        return platformTransactionManager;
    }

    @Bean
    public TransactionOperations albumsTransactionOperations(PlatformTransactionManager albumsTransactionManager) {
        return new TransactionTemplate(albumsTransactionManager);
    }

    @Bean
    public TransactionOperations moviesTransactionOperations(PlatformTransactionManager moviesTransactionManager) {
        return new TransactionTemplate(moviesTransactionManager);
    }
}