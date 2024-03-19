package com.example.todoList.form;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttachedFileData {
    private Integer id;

    private String fileName;

    private String note;

    private boolean openInNewTab;
}
