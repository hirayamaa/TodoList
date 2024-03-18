package com.example.todoList.service;

import com.example.todoList.common.Utils;
import com.example.todoList.entity.Todo;
import com.example.todoList.form.TodoData;
import com.example.todoList.form.TodoQuery;
import com.example.todoList.repository.TodoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    public boolean isValid(TodoData todoData, BindingResult result) {
        boolean ans = true;
        // 件名が全角スペースだけで構成されていたらエラー
        String title = todoData.getTitle();
        if (title != null && !title.isEmpty()) {
            boolean isAllDoubleSpace = true;
            for (var i = 0; i < title.length(); i++) {
                if (title.charAt(i) != ' ') {
                    isAllDoubleSpace = false;
                    break;
                }
            }
            // エラーメッセージの設定
            if (isAllDoubleSpace) {
                var fieldError = new FieldError(result.getObjectName(),
                        "title", "件名が全角スペースです");
                result.addError(fieldError);
                ans = false;
            }
        }
        //　期限が過去日付ならエラー
        String deadline = todoData.getDeadline();
        if (!deadline.isEmpty()) {
            var today = LocalDate.now();
            LocalDate deadlineDate;
            try {
                deadlineDate = LocalDate.parse(deadline);
                if (deadlineDate.isBefore(today)) {
                    var fieldError = new FieldError(result.getObjectName(),
                            "deadline", "期限を設定するときは今日以降にしてください");
                    result.addError(fieldError);
                    ans = false;
                }
            } catch (DateTimeException e) {
                FieldError fieldError = new FieldError(result.getObjectName(),
                        "deadline", "期限を設定するときはyyyy-mm-dd形式で入力してください");
                result.addError(fieldError);
                ans = false;
            }
        }
        return ans;
    }

    public boolean isValid(TodoQuery todoQuery, BindingResult result) {
        boolean ans = true;
        // 期限：開始のチェック
        String date = todoQuery.getDeadlineFrom();
        if (!date.isEmpty()) {
            try {
                LocalDate.parse(date);
            } catch (DateTimeException e) {
                var fieldError = new FieldError(result.getObjectName(),
                        "deadlineFrom",
                        "期限：開始を入力するときはyyyy-mm-dd形式で入力してください");
                result.addError(fieldError);
                ans = false;
            }
        }
        // 期限：終了のチェック
        date = todoQuery.getDeadlineTo();
        if (!date.isEmpty()) {
            try {
                LocalDate.parse(date);
            } catch (DateTimeException e) {
                var fieldError = new FieldError(result.getObjectName(),
                        "deadlineTo",
                        "期限：終了を入力するときはyyyy-mm-dd形式で入力してください");
                result.addError(fieldError);
                ans = false;
            }
        }
        return ans;
    }

    public List<Todo> doQuery(TodoQuery todoQuery) {
        List<Todo> todoList = null;
        if (!todoQuery.getTitle().isEmpty()) {
            // タイトルで検索
            todoList = todoRepository.findByTitleLike("%" + todoQuery.getTitle() + "%");
        } else if (todoQuery.getImportance() != null && todoQuery.getImportance() != -1) {
            // 重要度で検索
            todoList = todoRepository.findByImportance(todoQuery.getImportance());
        } else if (todoQuery.getUrgency() != null && todoQuery.getUrgency() != -1) {
            // 緊急度で検索
            todoList = todoRepository.findByUrgency(todoQuery.getUrgency());
        } else if (!todoQuery.getDeadlineFrom().isEmpty() && todoQuery.getDeadlineTo().isEmpty()) {
            // 期限：開始
            todoList = todoRepository.
                    findByDeadlineGreaterThanEqualOrderByDeadlineAsc(
                            Utils.str2date(todoQuery.getDeadlineFrom()));
        } else if (todoQuery.getDeadlineFrom().isEmpty() && !todoQuery.getDeadlineTo().isEmpty()) {
            // 期限：終了
            todoList = todoRepository.
                    findByDeadlineLessThanEqualOrderByDeadlineAsc(
                            Utils.str2date(todoQuery.getDeadlineTo()));
        } else if (!todoQuery.getDeadlineFrom().isEmpty() && !todoQuery.getDeadlineTo().isEmpty()) {
            // 期限：終了
            todoList = todoRepository.
                    findByDeadlineBetweenOrderByDeadlineAsc(
                            Utils.str2date(todoQuery.getDeadlineFrom()),
                            Utils.str2date(todoQuery.getDeadlineTo()));
        } else if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
            // 完了で検索
            todoList = todoRepository.findByDone("Y");
        } else {
            // 検索条件がなければ全件検索
            todoList = todoRepository.findAll();
        }
        return todoList;
    }
}
