package com.sparta.oneandzerobest.newsfeed.dto;

import com.sparta.oneandzerobest.newsfeed.entity.Newsfeed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsfeedRequestDto {
    @NotBlank(message = "내용이 비어있습니다.")
    private String content;
}
