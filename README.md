# 뜬 구름 벡엔드 on Spring Boot<br>
# Cloud Story Backend on Spring Boot

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


## 1. 사용자 User 관련 API 목록

### 1. 사용자 회원가입

- **메소드**: POST
- **URL**: /api/users/register
- **Headers**:
    - Content-Type: multipart/form-data
- **Request Body**:
    - user (String): 사용자 정보를 JSON 형식으로 포함
    - profileImage (MultipartFile): 선택 사항, 프로필 이미지
- **Request Example**:
    
    ```swift
    {
        "user": "{\\"email\\":\\"user@example.com\\",\\"password\\":\\"securePassword123\\",\\"nickname\\":\\"userNick\\"}",
        "profileImage": (file)
    }
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "userId": 1,
        "profileImageUrl": "/uploads/profile.jpg"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid user data"
    }
    
    ```
    

### 2. 사용자 로그인

- **메소드**: POST
- **URL**: /api/users/login
- **Headers**:
    - Content-Type: application/json
- **Request Body**:
    - email (String): 사용자 이메일
    - password (String): 사용자 비밀번호
- **Request Example**:
    
    ```json
    {
        "email": "user@example.com",
        "password": "securePassword123"
    }
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "token": "jwt-token-string",
        "userId": 1,
        "email": "user@example.com",
        "nickname": "userNick",
        "profileImageUrl": "/uploads/profile.jpg"
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Invalid email or password"
    }
    
    ```
    

### 3. 이메일 중복 확인

- **메소드**: POST
- **URL**: /api/users/check-email
- **Headers**:
    - Content-Type: application/json
- **Request Body**:
    - email (String): 중복 확인할 이메일
- **Request Example**:
    
    ```json
    {
        "email": "user@example.com"
    }
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Verification code sent to email"
    }
    
    ```
    
    - **실패**: 409 Conflict
    
    ```json
    {
        "success": false,
        "message": "Email already in use"
    }
    
    ```
    

### 4. 닉네임 중복 확인

- **메소드**: GET
- **URL**: /api/users/check-nickname
- **Request Parameters**:
    - nickname (String): 중복 확인할 닉네임
- **Request Example**:
    
    ```
    /api/users/check-nickname?nickname=userNick
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "available": true
    }
    
    ```
    
    - **실패**: 409 Conflict
    
    ```json
    {
        "success": false,
        "available": false,
        "message": "Nickname already in use"
    }
    
    ```
    

### 5. 이메일 인증 코드 확인

- **메소드**: POST
- **URL**: /api/users/verify-email
- **Headers**:
    - Content-Type: application/json
- **Request Body**:
    - email (String): 인증할 이메일
    - verificationCode (String): 인증 코드
- **Request Example**:
    
    ```json
    {
        "email": "user@example.com",
        "verificationCode": "123456"
    }
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Email verified successfully"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid verification code"
    }
    
    ```
    

### 6. 사용자 정보 수정

- **메소드**: PUT
- **URL**: /api/users/update
- **Headers**:
    - Authorization: Bearer jwt-token-string
    - Content-Type: multipart/form-data
- **Request Body**:
    - user (String): 수정할 사용자 정보를 JSON 형식으로 포함
    - profileImage (MultipartFile): 선택 사항, 수정할 프로필 이미지
- **Request Example**:
    
    ```swift
    {
        "user": "{\\"nickname\\":\\"newNick\\", \\"password\\":\\"newPassword123\\"}",
        "profileImage": (file)
    }
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "User information updated successfully"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "User not found"
    }
    
    ```
    

### 7. 사용자 탈퇴

- **메소드**: DELETE
- **URL**: /api/users/delete
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Example**:
    
    ```
    DELETE /api/users/delete HTTP/1.1
    Host: example.com
    Authorization: Bearer jwt-token-string
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "User account deleted successfully"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "User not found"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Failed to delete user account"
    }
    
    ```
    

## 2. 게시글 Post 관련 API 목록

### 1. 게시글 작성

- **메소드**: POST
- **URL**: /api/posts
- **Headers**:
    - Authorization: Bearer jwt-token-string
    - Content-Type: multipart/form-data
- **Request Body**:
    - title (String): 게시글 제목
    - content (String): 게시글 내용
    - photos (List<MultipartFile>): 선택 사항, 게시글 사진
- **Request Example**:
    
    ```swift
    {
        "title": "New Post Title",
        "content": "This is the content of the new post.",
        "photos": [(file1), (file2)]
    }
    
    ```
    
- **Response**:
    - **성공**: 201 Created
    
    ```json
    {
        "success": true,
        "postId": 1,
        "message": "Post created successfully"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while creating the post"
    }
    
    ```
    

### 2. 게시글 조회 (페이징)

