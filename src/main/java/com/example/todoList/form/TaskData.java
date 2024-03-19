package com.example.todoList.form;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskData {
    private Integer id;

    @NotBlank
    private String title;

    private String deadline;

    private String done;
}
