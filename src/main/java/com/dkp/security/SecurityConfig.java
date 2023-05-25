package com.dkp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
	 @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	        http.authorizeHttpRequests()
	                .requestMatchers("/pay","orders").hasAnyRole(null)
//	                .hasRole("USER")
	     // ↓ /sample/admin/**  주소로 들어오는 요청은 '인증' 뿐 아니라 ROLE_ADMIN 권한을 갖고 있어야 한다 ('인가')
	        		.requestMatchers("/member/**").hasAnyRole("ADMIN","MEMBER")
	     			.requestMatchers("/admin/**").hasRole("ADMIN")
	     			.anyRequest().permitAll()
	                .and()
	                .httpBasic()
	                .and()
	                .formLogin()
					.loginPage("/login")
					.usernameParameter("userid")
					.passwordParameter("userpassword")
					.loginProcessingUrl("/loginOk")
					.defaultSuccessUrl("/")
					.and()
					.rememberMe()
					.key("secret")
					.rememberMeParameter("autoLogin")
					.tokenValiditySeconds(86400)
					.userDetailsService(null)
					.and()
					.logout()
					.logoutUrl("/logout")
					.invalidateHttpSession(true)
	                ;

	        return http.build();
	    }

}
