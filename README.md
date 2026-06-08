# Task Management System REST API

Полноценная реализация REST API для системы управления задачами согласно обновлённому заданию.

## Требования (выполнено)

- ID задач и пользователей — **UUID**
- Пользователь имеет `roles` (ROLE_USER / ROLE_ADMIN)
- Статусы задач: **NEW, IN_PROGRESS, DONE**
- Эндпоинты точно по спецификации:
  - `POST /auth/register`
  - `POST /auth/login`
  - `GET /users/me`
  - `GET /tasks` (фильтрация по status/priority + пагинация)
  - `GET /tasks/{id}`
  - `POST /tasks`
  - `PUT /tasks/{id}`
  - `DELETE /tasks/{id}`

## Стек

- Java 17 + Spring Boot 3.3
- Spring Security + JWT (jjwt 0.12)
- Spring Data JPA + Hibernate
- H2 (по умолчанию) / PostgreSQL
- MapStruct + Lombok
- JUnit + Mockito

## Запуск

```bash
mvn spring-boot:run
```

- Swagger: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

**PostgreSQL:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Примеры запросов

### Регистрация
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ivan","email":"ivan@example.com","password":"password123"}'
```

### Логин
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ivan","password":"password123"}'
```

Скопируйте `token` из ответа.

### Получить свои задачи (с фильтром)
```bash
curl -X GET "http://localhost:8080/tasks?status=NEW&priority=HIGH" \
  -H "Authorization: Bearer <TOKEN>"
```

### Создать задачу
```bash
curl -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Сделать задание","description":"Spring Boot + UUID","priority":"HIGH"}'
```

## Тесты

```bash
mvn test
```

## Структура проекта

```
src/main/java/com/example/taskmanagement
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   └── TaskController.java
├── entity/
│   ├── User.java
│   ├── Task.java
│   ├── TaskStatus.java
│   └── TaskPriority.java
├── repository/
├── security/
├── service/
│   ├── UserService.java
│   └── TaskService.java
├── mapper/
├── dto/
└── exception/
```

## GitHub

Репозиторий создан автоматически.