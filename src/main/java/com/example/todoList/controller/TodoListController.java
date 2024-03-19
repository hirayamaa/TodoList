package com.example.todoList.controller;

import com.example.todoList.common.OpMsg;
import com.example.todoList.dao.TodoDaoImpl;
import com.example.todoList.entity.Todo;
import com.example.todoList.form.TodoData;
import com.example.todoList.form.TodoQuery;
import com.example.todoList.repository.TaskRepository;
import com.example.todoList.repository.TodoRepository;
import com.example.todoList.service.TodoService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class TodoListController {
    private final TodoRepository todoRepository;
    private final TaskRepository taskRepository;
    private final TodoService todoService;
    private final HttpSession session;

    @PersistenceContext
    private EntityManager entityManager;
    TodoDaoImpl todoDaoImpl;

    @PostConstruct
    public void init() {
        todoDaoImpl = new TodoDaoImpl(entityManager);
    }

    // Todo一覧表示
    @GetMapping("/todo")
    public ModelAndView showTodoList(ModelAndView mv,
                                     @PageableDefault(size = 5, sort = "id") Pageable pageable) {
        // 一覧を検索して表示する
        mv.setViewName("todoList");
        Page<Todo> todoPage = todoRepository.findAll(pageable);
        mv.addObject("todoList", todoPage.getContent());
        mv.addObject("todoPage", todoPage);
        mv.addObject("todoQuery", new TodoQuery());
        session.setAttribute("todoQuery", new TodoQuery());
        return mv;
    }

    // Todo一覧画面で新規追加リンクがクリックされたとき
    @PostMapping("/todo/create/form")
    public ModelAndView createTodo(ModelAndView mv) {
        mv.setViewName("todoForm");
        mv.addObject("todoData", new TodoData());
        // 画面表示するボタンの切り替えに使用
        session.setAttribute("mode", "create");
        return mv;
    }
    // Todo追加処理
    @PostMapping("/todo/create/do")
    public String createTodo(@ModelAttribute @Validated TodoData todoData,
                             BindingResult result, Model model,
                             RedirectAttributes redirectAttributes) {
        // エラーチェック
        boolean isValid = todoService.isValid(todoData, result);
        if (!result.hasErrors() && isValid) {
            // エラーなし
            var todo = todoData.toEntity();
            todoRepository.saveAndFlush(todo);
            redirectAttributes.addFlashAttribute("msg", new OpMsg("I", "Todoを追加しました"));
            return "redirect:/todo/" + todo.getId();
        } else {
            // エラーあり
            model.addAttribute("msg", new OpMsg("E", "入力に誤りがあります"));
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
        mv.addObject("todoData", new TodoData(todo));
        // 画面表示するボタンの切り替えに使用
        session.setAttribute("mode", "update");
        return mv;
    }

    @PostMapping("/todo/update")
    public String updateTodo(@ModelAttribute @Validated TodoData todoData,
                             BindingResult result, Model model,
                             RedirectAttributes redirectAttributes) {
        // エラーチェック
        boolean isValid = todoService.isValid(todoData, result);
        if (!result.hasErrors() && isValid) {
            // エラーなしの場合
            var todo = todoData.toEntity();
            todoRepository.saveAndFlush(todo);
            // リダイレクト先でメッセージを表示する
            redirectAttributes.addFlashAttribute("msg",
                    new OpMsg("I", "Todoを更新しました"));
            return "redirect:/todo/" + todo.getId();
        } else {
            // エラーあり
            model.addAttribute("msg", new OpMsg("E", "入力に誤りがあります"));
            return "todoForm";
        }
    }

    @PostMapping("/todo/delete")
    public String deleteTodo(@ModelAttribute TodoData todoData,
                             RedirectAttributes redirectAttributes) {
        todoRepository.deleteById(todoData.getId());
        redirectAttributes.addFlashAttribute("msg",
                new OpMsg("I", "Todoを削除しました"));
        return "redirect:/todo";
    }

    @GetMapping("/todo/query")
    public ModelAndView queryTodo(@PageableDefault(size = 5) Pageable pageable, ModelAndView mv) {
        mv.setViewName("todoList");
        // sessionに保存されている条件で検索
        TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
        var todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
        mv.addObject("todoQuery", todoQuery);
        mv.addObject("todoPage", todoPage);
        mv.addObject("todoList", todoPage.getContent());
        return mv;
    }

    @PostMapping("/todo/query")
    public ModelAndView queryTodo(@ModelAttribute TodoQuery todoQuery,
                                  BindingResult result,
                                  @PageableDefault(size = 5) Pageable pageable,
                                  ModelAndView mv) {
        mv.setViewName("todoList");
        Page<Todo> todoPage;
        if (todoService.isValid(todoQuery, result)) {
            // エラーがなければ検索
            todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
            // 入力された条件をsessionに保存
            session.setAttribute("todoQuery", todoQuery);
            mv.addObject("todoPage", todoPage);
            mv.addObject("todoList", todoPage.getContent());
            // 該当するTodoがなければ、メッセージを表示
            if (todoPage.getContent().isEmpty()) {
                mv.addObject("msg",
                        new OpMsg("W", "該当するTodoが見つかりませんでした"));
            }
        } else {
            // 検索条件エラーあり
            mv.addObject("msg", new OpMsg("E", "入力に誤りがあります"));
            mv.addObject("todoPage", null);
            mv.addObject("todoList", null);
        }
        return mv;
    }

    @GetMapping("/task/delete")
    public String deleteTask(@RequestParam(name="task_id") int taskId,
                             @RequestParam(name = "todo_id") int todoId,
                             RedirectAttributes redirectAttributes) {
        taskRepository.deleteById(taskId);
        redirectAttributes.addFlashAttribute("msg",
                new OpMsg("I", "タスクを削除しました"));
        return "redirect:/todo/" + todoId;
    }

    @PostMapping("/task/create")
    public String createTask(@ModelAttribute TodoData todoData, BindingResult result,
                             Model model, RedirectAttributes redirectAttributes) {
        // エラーチェック
        boolean isValid = todoService.isValid(todoData.getNewTask(), result);
        if (isValid) {
            // エラーなし
            var todo = todoData.toEntity();
            var task = todoData.toTaskEntity();
            task.setTodo(todo);
            taskRepository.saveAndFlush(task);
            redirectAttributes.addFlashAttribute("msg",
                    new OpMsg("I", "タスクを登録しました"));
            return "redirect:/todo/" + todo.getId();
        } else {
            // エラーあり
            model.addAttribute("msg", new OpMsg("E", "入力に誤りがあります"));
            return "todoForm";
        }
    }

}
