package com.example.todoList.form;

import com.example.todoList.common.Utils;
import com.example.todoList.entity.Task;
import com.example.todoList.entity.Todo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Data
public class TodoData {
    private Integer id;

    @NotBlank(message = "件名を入力してください")
    private String title;

    @NotNull(message = "重要度を選択してください")
    private Integer importance;

    @Min(value = 0, message = "緊急度を設定してください")
    private Integer urgency;

    private String deadline;

    private String done;

    @Valid
    private List<TaskData> taskList;

    /**
     * 入力データからEntityを生成して返す
     * @return TodoEntityを返す
     */
    public Todo toEntity() {
        var todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setImportance(importance);
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

        Date date;
        Task task;
        if (taskList != null) {
            for (var taskData : taskList) {
                date = Utils.str2dateOrNull(taskData.getDeadline());
                task = new Task(taskData.getId(), null, taskData.getTitle(), date, taskData.getDone());
                todo.addTask(task);
            }
        }
        return todo;
    }
}
