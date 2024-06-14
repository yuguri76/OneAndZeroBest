package com.sparta.oneandzerobest.profile.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequestDto {
    private String name;
    private String introduction;
    private String password;
    private String newPassword;
}
