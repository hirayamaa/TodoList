package com.example.todoList.controller;

import com.example.todoList.entity.Todo;
import com.example.todoList.form.TodoData;
import com.example.todoList.repository.TodoRepository;
import com.example.todoList.service.TodoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@AllArgsConstructor
public class TodoListController {
    private final TodoRepository todoRepository;
    private final TodoService todoService;

    // Todo一覧表示
    @GetMapping("/todo")
    public ModelAndView showTodoList(ModelAndView mv) {
        // 一覧を検索して表示する
        mv.setViewName("todoList");
        List<Todo> todoList = todoRepository.findAll();
        mv.addObject("todoList", todoList);
        return mv;
    }

    // Todo一覧画面で新規追加リンクがクリックされたとき
    @GetMapping("/todo/create")
    public ModelAndView createTodo(ModelAndView mv) {
        mv.setViewName("todoForm");
        mv.addObject("todoData", new TodoData());
        return mv;
    }
    // Todo追加処理
    @PostMapping("/todo/create")
    public ModelAndView createTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result,
                                   ModelAndView mv) {
        // エラーチェック
        boolean isValid = todoService.isValid(todoData, result);
        if (!result.hasErrors() && isValid) {
            // エラーなし
            var todo = todoData.toEntity();
            todoRepository.saveAndFlush(todo);
            return showTodoList(mv);
        } else {
            // エラーあり
            mv.setViewName("todoForm");
            return mv;
        }
    }

    // Todo一覧へ戻る
    @PostMapping("/todo/cancel")
    public String cancel() {
        return "redirect:/todo";
    }
}
