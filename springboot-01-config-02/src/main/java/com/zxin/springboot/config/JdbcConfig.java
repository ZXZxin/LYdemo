package com.zxin.springboot.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
//@PropertySource("classpath:jdbc.properties")  //这里不需要了
@EnableConfigurationProperties(JdbcProperties.class)  // 使用某个配置属性 (这里使用我们的属性JdbcProperties)
public class JdbcConfig {

    @Bean // Spring会调用这个, 因为注解EnableConfigurationProperties会应用那个属性
    public DataSource dataSource(JdbcProperties prop){
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(prop.getDriverClassName());
        dataSource.setUrl(prop.getUrl());
        dataSource.setUsername(prop.getUrl());
        dataSource.setPassword(prop.getPassword());
        return dataSource;
    }
}
