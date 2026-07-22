package org.scoula.controller;

import lombok.extern.log4j.Log4j2;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.security.account.domain.MemberVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequestMapping("/security")
@Log4j2
public class SecurityController {

    @GetMapping("/all")
    public void doAll(){
        log.info("do all can access everybody");
    }

    @GetMapping("/member")
    public void doMember(){
        log.info("logined member");
    }

    @GetMapping("/admin")
    public void doAdmin(){
        log.info("admin only");
    }

    @GetMapping("/login")
    public void login() {
        log.info("login page");
    }

    @GetMapping("/logout")
    public void logout() {
        log.info("logout page");
    }

    @GetMapping("/security/info")
    @ResponseBody
    public String info() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        return auth.getName();
    }

    @GetMapping("/member1")
    public void doMember(Principal principal) {
        log.info("username = " + principal.getName());
    }

    @GetMapping("/member2")
    public void doMember(Authentication authentication) {
        CustomUser customUser = (CustomUser)authentication.getPrincipal();
        log.info("username = " + customUser.getUsername());
    }

    @GetMapping("/admin3")
    public void doAdmin(@AuthenticationPrincipal CustomUser customUser) {
        MemberVO member = customUser.getMember();

        log.info("username = " + member);
    }

}
