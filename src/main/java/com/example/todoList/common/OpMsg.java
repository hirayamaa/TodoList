package com.example.todoList.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpMsg {
    // I:Information, W: Warning, E: Error
    private String msgType;

    private String msgText;
}
