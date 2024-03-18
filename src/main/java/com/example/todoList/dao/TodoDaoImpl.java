package com.example.todoList.dao;

import com.example.todoList.common.Utils;
import com.example.todoList.entity.Todo;
import com.example.todoList.form.TodoQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {
    private final EntityManager entityManager;

    @Override
    public List<Todo> findByJPQL(TodoQuery todoQuery) {
        var sb = new StringBuilder("SELECT t FROM Todo t WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        int pos = 0;
        // 実行するJPQLの組み立て
        // 件名
        if (!todoQuery.getTitle().isEmpty()) {
            sb.append(" AND t.title LIKE ?" + (++pos));
            params.add("%" + todoQuery.getTitle() + "%");
        }
        // 重要度
        if (todoQuery.getImportance() != -1) {
            sb.append(" AND t.importance = ?" + (++pos));
            params.add(todoQuery.getImportance());
        }
        // 緊急度
        if (todoQuery.getUrgency() != -1) {
            sb.append(" AND t.urgency = ?" + (++pos));
            params.add(todoQuery.getUrgency());
        }
        // 期限：開始
        if (!todoQuery.getDeadlineFrom().isEmpty()) {
            sb.append(" and t.deadline >= ?" + (++pos));
            params.add(Utils.str2date(todoQuery.getDeadlineFrom()));
        }
        // 期限：終了
        if (!todoQuery.getDeadlineTo().isEmpty()) {
            sb.append(" and t.deadline <= ?" + (++pos));
            params.add(Utils.str2date(todoQuery.getDeadlineTo()));
        }
        // 完了
        if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
            sb.append(" AND t.done = ?" + (++pos));
            params.add((todoQuery.getDone()));
        }
        sb.append(" ORDER BY id");
        var query = entityManager.createQuery(sb.toString());
        for (var i = 0; i < params.size(); i++) {
            query = query.setParameter(i + 1, params.get(i));
        }

        @SuppressWarnings("unchecked")
        List<Todo> list = query.getResultList();
        return list;
    }

    @Override
    public Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable) {
        var builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Todo> query = builder.createQuery(Todo.class);
        Root<Todo> root = query.from(Todo.class);
        List<Predicate> predicates = new ArrayList<>();
        // 件名
        String title = "";
        if (!todoQuery.getTitle().isEmpty()) {
            title = "%" + todoQuery.getTitle() + "%";
        } else {
            title = "%";
        }
        predicates.add(builder.like(root.get("title"), title));
        // 重要度
        if (todoQuery.getImportance() != -1) {
            predicates.add(builder.and(builder.equal(
                    root.get("importance"), todoQuery.getImportance())));
        }
        // 緊急度
        if (todoQuery.getUrgency() != -1) {
            predicates.add(builder.and(builder.equal(
                    root.get("urgency"), todoQuery.getUrgency())));
        }
        // 期限：開始
        if (!todoQuery.getDeadlineFrom().isEmpty()) {
            predicates.add(builder.and(builder.greaterThanOrEqualTo(
                    root.get("deadline"), Utils.str2date(todoQuery.getDeadlineFrom()))));
        }
        // 期限：終了
        if (!todoQuery.getDeadlineTo().isEmpty()) {
            predicates.add(builder.and(builder.lessThanOrEqualTo(
                    root.get("deadline"), Utils.str2date(todoQuery.getDeadlineTo()))));
        }
        // 完了
        if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
            predicates.add(builder.and(builder.equal(
                    root.get("done"), todoQuery.getDone())));
        }
        // SELECT作成
        Predicate[] predArray = new Predicate[predicates.size()];
        predicates.toArray(predArray);
        query = query.select(root).where(predArray).orderBy(builder.asc(root.get("id")));
        // クエリ生成
        TypedQuery<Todo> typedQuery = entityManager.createQuery(query);
        // 該当レコード件数取得
        int totalRows = typedQuery.getResultList().size();
        // 先頭レコードの位置指定
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        // 1ページあたりの件数
        typedQuery.setMaxResults(pageable.getPageSize());
        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }
}
