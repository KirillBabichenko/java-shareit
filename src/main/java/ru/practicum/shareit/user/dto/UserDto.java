package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
public class UserDto {
    private Long id;
    private String name;
    @NotBlank
    @Email(message = "Электронная почта не может быть пустой")
    private String email;
}

