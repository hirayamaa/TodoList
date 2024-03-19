package com.example.todoList.repository;

import com.example.todoList.entity.AttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachedFileRepository extends JpaRepository<AttachedFile, Integer>{
    // todoIdをキーに検索する(idの昇順)
    List<AttachedFile> findByTodoIdOrderById(Integer todoId);
}
