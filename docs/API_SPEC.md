# V-Log REST API 명세서

## 개요

- **Base URL**: `/api/v1`
- **인증 방식**: Session 기반 (Cookie: JSESSIONID)
- **Content-Type**: `application/json`

---

## 인증 API (Demo)

> Sprint 1 완성 전까지 사용하는 임시 인증 API

### 데모 로그인

세션에 userId를 저장하여 로그인 상태로 만듭니다.

```
POST /api/v1/auth/demo-login
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | Long | O | 로그인할 사용자 ID |

**Response (200 OK)**

```json
{
  "userId": 1,
  "email": "test@test.com",
  "nickname": "테스트유저",
  "message": "데모 로그인 성공"
}
```

---

### 데모 로그아웃

세션을 무효화하여 로그아웃 처리합니다.

```
POST /api/v1/auth/demo-logout
```

**Response (200 OK)**

```json
{
  "message": "로그아웃 성공"
}
```

---

### 현재 로그인 정보 조회

현재 세션의 로그인 상태를 확인합니다.

```
GET /api/v1/auth/demo-me
```

**Response (200 OK) - 로그인 상태**

```json
{
  "userId": 1,
  "email": "test@test.com",
  "nickname": "테스트유저"
}
```

**Response (200 OK) - 비로그인 상태**

```json
{
  "message": "로그인 상태가 아닙니다."
}
```

---/

## 게시글 API

### 게시글 목록 조회

게시글 목록을 페이징하여 조회합니다.

```
GET /api/v1/posts
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| page | int | X | 0 | 페이지 번호 (0부터 시작) |
| size | int | X | 10 | 페이지당 게시글 수 |
| tag | String | X | - | 태그로 필터링 |
| blogId | Long | X | - | 블로그 ID로 필터링 |

**Response (200 OK)**

```json
{
  "content": [
    {
      "postId": 1,
      "title": "게시글 제목",
      "summary": "게시글 내용의 앞 100자...",
      "author": {
        "userId": 1,
        "nickname": "테스트유저"
      },
      "tags": ["Spring", "Java"],
      "likeCount": 5,
      "commentCount": 3,
      "createdAt": "2024-01-01T12:00:00"
    }
  ],
  "pageInfo": {
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```

---

### 게시글 상세 조회

특정 게시글의 상세 정보를 조회합니다.

```
GET /api/v1/posts/{postId}
```

**Path Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| postId | Long | O | 게시글 ID |

**Response (200 OK)**

```json
{
  "postId": 1,
  "title": "게시글 제목",
  "content": "게시글 전체 내용",
  "author": {
    "userId": 1,
    "nickname": "테스트유저"
  },
  "tags": ["Spring", "Java"],
  "likeCount": 5,
  "isLiked": true,
  "comments": [
    {
      "commentId": 1,
      "content": "댓글 내용",
      "author": {
        "userId": 2,
        "nickname": "댓글작성자"
      },
      "createdAt": "2024-01-01T13:00:00"
    }
  ],
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T14:00:00"
}
```

| 필드 | 설명 |
|------|------|
| isLiked | 현재 로그인한 사용자가 좋아요 했는지 여부 (비로그인시 false) |

---

### 게시글 작성

새 게시글을 작성합니다.

```
POST /api/v1/posts
```

**인증**: 필수 (로그인 필요)

**Request Body**

```json
{
  "title": "게시글 제목",
  "content": "게시글 내용",
  "tags": ["Spring", "Java"]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | O | 게시글 제목 (빈 문자열 불가) |
| content | String | O | 게시글 내용 (빈 문자열 불가) |
| tags | String[] | X | 태그 목록 |

**Response (201 Created)**

```json
{
  "postId": 1,
  "title": "게시글 제목",
  "content": "게시글 내용",
  "author": {
    "userId": 1,
    "nickname": "테스트유저"
  },
  "tags": ["Spring", "Java"],
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": null
}
```

**Error Response**

| 상태 코드 | 설명 |
|----------|------|
| 401 Unauthorized | 로그인이 필요합니다 |
| 400 Bad Request | 유효성 검증 실패 (제목/내용 누락) |

---

### 게시글 수정

게시글을 수정합니다. 작성자만 수정 가능합니다.

```
PUT /api/v1/posts/{postId}
```

**인증**: 필수 (로그인 필요)

**Path Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| postId | Long | O | 게시글 ID |

**Request Body**

```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "tags": ["Spring", "JPA"]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | O | 게시글 제목 |
| content | String | O | 게시글 내용 |
| tags | String[] | X | 태그 목록 (기존 태그 전체 교체) |

**Response (200 OK)**

```json
{
  "postId": 1,
  "title": "수정된 제목",
  "content": "수정된 내용",
  "author": {
    "userId": 1,
    "nickname": "테스트유저"
  },
  "tags": ["Spring", "JPA"],
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T15:00:00"
}
```

**Error Response**

