package com.example.todoList.controller;

import com.example.todoList.entity.Todo;
import com.example.todoList.repository.TodoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@AllArgsConstructor
public class TodoListController {
    private final TodoRepository todoRepository;

    @GetMapping("/todo")
    public ModelAndView showTodoList(ModelAndView mv) {
        // 一覧を検索して表示する
        mv.setViewName("todoList");
        List<Todo> todoList = todoRepository.findAll();
        mv.addObject("todoList", todoList);
        return mv;
    }
}
