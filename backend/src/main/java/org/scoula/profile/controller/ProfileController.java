package org.scoula.profile.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.scoula.profile.dto.ProfileResponseDTO;
import org.scoula.profile.service.ProfileService;
import org.scoula.settlement.dto.SettlementResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

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
