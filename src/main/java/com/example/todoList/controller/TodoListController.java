package com.example.todoList.controller;

import com.example.todoList.entity.Todo;
import com.example.todoList.form.TodoData;
import com.example.todoList.form.TodoQuery;
import com.example.todoList.repository.TodoRepository;
import com.example.todoList.service.TodoService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@AllArgsConstructor
public class TodoListController {
    private final TodoRepository todoRepository;
    private final TodoService todoService;
    private final HttpSession session;

    // Todo一覧表示
    @GetMapping("/todo")
    public ModelAndView showTodoList(ModelAndView mv) {
        // 一覧を検索して表示する
        mv.setViewName("todoList");
        List<Todo> todoList = todoRepository.findAll();
        mv.addObject("todoList", todoList);
        mv.addObject("todoQuery", new TodoQuery());
        return mv;
    }

    // Todo一覧画面で新規追加リンクがクリックされたとき
    @GetMapping("/todo/create")
    public ModelAndView createTodo(ModelAndView mv) {
        mv.setViewName("todoForm");
        mv.addObject("todoData", new TodoData());
        // 画面表示するボタンの切り替えに使用
        session.setAttribute("mode", "create");
        return mv;
    }
    // Todo追加処理
    @PostMapping("/todo/create")
    public String createTodo(@ModelAttribute @Validated TodoData todoData,
                             BindingResult result, Model model) {
        // エラーチェック
        boolean isValid = todoService.isValid(todoData, result);
        if (!result.hasErrors() && isValid) {
            // エラーなし
            var todo = todoData.toEntity();
            todoRepository.saveAndFlush(todo);
            return "redirect:/todo";
        } else {
            // エラーあり
            return "todoForm";
        }
    }

    // Todo一覧へ戻る
    @PostMapping("/todo/cancel")
    public String cancel() {
        return "redirect:/todo";
    }

    @GetMapping("/todo/{id}")
    public ModelAndView todoById(@PathVariable(name="id") int id, ModelAndView mv) {
        mv.setViewName("todoForm");
        var todo = todoRepository.findById(id).get();
        mv.addObject("todoData", todo);
        // 画面表示するボタンの切り替えに使用
        session.setAttribute("mode", "update");
        return mv;
    }

    @PostMapping("/todo/update")
    public String updateTodo(@ModelAttribute @Validated TodoData todoData,
                             BindingResult result, Model model) {
        // エラーチェック
        boolean isValid = todoService.isValid(todoData, result);
        if (!result.hasErrors() && isValid) {
            // エラーなしの場合
            var todo = todoData.toEntity();
            todoRepository.saveAndFlush(todo);
            return "redirect:/todo";
        } else {
            // エラーあり
            return "todoForm";
        }
    }

    @PostMapping("/todo/delete")
    public String deleteTodo(@ModelAttribute TodoData todoData) {
        todoRepository.deleteById(todoData.getId());
        return "redirect:/todo";
    }

    @PostMapping("/todo/query")
    public ModelAndView queryTodo(@ModelAttribute TodoQuery todoQuery,
                                  BindingResult result, ModelAndView mv) {
        mv.setViewName("todoList");
        List<Todo> todoList = null;
        if (todoService.isValid(todoQuery, result)) {
            // エラーがなければ検索
            todoList = todoService.doQuery(todoQuery);
            mv.addObject("todoList", todoList);
        }
        return mv;
    }
}
