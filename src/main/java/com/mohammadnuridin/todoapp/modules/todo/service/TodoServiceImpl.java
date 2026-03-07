package com.mohammadnuridin.todoapp.modules.todo.service;

import com.mohammadnuridin.todoapp.core.exception.AppException;
import com.mohammadnuridin.todoapp.core.exception.ErrorCode;
import com.mohammadnuridin.todoapp.core.response.PageResponse;
import com.mohammadnuridin.todoapp.core.util.SecurityUtil;
import com.mohammadnuridin.todoapp.modules.category.domain.Category;
import com.mohammadnuridin.todoapp.modules.category.repository.CategoryRepository;
import com.mohammadnuridin.todoapp.modules.todo.domain.Todo;
import com.mohammadnuridin.todoapp.modules.todo.dto.CreateTodoRequest;
import com.mohammadnuridin.todoapp.modules.todo.dto.TodoFilterRequest;
import com.mohammadnuridin.todoapp.modules.todo.dto.TodoResponse;
import com.mohammadnuridin.todoapp.modules.todo.dto.UpdateTodoRequest;
import com.mohammadnuridin.todoapp.modules.todo.repository.TodoRepository;
import com.mohammadnuridin.todoapp.modules.todo.repository.TodoSpecification;
import com.mohammadnuridin.todoapp.modules.user.domain.User;
import com.mohammadnuridin.todoapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // ── GET ALL (paginated + filtered) ───────────────────────
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TodoResponse> getAll(TodoFilterRequest filter) {
        String userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = buildPageable(filter);
        Specification<Todo> spec = TodoSpecification.filter(userId, filter);

        Page<TodoResponse> page = todoRepository.findAll(spec, pageable)
                .map(TodoResponse::from);

        return PageResponse.of(page);
    }

    // ── GET BY ID ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public TodoResponse getById(String id) {
        Todo todo = getOwnedTodo(id);
        return TodoResponse.from(todo);
    }

    // ── CREATE ────────────────────────────────────────────────
    @Override
    @Transactional
    public TodoResponse create(CreateTodoRequest request) {
        String userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Todo todo = Todo.builder()
                .user(user)
                .title(request.title().trim())
                .description(request.description())
                .completed(false)
                .dueDate(request.dueDate())
                .priority(request.priority())
                .categories(resolveCategories(request.categoryIds(), userId))
                .build();

        Todo saved = todoRepository.save(todo);
        log.info("Todo created: {} for user: {}", saved.getId(), userId);
        return TodoResponse.from(saved);
    }

    // ── UPDATE ────────────────────────────────────────────────
    @Override
    @Transactional
    public TodoResponse update(String id, UpdateTodoRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        Todo todo = getOwnedTodo(id);

        if (request.title() != null && !request.title().isBlank()) {
            todo.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            todo.setDescription(request.description());
        }
        if (request.dueDate() != null) {
            todo.setDueDate(request.dueDate());
        }
        if (request.priority() != null) {
            todo.setPriority(request.priority());
        }
        if (request.completed() != null) {
            todo.setCompleted(request.completed());
        }
        if (request.categoryIds() != null) {
            todo.setCategories(resolveCategories(request.categoryIds(), userId));
        }

        Todo saved = todoRepository.save(todo);
        log.info("Todo updated: {} for user: {}", id, userId);
        return TodoResponse.from(saved);
    }

    // ── DELETE ────────────────────────────────────────────────
    @Override
    @Transactional
    public void delete(String id) {
        Todo todo = getOwnedTodo(id);
        todoRepository.delete(todo);
        log.info("Todo deleted: {} for user: {}", id, SecurityUtil.getCurrentUserId());
    }

    // ── TOGGLE COMPLETE ───────────────────────────────────────
    @Override
    @Transactional
    public TodoResponse toggleComplete(String id) {
        Todo todo = getOwnedTodo(id);
        todo.setCompleted(!todo.getCompleted());
        Todo saved = todoRepository.save(todo);
        log.info("Todo {} toggled to completed={}", id, saved.getCompleted());
        return TodoResponse.from(saved);
    }

    // ── Helper: ownership check ───────────────────────────────
    private Todo getOwnedTodo(String id) {
        String userId = SecurityUtil.getCurrentUserId();
        return todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND));
    }

    // ── Helper: resolve category ids → Category entities ─────
    private Set<Category> resolveCategories(Set<String> categoryIds, String userId) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Category> categories = new HashSet<>();
        for (String catId : categoryIds) {
            Category category = categoryRepository.findByIdAndUserId(catId, userId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            categories.add(category);
        }
        return categories;
    }

    // ── Helper: build Pageable dari filter ────────────────────
    private Pageable buildPageable(TodoFilterRequest filter) {
        String sortParam = filter.getSort(); // format: "field:direction"
        String[] parts = sortParam.split(":");

        String field = parts.length > 0 ? parts[0] : "createdAt";
        String direction = parts.length > 1 ? parts[1] : "desc";

        // Whitelist field yang boleh di-sort — cegah injection
        field = switch (field) {
            case "title", "dueDate", "priority", "completed", "updatedAt" -> field;
            default -> "createdAt";
        };

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();

        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
}