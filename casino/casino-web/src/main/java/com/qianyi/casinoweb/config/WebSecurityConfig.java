package com.qianyi.casinoweb.config;

import com.qianyi.casinoweb.config.security.filter.CusAuthenticationProcessingFilter;
import com.qianyi.casinoweb.config.security.filter.MyAuthenticationFilter;
import com.qianyi.casinoweb.config.security.filter.ValidateCodeFilter;
import com.qianyi.casinoweb.config.security.login.CusAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyAuthenticationFilter myAuthenticationFilter;

    @Autowired
    private CusAuthenticationEntryPoint cusAuthenticationEntryPoint;

    @Autowired
    private CusAuthenticationProcessingFilter cusAuthenticationProcessingFilter;

    @Autowired
    private ValidateCodeFilter validateCodeFilter;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and().logout().permitAll();


        http.addFilterAt(cusAuthenticationProcessingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(myAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(validateCodeFilter, CusAuthenticationProcessingFilter.class)
                ;

    }
}
