package com.example.todoList.form;

import com.example.todoList.entity.Todo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Data
public class TodoData {
    private Integer id;

    @NotBlank(message = "件名を入力してください")
    private String title;

    @NotNull(message = "重要度を選択してください")
    private Integer important;

    @Min(value = 0, message = "緊急度を設定してください")
    private Integer urgency;

    private String deadline;

    private String done;

    /**
     * 入力データからEntityを生成して返す
     * @return TodoEntityを返す
     */
    public Todo toEntity() {
        var todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setImportance(important);
        todo.setUrgency(urgency);
        todo.setDone(done);
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        long ms;
        try {
            ms = sdFormat.parse(deadline).getTime();
            todo.setDeadline(new Date(ms));
        } catch (ParseException e) {
            todo.setDeadline(null);
        }
        return todo;
    }
}
