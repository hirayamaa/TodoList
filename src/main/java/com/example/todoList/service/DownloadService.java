package com.example.todoList.service;

import com.example.todoList.common.Utils;
import com.example.todoList.repository.AttachedFileRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;

@Service
@RequiredArgsConstructor
public class DownloadService {
    private final AttachedFileRepository attachedFileRepository;

    @Value("${attached.file.path}")
    private String ATTACHED_FILE_PATH;

    public void downloadAttachedFile(int afId, HttpServletResponse response) {
        // 添付ファイルの情報を取得する
        var af = attachedFileRepository.findById(afId).get();
        // 拡張子からContent-Typeを求める
        var fileName = af.getFileName();
        var fext = fileName.substring(fileName.lastIndexOf(".") + 1);
        var contentType = Utils.ext2contentType(fext);
        // ダウンロードするファイル
        var downloadFilePath = Utils.makeAttachedFilePath(ATTACHED_FILE_PATH, af);
        var downloadFile = new File(downloadFilePath);
        // ダウンロードファイル送信
        BufferedInputStream bis;
        OutputStream out;
        try {
            if (contentType.isEmpty()) {
                // バイナリで送信
                response.setContentType("application/force-download");
                // ローカル保存
                response.setHeader("Content-Disposition", "attachment; filename=\""
                        + URLEncoder.encode(af.getFileName(), "UTF-8") + "\"");
            } else {
                // 拡張子に対応するContent-Type
                response.setContentType(contentType);
                // 別タブ表示
                response.setHeader("Content-Disposition", "inline");
            }
            // ファイルサイズ
            response.setContentLengthLong(downloadFile.length());
            // ファイルの内容をブラウザへ出力
            bis = new BufferedInputStream(new FileInputStream(downloadFilePath));
            out = response.getOutputStream();
            byte[] buff = new byte[8 * 1024];
            int nRead = 0;
            while((nRead = bis.read(buff)) != -1) {
                out.write(buff, 0, nRead);
            }
            out.flush();
            // ファイルクローズ
            bis.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
