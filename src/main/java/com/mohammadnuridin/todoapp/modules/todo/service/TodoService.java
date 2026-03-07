package com.mohammadnuridin.todoapp.modules.todo.service;

import com.mohammadnuridin.todoapp.core.response.PageResponse;
import com.mohammadnuridin.todoapp.modules.todo.dto.CreateTodoRequest;
import com.mohammadnuridin.todoapp.modules.todo.dto.TodoFilterRequest;
import com.mohammadnuridin.todoapp.modules.todo.dto.TodoResponse;
import com.mohammadnuridin.todoapp.modules.todo.dto.UpdateTodoRequest;

public interface TodoService {

    PageResponse<TodoResponse> getAll(TodoFilterRequest filter);

    TodoResponse getById(String id);

    TodoResponse create(CreateTodoRequest request);

    TodoResponse update(String id, UpdateTodoRequest request);

    void delete(String id);

    TodoResponse toggleComplete(String id);
}