package org.scoula.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.UploadFiles;
import org.scoula.member.dto.ChangePasswordDTO;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.member.dto.MemberUpdateDTO;
import org.scoula.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/checkusername/{username}")
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username){
        return ResponseEntity.ok(memberService.checkDuplicate(username));
    }

    @PostMapping("")
    public ResponseEntity<MemberDTO> create(MemberJoinDTO memberJoinDTO){
        return  ResponseEntity.ok(memberService.join(memberJoinDTO));
    }

    @PutMapping("/{username}")
    public ResponseEntity<MemberDTO> update(MemberUpdateDTO memberUpdateDTO){
        return  ResponseEntity.ok(memberService.update(memberUpdateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id){
        return  ResponseEntity.ok(memberService.delete(id));
    }

    @GetMapping("/{username}/avatar")
    public void getAvatar(@PathVariable String username, HttpServletResponse response) {
        String avatarPath = "c:/upload/" + username + ".png";
        log.info(avatarPath);
        File file = new File(avatarPath);
        if(!file.exists()) { // 아바타 등록이 없는 경우, 디폴트 아바타 이미지 사용
            file = new File("C:/upload/unknown.png");
        }
        UploadFiles.downloadImage(response, file);
    }

    @PutMapping("/{username}/changepassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        memberService.changePassword(changePasswordDTO);
        return ResponseEntity.ok().build();
    }

}
