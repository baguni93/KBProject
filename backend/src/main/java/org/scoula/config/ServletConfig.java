package org.scoula.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;


// mvc 컨테이너(mvc Context)의 설정 클래스
//HandlerMapping
//HandlerAdapter
//ViewResolver
//MessageConverter

@Log4j2
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
        "org.scoula.controller",
        "org.scoula.exception",
        "org.scoula.member.controller",
        "org.scoula.feed.controller",
        "org.scoula.settlement.controller",
        "org.scoula.notification.controller",
        "org.scoula.friend.controller",
        "org.scoula.comment.controller",
        "org.scoula.like.controller",
})
public class ServletConfig implements WebMvcConfigurer {


    /*d
    프론트파일(css, js, img)의 위치를 지정해주는 함수
    /resources/img/a.png라고 요청이 들어오면 /resources/밑에서 찾겠다라는 설정
    ex) <img src="/resources/img/a.png">
    DispatcherServlet이 직접 처리하지 않고 정적 파일로 응답하도록 설정
    */

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/resources/**") // url이 /resources/로 시작하는 모든 경로
                .addResourceLocations("/resources/"); // webapp/resources/경로로 매핑

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("/resources/assets/");

    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        log.info("===== addViewControllers =====");

        registry.addViewController("/")
                .setViewName("forward:/resources/index.html");
    }

    //Servlet 3.0 파일 업로드 사용시 - MultipartResolver 빈 등록
    //Spring MVC 컨테이너(ServletConfig)가 관리하는 MultipartResolver
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver
                = new StandardServletMultipartResolver();
        return resolver;
    }

    @Bean
    public RestTemplate restTemplate() {

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();

        return new RestTemplate(factory);
    }
}