- **메소드**: GET
- **URL**: /api/posts
- **Headers**:
    - Content-Type: application/json
- **Request Parameters**:
    - page (int): 페이지 번호 (기본값: 1)
    - limit (int): 페이지 당 게시글 수 (기본값: 10)
- **Request Example**:
    
    ```
    /api/posts?page=1&limit=10
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "currentPage": 1,
        "totalPages": 10,
        "totalPosts": 100,
        "posts": [
            {
                "id": 1,
                "title": "Post Title",
                "content": "This is the content of the post.",
                "createdAt": "2023-01-01T13:00:00Z",
                "updatedAt": "2023-01-01T13:00:00Z",
                "likeCount": 10,
                "dislikeCount": 2,
                "viewCount": 100,
                "photos": [
                    {
                        "id": 1,
                        "url": "/uploads/photo1.jpg",
                        "photoOrder": 1
                    }
                ]
            }
        ]
    }
    
    ```
    

### 3. 게시글 상세 조회

- **메소드**: GET
- **URL**: /api/posts/{postId}
- **Headers**:
    - Content-Type: application/json
- **Request Parameters**:
    - postId (Long): 조회할 게시글의 ID
- **Request Example**:
    
    ```
    /api/posts/1
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "post": {
            "id": 1,
            "title": "Post Title",
            "content": "This is the content of the post.",
            "createdAt": "2023-01-01T13:00:00Z",
            "updatedAt": "2023-01-01T13:00:00Z",
            "likeCount": 10,
            "dislikeCount": 2,
            "viewCount": 100,
            "photos": [
                {
                    "id": 1,
                    "url": "/uploads/photo1.jpg",
                    "photoOrder": 1
                }
            ]
        }
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Post not found"
    }
    
    ```
    

### 4. 게시글 수정

- **메소드**: PUT
- **URL**: /api/posts/{postId}
- **Headers**:
    - Authorization: Bearer jwt-token-string
    - Content-Type: multipart/form-data
- **Request Parameters**:
    - postId (Long): 수정할 게시글의 ID
- **Request Body**:
    - title (String): 게시글 제목
    - content (String): 게시글 내용
    - photos (List<MultipartFile>): 선택 사항, 게시글 사진
- **Request Example**:
    
    ```swift
    {
        "title": "Updated Post Title",
        "content": "This is the updated content of the post.",
        "photos": [(file1), (file2)]
    }
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Post updated successfully"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Post not found"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    

### 5. 게시글 삭제

- **메소드**: DELETE
- **URL**: /api/posts/{postId}
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Parameters**:
    - postId (Long): 삭제할 게시글의 ID
- **Request Example**:
    
    ```
    /api/posts/1
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Post deleted successfully"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Post not found"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    

### 6. 게시글 좋아요

- **메소드**: POST
- **URL**: /api/posts/{postId}/like
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Parameters**:
    - postId (Long): 좋아요를 할 게시글의 ID
