package ru.practicum.explore_with_me.interaction_api.model.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    private Set<Long> eventsId;

    private Boolean pinned;

    @NotBlank(message = "Название подборки должно быть указано")
    @Length(min = 1, max = 50, message = "Минимальная длина названия подборки 1 символ, максимальная 50 символов.")
    private String title;
}
