# 뜬 구름 벡엔드 on Spring Boot, Cloud Story Backend on Spring Boot

Cloud Story Backend는 사용자 등록, 로그인, 게시글 작성, 댓글 작성 등의 기능을 제공하는 RESTful API 서버입니다. 이 프로젝트는 Spring Boot를 사용하여 구현되었습니다.

## 프로젝트 구조

```
cloud-story
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.example.cloud_story_be
│   │   │       ├── controller
│   │   │       ├── entity
│   │   │       ├── repository
│   │   │       ├── security
│   │   │       ├── service
│   │   │       └── CloudStoryBeApplication.java
│   │   └── resources
│   │       ├── application.properties
│   │       └── uploads (이미지 파일 저장 디렉토리)
│   └── test
├── initialize_cloud_story_db_schema.sql
├── initialize_cloud_story_db_data.sql
└── README.md
```

## 설치 및 실행

### 사전 요구사항

- Java 11 이상
- Maven
- MySQL

### 데이터베이스 설정

1. `initialize_cloud_story_db_schema.sql` 파일을 실행하여 데이터베이스 스키마를 초기화합니다.
2. `initialize_cloud_story_db_data.sql` 파일을 실행하여 더미 데이터를 삽입합니다.

### 프로젝트 빌드 및 실행

1. 프로젝트를 클론합니다.

```bash
git clone https://github.com/yourusername/cloud-story-backend.git
cd cloud-story-backend
```

2. `application.properties` 파일을 설정합니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cloud_story_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
jwt.secret=your_jwt_secret_key
```

3. Maven을 사용하여 프로젝트를 빌드하고 실행합니다.

```bash
mvn clean install
mvn spring-boot:run
```

## API 명세서

### 사용자 관련 API

#### 사용자 등록

- **메소드**: POST
- **URL**: /api/users/register
- **요청 헤더**:
  - Content-Type: multipart/form-data
- **요청 본문**:
  ```json
  {
    "user": "{\"email\": \"user@example.com\", \"password\": \"securePassword123\", \"nickname\": \"user1\"}",
    "profileImage": 파일 업로드 (선택)
  }
  ```
- **응답**:
  - 성공:
    ```json
    {
      "success": true,
      "userId": 1
    }
    ```
  - 실패:
    ```json
    {
      "success": false,
      "message": "Invalid user data"
    }
    ```

#### 로그인

- **메소드**: POST
- **URL**: /api/users/login
- **요청 헤더**:
  - Content-Type: application/json
- **요청 본문**:
  ```json
  {
    "email": "user@example.com",
    "password": "securePassword123"
  }
  ```
- **응답**:
  - 성공:
    ```json
    {
      "success": true,
      "token": "jwt-token-string",
      "userId": 1,
      "email": "user@example.com",
      "nickname": "user1",
      "profileImageUrl": "/uploads/12345_profile.jpg"
    }
    ```
  - 실패:
    ```json
    {
      "success": false,
      "message": "Invalid email or password"
    }
    ```

### 게시글 관련 API

#### 게시글 작성

- **메소드**: POST
- **URL**: /api/posts
- **요청 헤더**:
  - Authorization: Bearer jwt-token-string
  - Content-Type: multipart/form-data
- **요청 본문**:
  ```json
  {
    "title": "New Post Title",
    "content": "This is the content of the new post.",
    "photos": 파일 업로드 (선택)
  }
  ```
- **응답**:
  - 성공:
    ```json
    {
      "success": true,
      "postId": 1,
      "message": "Post created successfully"
    }
    ```
  - 실패:
    ```json
    {
      "success": false,
      "message": "Server error while creating the post"
    }
    ```

#### 게시글 조회

- **메소드**: GET
- **URL**: /api/posts/{postId}
- **요청 헤더**:
  - Authorization: Bearer jwt-token-string
- **응답**:
  - 성공:
    ```json
    {
      "success": true,
      "post": {
        "id": 1,
        "user": {
          "id": 1,
          "email": "user@example.com",
          "nickname": "user1",
          "profileImageUrl": "/uploads/12345_profile.jpg"
        },
        "title": "New Post Title",
        "content": "This is the content of the new post.",
        "createdAt": "2024-06-11T10:00:00",
        "updatedAt": "2024-06-11T10:00:00",
        "likeCount": 0,
        "dislikeCount": 0,
        "viewCount": 1,
        "photos": [
          {
            "id": 1,
            "url": "/uploads/12345_photo1.jpg",
            "photoOrder": 1
          }
        ]
      }
    }
    ```
  - 실패:
    ```json
    {
      "success": false,
      "message": "Post not found"
    }
    ```
