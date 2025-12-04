package com.docker.atsea.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
		
	@Autowired
    DataSource dataSource;
 
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery("SELECT username, password, enabled FROM customer WHERE username = ?")
               .authoritiesByUsernameQuery("SELECT username, role FROM customer WHERE username=?");
    }
	
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		http
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
            .httpBasic()
            .and()
            .csrf().disable();
		return http.build();
	}	

}
