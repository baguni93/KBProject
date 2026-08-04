package org.scoula.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNameChangeDTO {

    private String phoneNumber;
    private String newUserName;
}