package org.scoula.profileTest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.scoula.profileTest.dto.ProfileResponseDTO;
import org.scoula.profileTest.service.ProfileTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
@Log4j2
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileTestController {

    private final ProfileTestService profileService;

    @GetMapping("{userId}")
    public ResponseEntity<ProfileResponseDTO> get(@PathVariable int userId){

        return ResponseEntity.ok(profileService.get(userId));
    }

    @GetMapping("/image/{imageName}")
    public void viewProfileImage(@PathVariable String imageName, HttpServletResponse response) {
        File file = new File(UploadPathName.getProfilePath()+imageName);
        UploadFiles.downloadImage(response, file);
    }
}
