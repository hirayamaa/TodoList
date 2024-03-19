package com.example.todoList.service;

import com.example.todoList.common.Utils;
import com.example.todoList.entity.AttachedFile;
import com.example.todoList.entity.Todo;
import com.example.todoList.form.TaskData;
import com.example.todoList.form.TodoData;
import com.example.todoList.form.TodoQuery;
import com.example.todoList.repository.AttachedFileRepository;
import com.example.todoList.repository.TodoRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final AttachedFileRepository attachedFileRepository;

    @Value("${attached.file.path}")
    private String ATTACHED_FILE_PATH;

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
        // Taskのチェック
        List<TaskData> taskList = todoData.getTaskList();
        if (taskList != null) {
            for (var n = 0; n < taskList.size(); n++) {
                var taskData = taskList.get(n);
                // 全角スペースだけで構成されていたらエラー
                if (Utils.isBlank(taskData.getTitle())) {
                    if (Utils.isAllDoubleSpace(taskData.getTitle())) {
                        var fieldError = new FieldError(result.getObjectName(),
                                "taskList[" + n + "].title",
                                "件名が全角スペースです");
                        result.addError(fieldError);
                        ans = false;
                    }
                }
                // 期限の形式チェック
                String taskDeadline = taskData.getDeadline();
                if (!taskDeadline.isEmpty() && !Utils.isValidDateFormat(taskDeadline)) {
                    var fieldError = new FieldError(result.getObjectName(),
                            "taskList[" + n + "].deadline",
                            "期限を入力するときはyyyy-mm-dd形式で入力してください");
                    result.addError(fieldError);
                    ans = false;
                }
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
        List<Todo> todoList;
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

    public boolean isValid(TaskData taskData, BindingResult result) {
        boolean ans = true;
        // タスクの件名が半角スペースだけまたは、""ならエラー
        if (Utils.isBlank(taskData.getTitle())) {
            var fieldError = new FieldError(
                    result.getObjectName(),
                    "newTask.title",
                    "件名を入力してください");
            result.addError(fieldError);
            ans = false;
        } else {
            // タスクの件名が全角スペースだけで構成されていたらエラー
            if (Utils.isAllDoubleSpace(taskData.getTitle())) {
                var fieldError = new FieldError(
                        result.getObjectName(),
                        "newTask.title",
                        "件名が全角スペースです"
                );
                result.addError(fieldError);
                ans = false;
            }
        }
        // 期限が未入力ならチェックしない
        String deadline = taskData.getDeadline();
        if (deadline.isEmpty()) {
            return ans;
        }
        // 期限の形式チェック
        if (!Utils.isValidDateFormat(deadline)) {
            var fieldError = new FieldError(
                    result.getObjectName(),
                    "newTask.deadline",
                    "期限を入力するときはyyyy-mm-dd形式で入力してください");
            result.addError(fieldError);
            ans = false;
        } else {
            // 過去日付ならエラー
            if (!Utils.isTodayOrFurtureDate(deadline)) {
                var fieldError = new FieldError(
                        result.getObjectName(),
                        "newTask.deadline",
                        "過去の日付は入力できません");
                result.addError(fieldError);
                ans = false;
            }
        }
        return ans;
    }

    public void saveAttachedFile(int todoId, String note, MultipartFile fileContents) {
        // アップロード元ファイル名
        var fileName = fileContents.getOriginalFilename();
        // 格納フォルダの存在チェック
        var uploadDir = new File(ATTACHED_FILE_PATH);
        if (!uploadDir.exists()) {
            // フォルダが存在しない場合、作成する
            uploadDir.mkdirs();
        }
        // 添付ファイルの格納時刻を取得
        var sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        var createTime = sdf.format(new Date());
        // テーブルに格納するインスタンスを作成
        var af = new AttachedFile();
        af.setTodoId(todoId);
        af.setFileName(fileName);
        af.setCreateTime(createTime);
        af.setNote(note);

        // アップロードファイルの内容を取得
        byte[] contents;
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(Utils.makeAttachedFilePath(ATTACHED_FILE_PATH, af)))) {
            contents = fileContents.getBytes();
            bos.write(contents);
            // テーブルに登録
            attachedFileRepository.saveAndFlush(af);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 添付ファイル削除処理
    public void deleteAttachedFile(int afId) {
        var af = attachedFileRepository.findById(afId).get();
        var file = new File(Utils.makeAttachedFilePath(ATTACHED_FILE_PATH, af));
        file.delete();
    }
}
