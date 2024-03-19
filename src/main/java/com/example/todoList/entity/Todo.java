package com.example.todoList.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "todo")
@Data
@ToString(exclude = "taskList")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "importance")
    private Integer importance;

    @Column(name = "urgency")
    private Integer urgency;

    @Column(name = "deadline")
    private Date deadline;

    @Column(name = "done")
    private String done;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL)
    private List<Task> taskList = new ArrayList<>();

    public void addTask(Task task) {
        task.setTodo(this);
        taskList.add(task);
    }
}
