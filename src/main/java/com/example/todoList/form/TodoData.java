package com.example.todoList.form;

import com.example.todoList.common.Utils;
import com.example.todoList.entity.AttachedFile;
import com.example.todoList.entity.Task;
import com.example.todoList.entity.Todo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
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

    private TaskData newTask;

    private List<AttachedFileData> attachedFileList;

    public TodoData(Todo todo, List<AttachedFile> attachedFiles) {
        this.id = todo.getId();
        this.title = todo.getTitle();
        this.importance = todo.getImportance();
        this.urgency = todo.getUrgency();
        this.deadline = Utils.date2str(todo.getDeadline());
        this.done = todo.getDone();
        // 登録済Task
        this.taskList = new ArrayList<>();
        String dt;
        for (var task : todo.getTaskList()) {
            dt = Utils.date2str(task.getDeadline());
            this.taskList.add(new TaskData(task.getId(), task.getTitle(), dt, task.getDone()));
        }
        // 新規追加用Task
        newTask = new TaskData();
        // 添付ファイル
        attachedFileList = new ArrayList<>();
        String fileName;
        String fext;
        String contentType;
        boolean isOpenNewWindow;
        for (var af : attachedFiles) {
            // ファイル名
            fileName = af.getFileName();
            // 拡張子
            fext = fileName.substring(fileName.lastIndexOf(".") + 1);
            // Content-Type
            contentType = Utils.ext2contentType(fext);
            // 別Windowで表示するか
            isOpenNewWindow = !contentType.isEmpty();
            attachedFileList.add(new AttachedFileData(af.getId(), fileName, af.getNote(), isOpenNewWindow));
        }
    }

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

    public Task toTaskEntity() {
        var task = new Task();
        task.setId(newTask.getId());
        task.setTitle(newTask.getTitle());
        task.setDeadline(Utils.str2date(newTask.getDeadline()));
        task.setDone(newTask.getDone());
        return task;
    }
}
