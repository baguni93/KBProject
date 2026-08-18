package org.scoula.insurancerecommendation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/** 크롤링 시 로컬에 저장한 보험 대표 이미지를 제공한다. */
@Api(tags = "보험 상품 이미지 API")
@RestController
public class InsuranceImageController {

    @ApiOperation("보험 상품 대표 이미지 조회")
    @GetMapping("/api/insurance-products/images/{imageName:.+}")
    public void getInsuranceProductImage(
            @PathVariable("imageName") String imageName,
            HttpServletResponse response
    ) throws IOException {
        String safeName = Path.of(imageName).getFileName().toString();
        if (!safeName.equals(imageName)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        File image = new File(UploadPathName.getInsurancePath(), safeName);
        if (!image.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        UploadFiles.downloadImage(response, image);
    }
}
