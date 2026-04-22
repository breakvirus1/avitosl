# AvitoSL - Многосервисная платформа объявлений

Многосервисное приложение для платформы объявлений с микроархитектурой, авторизацией через Keycloak и API Gateway.

## 🏗️ Архитектура

Проект состоит из следующих микросервисов:

| Сервис | Порт | Описание |
|--------|------|----------|
| **API Gateway** | 8081 | Единая точка входа, маршрутизация, аутентификация |
| **User Service** | 8081 | Управление пользователями (через gateway) |
| **Category Service** | 8082 | Категории и подкатегории |
| **Post Service** | 8083 | Посты, комментарии, фотографии |
| **Chat Service** | 8084 | Система сообщений и чатов |
| **Purchase Service** | 8085 | Покупки и кошелёк |
| **Eureka Server** | 8761 | Сервис-реестр (Service Discovery) |
| **Frontend** | 5173 | React-приложение |
| **Keycloak** | 14082 | Сервер аутентификации и авторизации |

## 🚀 Быстрый запуск

### Требования

- [Docker](https://docs.docker.com/get-docker/) 20.10+
- [Docker Compose](https://docs.docker.com/compose/install/) 2.0+
- [Git](https://git-scm.com/)

### 1. Клонирование репозитория

```bash
git clone ...
cd avitosl
```

### 2. Запуск всех сервисов

```bash
# Из корня проекта
docker-compose up -d

# Или из папки back/microservices
cd back/microservices
docker-compose up -d
```

### 3. Проверка статуса

```bash
docker-compose ps
```

Все контейнеры должны быть в статусе `Up`.

### 4. Доступ к приложению

- **Фронтенд**: http://localhost:5173
- **API Gateway**: http://localhost:8081
- **Keycloak Admin**: http://localhost:14082
- **Eureka Server**: http://localhost:8761

## 🔐 Аутентификация

### Keycloak данные

- URL Admin Console: http://localhost:14082/admin
- Realm: `avitorealm`
- Администратор:
  - Логин: `keycloak`
  - Пароль: `keycloak`

### Frontend клиент

- Client ID: `avitofrontend`
- Redirect URI: `http://localhost:5173/callback`
- Scope: `openid profile email`

## 📁 Структура проекта

```
avitosl/
├── back/
│   └── microservices/              # Все бэкенд микросервисы
│       ├── api-gateway/           # Spring Cloud Gateway
│       │   ├── src/main/java/...
│       │   └── src/main/resources/application.yml
│       ├── user-service/          # Spring Boot 3.2
│       │   ├── src/
│       │   └── pom.xml
│       ├── category-service/      # Spring Boot 3.2
│       ├── post-service/          # Spring Boot 3.2
│       ├── chat-service/          # Spring Boot 3.2
│       ├── purchase-service/      # Spring Boot 3.2
│       ├── eureka-server/         # Spring Cloud Netflix Eureka
│       └── docker-compose.yml     # Docker Compose для бэкенда
├── front/                          # React + Vite фронтенд
│   ├── src/
│   │   ├── components/
│   │   ├── contexts/
│   │   ├── services/
│   │   ├── oidc.js               # Keycloak OIDC клиент
│   │   └── config.js
│   ├── nginx.conf                 # Nginx конфиг для production
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml             # Основной docker-compose (если есть)
├── docker-compose.back.yml        # Docker Compose для бэкенда
├── realm-export.json              # Экспорт Keycloak realm
├── keycloak-import/               # Папка для импорта realm
├── init.sql                       # Инициализация PostgreSQL
├── kill-services.sh               # Остановка всех сервисов
├── restart-services.sh            # Перезапуск сервисов
├── start-prod-microservices.sh    # Запуск продакшн-версии
└── start-debug-microservices.sh   # Запуск в режиме отладки
```

## 🛠️ Технологический стек

### Бэкенд
- **Java 21**
- **Spring Boot 3.2** (Web, Security, Data JPA, Actuator)
- **Spring Cloud 2023.0.0** (Gateway, Netflix Eureka Client, OpenFeign)
- **OAuth2 Resource Server** (JWT валидация)
- **PostgreSQL 15**
- **Keycloak 26** (OIDC)

### Фронтенд
- **React 18**
- **Vite**
- **react-oidc-context** (Keycloak интеграция)
- **Axios** (HTTP клиент)

### Инфраструктура
- **Docker** / **Docker Compose**
- **Nginx** (production)

## 🔧 Конфигурация

### Переменные окружения

Основные настройки можно изменить в `docker-compose.yml`:

```yaml
# База данных
POSTGRES_DB: avitodb
POSTGRES_USER: user
POSTGRES_PASSWORD: 111111

# Keycloak
KC_BOOTSTRAP_ADMIN_USERNAME: keycloak
KC_BOOTSTRAP_ADMIN_PASSWORD: keycloak
```

### Настройки приложений

Конфигурация каждого сервиса находится в `src/main/resources/application.yml`:

- **API Gateway** (`api-gateway/src/main/resources/application.yml`):
  - `jwk-set-uri`: `http://avitosl-keycloak:8080/realms/avitorealm/protocol/openid-connect/certs`
  - Маршрутизация:
    - `/api/users/**` → user-service
    - `/api/categories/**` → category-service
    - `/api/posts/**` → post-service
    - `/api/chat/**` → chat-service
    - `/api/purchases/**` → purchase-service
    - `/actuator/**` → локально


## 🗄️ База данных

### PostgreSQL контейнеры

| База данных | Порт | Контейнер |
|-------------|------|-----------|
| Приложение (user) | 5433 | avito-postgres |
| Keycloak | 14092 | avitosl-keycloak-db |

### Инициализация

База данных инициализируется автоматически через `init.sql` (при первом запуске) и Flyway миграции внутри сервисов.

## 🧪 Тестирование

```bash
# Сборка без тестов
cd back/microservices
docker-compose build --no-cache

# Запуск unit-тестов (требует Java 21 и Maven)
cd back/microservices/user-service
mvn test
```

## 🔄 Пересборка

Если требуется пересобрать всё с нуля:

```bash
# Остановка и удаление контейнеров + volumes
cd back/microservices
docker-compose down -v

# Удаление образов
docker rmi microservices_*

# Сборка заново
docker-compose build --no-cache
docker-compose up -d
```


## 🌐 Сети

Все контейнеры находятся в одной Docker-сети `avito-network`. Внутри сети они могут обращаться друг к другу по именам контейнеров:
- `avitosl-keycloak:8080` — Keycloak
- `avito-api-gateway:8080` — API Gateway
- `avito-eureka-server:8761` — Eureka
- `avito-post-service:8083` — Post Service
- и т.д.

## ⚙️ Разработка

### Запуск фронтенда в режиме разработки

```bash
cd front
npm install
npm run dev
```

## 🔐 Безопасность

- Все сервисы используют OAuth2 Resource Server
- Токены JWT валидируются через `jwk-set-uri`
- Token Relay на API Gateway пробрасывает токен в downstream сервисы
- CORS настроен для локальной разработки

## 📈 Мониторинг

- **Actuator endpoints**:
  - `/actuator/health` — health check
  - `/actuator/info` — информация о приложении
  - `/actuator/metrics` — метрики

- **Eureka Dashboard**: http://localhost:8761

использованные статьи:

https://forpes.ru/post/151824?ysclid=mlxl4jwmji504571908
https://medium.com/@tobintom/documenting-oauth2-secured-spring-boot-microservices-with-swagger-3-openapi-3-0-166618ea1f5
https://github.com/kali973/Springboot-ReactJS-keycloak/tree/master
https://vc.ru/dev/1779404-ot-hello-world-k-secure-api-nastraivaem-keycloak-i-spring-security-na-java
https://asbnotebook.com/spring-boot-and-keycloak-integration-example/
https://www.geeksforgeeks.org/javascript/how-to-set-up-vite-for-a-multi-page-application/
https://github.com/mayurkukatkar/Auth-by-spring-boot/tree/main
https://www.descope.com/blog/post/oauth2-react-authentication-authorization