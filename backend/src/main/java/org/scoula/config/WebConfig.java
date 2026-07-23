package org.scoula.config;

import org.scoula.common.util.UploadFileName;
import org.scoula.common.util.UploadFiles;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import java.nio.file.Paths;


public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    final long MAX_FILE_SIZE = 1024 * 1024 * 10L;
    final long MAX_REQUEST_SIZE = 1024 * 1024 * 20L;
    final int FILE_SIZE_THRESHOLD = 1024 * 1024 * 5;

    //AbstractAnnotationConfigDispatcherServletInitializer가 제공하는 확장용 메서드(훅 메서드)
    //DispatcherServlet 록 시 추가 설정 적용
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {

//        404 발생 시
//        기본 처리하지 말고
//        예외(NoHandlerFoundException)처리
        registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");

        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                "",// 업로드 처리 디렉토리 경로
                MAX_FILE_SIZE,// 업로드 가능한 파일 하나의 최대 크기
                MAX_REQUEST_SIZE, // 업로드 가능한 전체 최대 크기(여러 파일 업로드 하는 경우)
                FILE_SIZE_THRESHOLD // 메모리 파일의 최대 크기(이보다 작으면 실제 메모리에서만 작업)
        );
        registration.setMultipartConfig(multipartConfig);
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] {"/"};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]
                {RootConfig.class, SecurityConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]
                {ServletConfig.class, SwaggerConfig.class };
    }


}
