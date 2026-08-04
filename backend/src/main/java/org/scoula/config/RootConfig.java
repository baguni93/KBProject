package org.scoula.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@Import(OpenAIConfig.class)
@PropertySource({
        "classpath:/application.properties",
        "classpath:/secret.properties"
})
@MapperScan(basePackages = {
        "org.scoula.mapper",
        "org.scoula.member.mapper",
        "org.scoula.security.account.mapper",
        "org.scoula.feed.mapper",
        "org.scoula.wallet.mapper",
        "org.scoula.remittance.mapper",
        "org.scoula.pointwallet.mapper",
        "org.scoula.settlement.mapper",
        "org.scoula.notification.mapper",
        "org.scoula.friend.mapper",
        "org.scoula.comment.mapper",
        "org.scoula.like.mapper",
        "org.scoula.auth.mapper",
        "org.scoula.transaction.mapper",
        "org.scoula.analysis.mapper",
        "org.scoula.card.mapper"
})
@ComponentScan(basePackages = {
        "org.scoula.member.service",
        "org.scoula.feed.service",
        "org.scoula.wallet.service",
        "org.scoula.remittance.service",
        "org.scoula.pointwallet.service",
        "org.scoula.settlement.service",
        "org.scoula.notification.service",
        "org.scoula.friend.service",
        "org.scoula.comment.service",
        "org.scoula.like.service",
        "org.scoula.auth.service",
        "org.scoula.member.service",
        "org.scoula.analysis.service",
        "org.scoula.transaction.service",
        "org.scoula.card.service"
})
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class RootConfig {
    @Value("${jdbc.driver}") String driver;
    @Value("${jdbc.url}") String url;
    @Value("${jdbc.username}") String username;
    @Value("${jdbc.password}") String password;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setMapperLocations(applicationContext.getResources("classpath:/org/scoula/**/mapper/*.xml"));
        sqlSessionFactory.setDataSource(dataSource());
        return (SqlSessionFactory) sqlSessionFactory.getObject();
    }
    @Bean
    public DataSourceTransactionManager transactionManager(){
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());
        return manager;
    }
}
