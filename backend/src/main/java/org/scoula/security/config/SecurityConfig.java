package org.scoula.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mybatis.spring.annotation.MapperScan;
import org.scoula.security.filter.AuthenticationErrorFilter;
import org.scoula.security.filter.JwtAuthenticationFilter;
import org.scoula.security.filter.JwtUsernamePasswordAuthenticationFilter;
import org.scoula.security.handler.CustomAccessDeniedHandler;
import org.scoula.security.handler.CustomAuthenticationEntryPoint;
import org.scoula.security.handler.LoginFailureHandler;
import org.scoula.security.handler.LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@Log4j2
@MapperScan(basePackages = {"org.scoula.security.account.mapper"})
@ComponentScan(basePackages = {
        "org.scoula.security.account.service",
        "org.scoula.security.filter",
        "org.scoula.security.handler",
        "org.scoula.security.util",
})
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationErrorFilter authenticationErrorFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        // 예외 처리 설정
        http.exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler);


        http.addFilterBefore(authenticationErrorFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class);

        http.httpBasic().disable() // 기본 HTTP 인증 비활성화
                .csrf().disable() // CSRF 비활성화
                .formLogin().disable() // formLogin 비활성화 관련 필터 해제 (ex UsernamePasswordAuthenticationFilter)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션 생성 모드 설정

            //이미 만들어진 Authentication을 가지고 "이 사용자가 이 URL에 들어갈 수 있는지" 판단하는
            //FilterSecurityInterceptor의 접근 제어 규칙 설정
        http
                .authorizeRequests() // 경로별 접근 권한 설정
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(HttpMethod.PUT,"/api/member").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/member/*/changepassword").authenticated()
                .anyRequest().permitAll();

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.info("SecurityConfig.........................................");

        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public JwtUsernamePasswordAuthenticationFilter jwtFilter(AuthenticationManager authenticationManager) {

        return new JwtUsernamePasswordAuthenticationFilter(
                authenticationManager,
                loginSuccessHandler,
                loginFailureHandler
        );
    }


    //프론트(Vue, React)에서 다른 Origin으로 API를 호출할 수 있도록 허용
    @Bean
    public CorsFilter corsFilter() {
        // URL 패턴별 CORS 정책을 저장하는 객체 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 하나의 CORS 정책(Configuration) 생성
        CorsConfiguration config = new CorsConfiguration();

        // --------------------------------------------------------------------
        // 인증 정보(Cookie, Session, Authorization Header 등)를
        // 다른 Origin에서도 함께 전송할 수 있도록 허용한다.
        // (JWT Authorization Header, Session 로그인 등에 필요)
        // --------------------------------------------------------------------
        config.setAllowCredentials(true);

        // --------------------------------------------------------------------
        // 모든 Origin(도메인)에서의 요청을 허용한다.
        // 예)
        //   http://localhost:5173
        //   http://localhost:3000
        //   https://example.com
        //
        // allowCredentials(true)와 함께 사용할 경우
        // addAllowedOrigin("*") 대신 addAllowedOriginPattern("*")을 사용해야 한다.
        // --------------------------------------------------------------------
        config.addAllowedOriginPattern("*");

        // --------------------------------------------------------------------
        // 요청에 포함될 모든 HTTP Header를 허용한다.
        // 예)
        //   Authorization
        //   Content-Type
        //   Accept
        //   X-Requested-With
        // --------------------------------------------------------------------
        config.addAllowedHeader("*");

        // --------------------------------------------------------------------
        // 모든 HTTP Method를 허용한다.
        // GET, POST, PUT, PATCH, DELETE, OPTIONS ...
        // --------------------------------------------------------------------
        config.addAllowedMethod("*");

        // --------------------------------------------------------------------
        // "/**" : 모든 URL에 대해 위에서 만든 CORS 정책을 적용한다.
        //
        // 예)
        //   /api/login
        //   /api/users
        //   /admin
        //   /files
        // --------------------------------------------------------------------
        source.registerCorsConfiguration("/**", config);

    /*
    // URL마다 서로 다른 CORS 정책을 적용하는 예시

    // /api/** 는 localhost:5173에서만 접근 허용
    CorsConfiguration apiConfig = new CorsConfiguration();
    apiConfig.addAllowedOriginPattern("http://localhost:5173");

    // /admin/** 는 admin.my.com에서만 접근 허용
    CorsConfiguration adminConfig = new CorsConfiguration();
    adminConfig.addAllowedOriginPattern("https://admin.my.com");

    source.registerCorsConfiguration("/api/**", apiConfig);
    source.registerCorsConfiguration("/admin/**", adminConfig);
    */

        // 설정한 CORS 정책을 적용하는 Filter를 반환한다.
        return new CorsFilter(source);
    }

//Security Filter 자체를 건너뛴다.
//    assets 안의 모든 정적 파일 제외
//    루트 바로 밑의 페이지 제외
//    회원 관련 API 전부 제외
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/assets/**", "/*",
                // Swagger 관련 url은 보안에서 제외
                "/swagger-ui.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs");
    }
}
