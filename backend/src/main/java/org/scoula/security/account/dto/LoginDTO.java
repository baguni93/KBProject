package org.scoula.security.account.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;

import javax.servlet.http.HttpServletRequest;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Log4j2
public class LoginDTO {

    private String username;
    private String password;

    public static LoginDTO of(HttpServletRequest request){

        ObjectMapper om = new ObjectMapper();

        try{
            log.info(request.getInputStream());
            return om.readValue(request.getInputStream(), LoginDTO.class);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new BadCredentialsException("username 또는 password가 없습니다.");
        }

    }

}
