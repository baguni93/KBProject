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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableAsync
@Import({OpenAIConfig.class,
        RedisConfig.class})
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
        "org.scoula.cardrecommendation.mapper",

        "org.scoula.profileTest.mapper",
		"org.scoula.user.mapper",
        "org.scoula.user.mapper",
        "org.scoula.agreement.mapper",
        "org.scoula.login.mapper",
        "org.scoula.profile.mapper",
        "org.scoula.bank.mapper",
        "org.scoula.account.mapper",
        "org.scoula.notifsetting.mapper",
        "org.scoula.card.mapper",
        "org.scoula.event.mapper",
        "org.scoula.cardpayment.mapper",
        "org.scoula.insurancerecommendation.mapper",
        "org.scoula.search.mapper",
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
        "org.scoula.cardrecommendation.service",
        "org.scoula.transaction.service",
        "org.scoula.user.service",
        "org.scoula.agreement.service",
        "org.scoula.login.service",
        "org.scoula.profile.service",
        "org.scoula.bank.service",
        "org.scoula.account.service",
        "org.scoula.notifsetting.service",
        "org.scoula.card.service",
        "org.scoula.profileTest.service",
        "org.scoula.event.service",
        "org.scoula.cardpayment.service",
        "org.scoula.insurancerecommendation.service",
        "org.scoula.search.service",

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