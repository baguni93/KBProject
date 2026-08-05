package org.scoula.common.util;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadFiles {

    public static String upload(String baseDir, MultipartFile part) throws IOException {
        // 업로드 디렉토리 생성
        File base = new File(baseDir);

        if (!base.exists()) {
            base.mkdirs();
        }

        // 고유 파일명 생성
        String fileName = UploadFileName.getUniqueName(part.getOriginalFilename());

        // 저장 파일 객체 생성
        File dest = new File(baseDir, fileName);

        // 실제 파일 저장 (Files.copy로 톰캣 상대경로 이슈 방지)
        try (java.io.InputStream is = part.getInputStream()) {
            java.nio.file.Files.copy(is, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // 저장된 파일 경로 반환
        return dest.getPath();
    }

    public static String getFormatSize(Long size) {
        // 파일 크기가 없으면 0 반환
        if (size == null || size <= 0) {
            return "0";
        }

        // Commons IO 사용
        return FileUtils.byteCountToDisplaySize(size);
    }

    public static void download(HttpServletResponse response,
                                File file,
                                String orgName) throws Exception {

        // 다운로드 응답 타입 지정
        response.setContentType("application/download");

        // 파일 크기 설정
        response.setContentLength((int) file.length());

        // 한글 파일명 인코딩
        String filename =
                URLEncoder.encode(orgName, "UTF-8");

        // 다운로드 헤더 설정
        response.setHeader(
                "Content-disposition",
                "attachment;filename=\"" + filename + "\""
        );

        // 파일 데이터를 응답으로 복사
        try (OutputStream os = response.getOutputStream();
             //브라우저로 데이터를 보낼 수 있는 통로를 얻는다. 브라우저에게 직접 데이터 전송
             //기존 출력 통로(os)에 버퍼를 붙여서 더 효율적으로 보내는 출력 통로(bos)를 만든다.
             BufferedOutputStream bos =  new BufferedOutputStream(os))

        {
            //C:/upload/test.jpg 파일의 내용을 읽어서 bos(OutputStream)에 써라.
            Files.copy(Paths.get(file.getPath()), bos);
        }
    }

    public static void downloadImage(HttpServletResponse response, File file) {
        try {
            Path path = Path.of(file.getPath());

            String mimeType = Files.probeContentType(path);

            response.setContentType(mimeType);

            response.setContentLength((int) file.length());
            response.setHeader("Cache-Control", "no-cache");

            try (OutputStream os = response.getOutputStream();
                 BufferedOutputStream bos = new BufferedOutputStream(os)) {
                Files.copy(path, bos);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String uploadAndGetFileName(String baseDir, MultipartFile part) throws IOException {

        File base = new File(baseDir);

        if (!base.exists()) {
            base.mkdirs();
        }

        // 고유 파일명 생성
        String fileName = UploadFileName.getUniqueName(part.getOriginalFilename());

        // 저장 파일 객체 생성
        File dest = new File(baseDir, fileName);

        // 실제 파일 저장
        part.transferTo(dest);

        // 저장된 파일 경로 반환
        return fileName;
    }
}
