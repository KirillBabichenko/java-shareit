package ru.practicum.shareit.item;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Getter
public class CommentDto {
    private Long id;
    @NotEmpty
    private String text;
    private String authorName;
    private LocalDateTime created;

}