| 상태 코드 | 설명 |
|----------|------|
| 401 Unauthorized | 로그인이 필요합니다 |
| 403 Forbidden | 수정 권한이 없습니다 (작성자가 아님) |
| 404 Not Found | 게시글을 찾을 수 없습니다 |

---

### 게시글 삭제

게시글을 삭제합니다. 작성자만 삭제 가능합니다.

```
DELETE /api/v1/posts/{postId}
```

**인증**: 필수 (로그인 필요)

**Path Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| postId | Long | O | 게시글 ID |

**Response (204 No Content)**

응답 본문 없음

**Error Response**

| 상태 코드 | 설명 |
|----------|------|
| 401 Unauthorized | 로그인이 필요합니다 |
| 403 Forbidden | 삭제 권한이 없습니다 (작성자가 아님) |
| 404 Not Found | 게시글을 찾을 수 없습니다 |

---

## 공통 응답 형식

### 페이징 정보 (PageInfo)

```json
{
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| page | int | 현재 페이지 번호 (0부터 시작) |
| size | int | 페이지당 항목 수 |
| totalElements | long | 전체 항목 수 |
| totalPages | int | 전체 페이지 수 |
| first | boolean | 첫 페이지 여부 |
| last | boolean | 마지막 페이지 여부 |

### 작성자 정보 (Author)

```json
{
  "userId": 1,
  "nickname": "테스트유저"
}
```

---

## 테스트 예시 (curl)

### 로그인

```bash
curl -X POST "http://localhost:8080/api/v1/auth/demo-login?userId=1" \
  -c cookies.txt
```

### 게시글 작성

```bash
curl -X POST http://localhost:8080/api/v1/posts \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"title":"제목","content":"내용","tags":["Spring","Java"]}'
```

### 게시글 목록 조회

```bash
curl http://localhost:8080/api/v1/posts
curl "http://localhost:8080/api/v1/posts?tag=Spring&page=0&size=5"
```

### 게시글 상세 조회

```bash
curl http://localhost:8080/api/v1/posts/1 -b cookies.txt
```

### 게시글 수정

```bash
curl -X PUT http://localhost:8080/api/v1/posts/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"title":"수정된 제목","content":"수정된 내용","tags":["JPA"]}'
```

### 게시글 삭제

```bash
curl -X DELETE http://localhost:8080/api/v1/posts/1 -b cookies.txt
```

---

## DTO 구조 상세

### Request DTO

#### PostCreateRequest

게시글 작성 시 사용하는 요청 DTO입니다.

```java
public class PostCreateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> tags;  // 선택사항
}
```

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|----------|------|
| title | String | O | @NotBlank | null, "", " " 불허 |
| content | String | O | @NotBlank | null, "", " " 불허 |
| tags | List<String> | X | - | null 허용, 빈 배열 허용 |

#### PostUpdateRequest

게시글 수정 시 사용하는 요청 DTO입니다. PostCreateRequest와 동일한 구조입니다.

```java
public class PostUpdateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> tags;
}
```

---

### Response DTO

#### PostResponse

게시글 상세 조회, 작성, 수정 시 응답하는 DTO입니다.

```java
public class PostResponse {
    private Long postId;
    private String title;
    private String content;
    private AuthorResponse author;
    private List<String> tags;
    private int likeCount;
    private boolean isLiked;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

| 필드 | 타입 | 상세 조회 | 작성/수정 | 설명 |
|------|------|----------|----------|------|
| postId | Long | O | O | 게시글 ID |
| title | String | O | O | 게시글 제목 |
| content | String | O | O | 게시글 전체 내용 |
| author | AuthorResponse | O | O | 작성자 정보 |
| tags | List<String> | O | O | 태그 목록 |
| likeCount | int | O | X | 좋아요 수 |
| isLiked | boolean | O | X | 현재 사용자의 좋아요 여부 |
| comments | List<CommentResponse> | O | X | 댓글 목록 |
| createdAt | LocalDateTime | O | O | 작성일시 |
| updatedAt | LocalDateTime | O | O | 수정일시 |

#### PostListResponse

게시글 목록 조회 시 응답하는 DTO입니다. 목록에 최적화된 요약 정보를 제공합니다.

```java
public class PostListResponse {
    private Long postId;
    private String title;
    private String summary;        // content 앞 100자 + "..."
    private AuthorResponse author;
    private List<String> tags;
    private int likeCount;
    private int commentCount;      // 댓글 개수만 표시
    private LocalDateTime createdAt;
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| postId | Long | 게시글 ID |
| title | String | 게시글 제목 |
| summary | String | 본문 앞 100자 요약 (100자 초과 시 "..." 추가) |
| author | AuthorResponse | 작성자 정보 |
| tags | List<String> | 태그 목록 |
| likeCount | int | 좋아요 수 |
| commentCount | int | 댓글 개수 |
| createdAt | LocalDateTime | 작성일시 |

#### PageResponse<T>

페이징 처리된 목록을 반환하는 공통 DTO입니다. 제네릭으로 다양한 목록에 재사용 가능합니다.

```java
public class PageResponse<T> {
    private List<T> content;       // 실제 데이터 목록
    private PageInfo pageInfo;     // 페이징 메타 정보

