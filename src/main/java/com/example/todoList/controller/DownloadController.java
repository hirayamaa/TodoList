package com.example.todoList.controller;

import com.example.todoList.service.DownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DownloadController {
    private final DownloadService downloadService;

    // 添付ファイルのダウンロード処理
    @GetMapping("/todo/af/download/{afId}")
    public void downloadAttachedFile(@PathVariable(name="afId") int afId, HttpServletResponse response) {
        downloadService.downloadAttachedFile(afId, response);
    }
}