- **Request Example**:
    
    ```
    /api/posts/1/like
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Post liked successfully"
    }
    
    ```
    
    - **실패**: 409 Conflict
    
    ```json
    {
        "success": false,
        "message": "Post already liked by user"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    

### 7. 게시글 좋아요 취소

- **메소드**: DELETE
- **URL**: /api/posts/{postId}/like
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Parameters**:
    - postId (Long): 좋아요를 취소할 게시글의 ID
- **Request Example**:
    
    ```
    /api/posts/1/like
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Post like removed successfully"
    }
    
    ```
    
    - **실패**: 409 Conflict
    
    ```json
    {
        "success": false,
        "message": "Post not liked by user"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    

### 8. 게시글 싫어요

- **메소드**: POST
- **URL**: /api/posts/{postId}/dislike
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Parameters**:
    - postId (Long): 싫어요를 할 게시글의 ID
- **Request Example**:
    
    ```
    /api/posts/1/dislike
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Post disliked successfully"
    }
    
    ```
    
    - **실패**: 409 Conflict
    
    ```json
    {
        "success": false,
        "message": "Post already disliked by user"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    

### 9. 게시글 싫어요 취소

- **메소드**: DELETE
- **URL**: /api/posts/{postId}/dislike
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Parameters**:
    - postId (Long):

싫어요를 취소할 게시글의 ID

- **Request Example**:
    
    ```
    /api/posts/1/dislike
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Post dislike removed successfully"
    }
    
    ```
    
    - **실패**: 409 Conflict
    
    ```json
    {
        "success": false,
        "message": "Post not disliked by user"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    

### 10. 오늘의 인기 게시글 조회

- **메소드**: GET
- **URL**: /api/posts/popular/today
- **Headers**:
    - Content-Type: application/json
- **Request Example**:
    
    ```
    /api/posts/popular/today
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "posts": [
            {
                "id": 1,
                "title": "Post Title",
                "content": "This is the content of the post.",
                "createdAt": "2023-01-01T13:00:00Z",
                "updatedAt": "2023-01-01T13:00:00Z",
                "likeCount": 10,
                "dislikeCount": 2,
                "viewCount": 100,
                "photos": [
                    {
                        "id": 1,
                        "url": "/uploads/photo1.jpg",
                        "photoOrder": 1
                    }
                ]
            }
        ]
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while fetching today's popular posts"
    }
    
    ```
    

### 11. 이번 주 인기 게시글 조회

- **메소드**: GET
- **URL**: /api/posts/popular/week
- **Headers**:
    - Content-Type: application/json
- **Request Example**:
    
    ```
    /api/posts/popular/week
    ```
    
- **Response**:
- **성공**: 200 OK

```json
{
    "success": true,
    "posts": [
        {
            "id": 1,
            "title": "Post Title",
            "content": "This is the content of the post.",
            "createdAt": "2023-01-01T13:00:00Z",
            "updatedAt": "2023-01-01T13:00:00Z",
            "likeCount": 10,
            "dislikeCount": 2,
            "viewCount": 100,
            "photos": [
                {
                    "id": 1,
                    "url": "/uploads/photo1.jpg",
                    "photoOrder": 1
                }
            ]
        }
    ]
}

```

- **실패**: 500 Internal Server Error

```json
{
    "success": false,
    "message": "Server error while fetching this week's popular posts"
}

```

## 3. 댓글 comment 관련 API 목록

### 1. 댓글 추가

- **메소드**: POST
- **URL**: /api/posts/{postId}/comments
- **Headers**:
    - Content-Type: application/json
    - Authorization: Bearer jwt-token-string
- **Request Body**:
    - content (String): 댓글 내용
- **Request Example**:
    
    ```json
    {
        "content": "This is a comment on the post."
    }
    
    ```
    
- **Response**:
    - **성공**: 201 Created
    
    ```json
    {
        "success": true,
        "commentId": 67890,
        "message": "Comment added successfully"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Post not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while adding the comment"
    }
    
    ```
    

### 2. 댓글 조회 (페이징)

- **메소드**: GET
- **URL**: /api/posts/{postId}/comments
- **Headers**:
    - Content-Type: application/json
    - Authorization: Bearer jwt-token-string
- **Request Parameters**:
    - page (int): 페이지 번호 (기본값: 1)
    - limit (int): 페이지 당 댓글 수 (기본값: 10)
- **Request Example**:
    
    ```
    /api/posts/1/comments?page=1&limit=10
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "currentPage": 1,
        "totalPages": 5,
        "totalComments": 50,
        "comments": [
            {
                "commentId": 1,
                "author": "Commenter1",
                "authorProfileImage": "<http://example.com/profile1.jpg>",
                "content": "This is the first comment.",
                "createdAt": "2023-01-01T13:00:00Z",
                "likes": 10,
                "dislikes": 0
            },
            {
                "commentId": 2,
                "author": "Commenter2",
                "authorProfileImage": "<http://example.com/profile2.jpg>",
                "content": "This is the second comment.",
                "createdAt": "2023-01-01T14:00:00Z",
                "likes": 5,
                "dislikes": 1
            }
        ]
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Post not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while fetching comments"
    }
    
    ```
    

### 3. 댓글 수정

- **메소드**: PUT
- **URL**: /api/posts/{postId}/comments/{commentId}
- **Headers**:
    - Content-Type: application/json
    - Authorization: Bearer jwt-token-string
- **Request Body**:
    - content (String): 수정된 댓글 내용
- **Request Example**:
    
    ```json
    {
        "content": "Updated content of the comment."
    }
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Comment updated successfully"
    }
    
    ```
    
    - **실패**: 400 Bad Request
    
    ```json
    {
        "success": false,
        "message": "Invalid request data"
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Comment not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while updating the comment"
    }
    
    ```
    

### 4. 댓글 삭제

- **메소드**: DELETE
- **URL**: /api/posts/{postId}/comments/{commentId}
- **Headers**:
    - Content-Type: application/json
    - Authorization: Bearer jwt-token-string
- **Request Example**:
    
    ```
    /api/posts/1/comments/1
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Comment deleted successfully"
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Comment not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while deleting the comment"
    }
    
    ```
    

### 5. 댓글 좋아요

- **메소드**: POST
- **URL**: /api/posts/{postId}/comments/{commentId}/like
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Example**:
    
    ```
    /api/posts/1/comments/1/like
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Comment liked successfully"
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Comment not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while liking the comment"
    }
    
    ```
    

### 6. 댓글 좋아요 취소

- **메소드**: DELETE
- **URL**: /api/posts/{postId}/comments/{commentId}/like
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Example**:
    
    ```
    /api/posts/1/comments/1/like
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Comment like removed successfully"
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Comment not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while removing like from the comment"
    }
    
    ```
    

### 7. 댓글 싫어요

- **메소드**: POST
- **URL**: /api/posts/{postId}/comments/{commentId}/dislike
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Example**:
    
    ```
    /api/posts/1/comments/1/dislike
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Comment disliked successfully"
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Comment not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while disliking the comment"
    }
    
    ```
    

### 8. 댓글 싫어요 취소

- **메소드**: DELETE
- **URL**: /api/posts/{postId}/comments/{commentId}/dislike
- *Headers**:
- Authorization: Bearer jwt-token-string
- **Request Example**:
    
    ```
    /api/posts/1/comments/1/dislike
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    
    ```json
    {
        "success": true,
        "message": "Comment dislike removed successfully"
    }
    
    ```
    
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "Comment not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while removing dislike from the comment"
    }
    
    ```
    

## 4. 기타 API 목록

### 1. 사진 조회

- **메소드**: GET
- **URL**: /uploads/{fileName}
- **Headers**:
    - Authorization: Bearer jwt-token-string
- **Request Parameters**:
    - fileName (String): 조회할 파일의 이름
- **Request Example**:
    
    ```
    GET /uploads/example.jpg
    Headers:
    {
        "Authorization": "Bearer jwt-token-string"
    }
    
    ```
    
- **Response**:
    - **성공**: 200 OK
    - **실패**: 401 Unauthorized
    
    ```json
    {
        "success": false,
        "message": "Unauthorized access"
    }
    
    ```
    
    - **실패**: 404 Not Found
    
    ```json
    {
        "success": false,
        "message": "File not found"
    }
    
    ```
    
    - **실패**: 500 Internal Server Error
    
    ```json
    {
        "success": false,
        "message": "Server error while fetching the file"
    }
    
    ```
    

## 5. 추가 예정 생각 중인 API 목록

### **1. 게시글 검색**

- **메소드:** GET
- **URL:** `/api/posts/search`
- **설명:** 제목 또는 내용을 기반으로 게시글을 검색합니다.
- **Request Parameters:** `query` (검색어), `page` (페이지 번호), `limit` (페이지당 게시글 수)
- **Authorization:** Bearer token
- **Response:**
    - 200 OK: 검색 결과 반환
    - 401 Unauthorized: 인증 실패
    - 500 Internal Server Error: 서버 오류

### **2. 유저의 게시글 목록 조회**

- **메소드:** GET
- **URL:** `/api/users/{userId}/posts`
- **설명:** 특정 유저가 작성한 게시글 목록을 조회합니다.
- **Request Parameters:** `page` (페이지 번호), `limit` (페이지당 게시글 수)
- **Authorization:** Bearer token
- **Response:**
    - 200 OK: 게시글 목록 반환
    - 401 Unauthorized: 인증 실패
    - 404 Not Found: 유저를 찾을 수 없음
    - 500 Internal Server Error: 서버 오류

### **3. 댓글의 댓글 추가 (대댓글)**

- **메소드:** POST
- **URL:** `/api/posts/{postId}/comments/{commentId}/replies`
- **설명:** 특정 댓글에 대한 대댓글을 추가합니다.
- **Request Body:** `content` (댓글 내용)
- **Authorization:** Bearer token
- **Response:**
    - 201 Created: 대댓글 추가 성공
    - 400 Bad Request: 요청 데이터가 잘못됨
    - 401 Unauthorized: 인증 실패
    - 404 Not Found: 댓글을 찾을 수 없음
    - 500 Internal Server Error: 서버 오류

### **4. 특정 유저의 댓글 목록 조회**

- **메소드:** GET
- **URL:** `/api/users/{userId}/comments`
- **설명:** 특정 유저가 작성한 댓글 목록을 조회합니다.
- **Request Parameters:** `page` (페이지 번호), `limit` (페이지당 댓글 수)
- **Authorization:** Bearer token
- **Response:**
    - 200 OK: 댓글 목록 반환
    - 401 Unauthorized: 인증 실패
    - 404 Not Found: 유저를 찾을 수 없음
    - 500 Internal Server Error: 서버 오류

### **5. 특정 게시글의 댓글 수 조회**

- **메소드:** GET
- **URL:** `/api/posts/{postId}/comments/count`
- **설명:** 특정 게시글의 댓글 수를 조회합니다.
- **Request Parameters:** 없음
- **Authorization:** Bearer token
- **Response:**
    - 200 OK: 댓글 수 반환
    - 401 Unauthorized: 인증 실패
    - 404 Not Found: 게시글을 찾을 수 없음
    - 500 Internal Server Error: 서버 오류