    public static class PageInfo {
        private int page;           // 현재 페이지 (0부터 시작)
        private int size;           // 페이지당 개수
        private long totalElements; // 전체 데이터 개수
        private int totalPages;     // 전체 페이지 수
        private boolean first;      // 첫 페이지 여부
        private boolean last;       // 마지막 페이지 여부
    }
}
```

#### AuthorResponse

사용자 정보를 담는 중첩 DTO입니다. PostResponse, PostListResponse, CommentResponse에서 사용됩니다.

```java
public class AuthorResponse {
    private Long userId;
    private String nickname;
}
```

#### CommentResponse

댓글 정보를 담는 DTO입니다. PostResponse의 comments 필드에 포함됩니다.

```java
public class CommentResponse {
    private Long commentId;
    private String content;
    private AuthorResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 데이터 흐름

### 게시글 작성 흐름

```
[Client]                    [Controller]              [Service]              [Repository]
   |                             |                        |                       |
   |-- POST /api/v1/posts ------>|                        |                       |
   |   PostCreateRequest         |                        |                       |
   |   {title, content, tags}    |                        |                       |
   |                             |                        |                       |
   |                             |-- createPost() ------->|                       |
   |                             |   (request, userId)    |                       |
   |                             |                        |                       |
   |                             |                        |-- save(Post) -------->|
   |                             |                        |<-- Post --------------|
   |                             |                        |                       |
   |                             |                        |-- saveTags() -------->|
   |                             |                        |<-- List<Tag> ---------|
   |                             |                        |                       |
   |                             |<-- PostResponse -------|                       |
   |<-- 201 Created -------------|                        |                       |
   |    PostResponse             |                        |                       |
```

### 게시글 목록 조회 흐름

```
[Client]                    [Controller]              [Service]              [Repository]
   |                             |                        |                       |
   |-- GET /api/v1/posts ------->|                        |                       |
   |   ?page=0&size=10&tag=Spring|                        |                       |
   |                             |                        |                       |
   |                             |-- getPosts() --------->|                       |
   |                             |   (tag, blogId,        |                       |
   |                             |    Pageable)           |                       |
   |                             |                        |                       |
   |                             |                        |-- findAll() --------->|
   |                             |                        |<-- Page<Post> --------|
   |                             |                        |                       |
   |                             |                        |-- (각 Post에 대해)    |
   |                             |                        |   getTags(),          |
   |                             |                        |   getLikeCount(),     |
   |                             |                        |   getCommentCount()   |
   |                             |                        |                       |
   |                             |<-- PageResponse<       |                       |
   |                             |    PostListResponse>   |                       |
   |<-- 200 OK ------------------|                        |                       |
   |    PageResponse             |                        |                       |
```

### 인증 흐름 (Spring Security)

```
[Client]                    [SecurityFilter]          [Controller]           [Service]
   |                             |                        |                       |
   |-- POST /api/v1/posts ------>|                        |                       |
   |   (with session cookie)     |                        |                       |
   |                             |                        |                       |
   |                             |-- 인증 확인 ---------->|                       |
   |                             |   SecurityContext에서  |                       |
   |                             |   인증 정보 추출       |                       |
   |                             |                        |                       |
   |                             |   인증 실패 시         |                       |
   |<-- 401 Unauthorized --------|   (anyRequest()        |                       |
   |                             |    .authenticated())   |                       |
   |                             |                        |                       |
   |                             |   인증 성공 시         |                       |
   |                             |-- @AuthenticationPrincipal                     |
   |                             |   CustomUserDetails    |                       |
   |                             |----------------------->|                       |
   |                             |                        |-- createPost() ------>|
   |                             |                        |   userDetails         |
   |                             |                        |   .getUserId()        |
```

---

## DTO 변환 규칙

### Entity → Response 변환

| Entity | Response | 변환 메서드 | 용도 |
|--------|----------|------------|------|
| Post | PostResponse | `PostResponse.of()` | 상세 조회 (좋아요, 댓글 포함) |
| Post | PostResponse | `PostResponse.of()` | 작성/수정 응답 (좋아요, 댓글 제외) |
| Post | PostListResponse | `PostListResponse.of()` | 목록 조회 (요약 정보) |
| User | AuthorResponse | `AuthorResponse.from()` | 작성자 정보 |
| Comment | CommentResponse | `CommentResponse.from()` | 댓글 정보 |
| Page<Post> | PageResponse | `PageResponse.of()` | 페이징 응답 |

### 변환 예시

```java
// 상세 조회용 변환
PostResponse.of(post, tags, likeCount, isLiked, comments);

// 작성/수정 응답용 변환 (좋아요, 댓글 제외)
PostResponse.of(post, tags);

// 목록 조회용 변환
PostListResponse.of(post, tags, likeCount, commentCount);

// 페이징 응답 변환
PageResponse.of(postPage, postListResponses);
```