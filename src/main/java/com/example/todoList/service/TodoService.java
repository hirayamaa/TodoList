package com.example.todoList.service;

import com.example.todoList.form.TodoData;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;

@Service
public class TodoService {
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
            LocalDate deadlineDate = null;
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
}
