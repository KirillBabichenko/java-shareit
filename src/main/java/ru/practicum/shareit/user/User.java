package ru.practicum.shareit.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class User {
    private Long id;
    private String name;
    @NotBlank
    @Email(message = "Электронная почта не может быть пустой")
    private String email;
}
