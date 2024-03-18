package com.example.todoList.dao;

import com.example.todoList.entity.Todo;
import com.example.todoList.form.TodoQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TodoDao {
    List<Todo> findByJPQL(TodoQuery todoQuery);

    Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable);
}
