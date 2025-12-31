## API 엔드포인트 요약

## 1: 인증 & 사용자

| Method | Endpoint           | 설명              | 인증     | 수정or 추가 |
| ------ | ------------------ | --------------- | ------ | ------- |
| POST   | `/auth/signup`     | 회원가입 (블로그 자동생성) | X      |         |
| POST   | `/auth/login`      | 로그인             | X      |         |
| POST   | `/auth/logout`     | 로그아웃            | O      |         |
| GET    | `/users/{user_id}` | 사용자 프로필 조회      | X      |         |
| PUT    | `/users/{user_id}` | 사용자 정보 수정       | O (본인) |         |
| DELETE | `/users/{user_id}` | 회원 탈퇴           | O (본인) |         |

## 2: 게시글

| Method | Endpoint           | 설명                   | 인증      | 수정or 추가 |
| ------ | ------------------ | -------------------- | ------- | ------- |
| GET    | `/posts`           | 전체 게시글 조회 (페이징, 필터링) | X       | O       |
| GET    | `/posts/{post_id}` | 게시글 상세 조회 (댓글 포함)    | X       | O       |
| POST   | `/posts`           | 게시글 작성               | O       |         |
| PUT    | `/posts/{post_id}` | 게시글 수정               | O (작성자) |         |
| DELETE | `/posts/{post_id}` | 게시글 삭제               | O (작성자) |         |
|        |                    |                      |         |         |

## 3: 댓글 & 좋아요

| Method | Endpoint                                                 | 설명     | 인증      | 수정or 추가 |
| ------ | -------------------------------------------------------- | ------ | ------- | ------- |
| POST   | `/posts/{post_id}/comments`                              | 댓글 작성  | O       |         |
| PUT    | `/posts/{post_id}/comments/{comment_id}`                 | 댓글 수정  | O (작성자) |         |
| DELETE | `/posts/{post_id}/comments/{comment_id}`                 | 댓글 삭제  | O (작성자) |         |
| POST   | `/posts/{post_id}/like`                                  | 좋아요    | O       |         |
| DELETE | `/posts/{post_id}/like`                                  | 좋아요 취소 | O       |         |
| POST   | `/posts/{postId}/comments/{commentId}/replies`           | 답글 생성  | O       | O       |
| PUT    | `/posts/{postId}/comments/{commentId}/replies/{replyId}` | 답글수정   | O (작성자) | O       |
| DELETE | `/posts/{postId}/comments/{commentId}/replies/{replyId}` | 답글삭제   | O (작성자) | O       |

## 4: 팔로우

| Method | Endpoint                      | 설명        | 인증  | 수정or 추가 |
| ------ | ----------------------------- | --------- | --- | ------- |
| GET    | `/users/{user_id}/followers`  | 팔로워 목록 조회 | O   |         |
| GET    | `/users/{user_id}/followings` | 팔로잉 목록 조회 | O   |         |
| POST   | `/users/{user_id}/follow`     | 팔로우       | O   |         |
| DELETE | `/users/{user_id}/follow`     | 언팔로우      | O   |         |

---

## Sprint 1: 인증 & 사용자

### 1.1 회원가입

사용자 등록 시 블로그가 자동으로 생성됩니다.

| 항목      | 내용                  |
| ------- | ------------------- |
| **URL** | `POST /auth/signup` |
| **인증**  | 불필요                 |

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "홍길동"
}
```

**Response**

- `201 Created`

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "홍길동",
  "blogId": 1,
  "createdAt": "2024-12-23T10:00:00"
}
```

**Error Response**
- `400 Bad Request` - 유효성 검증 실패
- `409 Conflict` - 이메일 또는 닉네임 중복

```json
{
  "status": 400,
  "message": "이메일 형식이 올바르지 않습니다."
}
```

---

### 1.2 로그인

|항목|내용|
|---|---|
|**URL**|`POST /auth/login`|
|**인증**|불필요|

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response**

- `200 OK`

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "홍길동",
  "message": "로그인 성공"
}
```

**Error Response**

- `401 Unauthorized` - 이메일 또는 비밀번호 불일치

```json
{
  "status": 401,
  "message": "이메일 또는 비밀번호가 일치하지 않습니다."
}
```

---

### 1.3 로그아웃

|항목|내용|
|---|---|
|**URL**|`POST /auth/logout`|
|**인증**|필요|

**Response**

- `200 OK`

```json
{
  "message": "로그아웃 성공"
}
```

**Error Response**

- `401 Unauthorized` - 로그인 상태가 아님

---

### 1.4 사용자 프로필 조회

|항목|내용|
|---|---|
|**URL**|`GET /users/{user_id}`|
|**인증**|불필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|user_id|Long|사용자 ID|

**Response**

- `200 OK`

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "홍길동",
  "profileImage": "<https://example.com/image.jpg>",
  "bio": "안녕하세요, 개발자입니다.",
  "blogId": 1,
  "createdAt": "2024-12-23T10:00:00"
}
```

**Error Response**

- `404 Not Found` - 사용자를 찾을 수 없음

```json
{
  "status": 404,
  "message": "사용자를 찾을 수 없습니다."
}
```

---

### 1.5 사용자 정보 수정

|항목|내용|
|---|---|
|**URL**|`PUT /users/{user_id}`|
|**인증**|필요 (본인만 가능)|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|user_id|Long|사용자 ID|

**Request Body**

```json
{
  "nickname": "새닉네임",
  "profileImage": "<https://example.com/new-image.jpg>",
  "bio": "수정된 자기소개입니다."
}
```

**Response**

- `200 OK`

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "새닉네임",
  "profileImage": "<https://example.com/new-image.jpg>",
  "bio": "수정된 자기소개입니다.",
  "updatedAt": "2024-12-23T12:00:00"
}
```

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `403 Forbidden` - 본인이 아님
- `409 Conflict` - 닉네임 중복

---

### 1.6 회원 탈퇴

|항목|내용|
|---|---|
|**URL**|`DELETE /users/{user_id}`|
|**인증**|필요 (본인만 가능)|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|user_id|Long|사용자 ID|

**Request Body** (선택)

```json
{
  "password": "password123"
}
```

**Response**

- `204 No Content`

**Error Response**

- `401 Unauthorized` - 로그인 필요 또는 비밀번호 불일치
- `403 Forbidden` - 본인이 아님

---

## Sprint 2: 게시글

### 2.1 전체 게시글 조회 #수정

페이징, 태그 필터링, 블로그 필터링을 지원합니다.

| 항목      | 내용           |
| ------- | ------------ |
| **URL** | `GET /posts` |
| **인증**  | 불필요          |

**Query Parameters**

| 파라미터    | 타입              | 설명                               |
| ------- | --------------- | -------------------------------- |
| page    | int             | 페이지 번호 (0부터)                     |
| size    | int             | 페이지당 개수                          |
| blogId  | long            | 블로그 필터                           |
| search  | string          | 검색 기준 (title, author 등)          |
| tag     | string (repeat) | 태그 목록                            |
| tagMode | Sting           | `OR` / `AND` (기본: AND)           |
| sort    | String          | view, like, createdAt, updatedAt |
| order   | Stting          | asc / desc                       |

**Request Example**

```
GET /posts?page=0&size=10&blogId=1
           &search=title
           &tag=c&tag=spring&tag=python
           &tagMode=OR
           &sort=view
           &order=desc
```


**Response**

- `200 OK`

```json
{
  "content": [
    {
      "postId": 1,
      "title": "Spring Boot 시작하기",
      "summary": "Spring Boot의 기초를 알아봅니다..."
      "author": {
        "userId": 1,
        "nickname": "홍길동",
        "profileImage": "<https://example.com/profile.jpg>"
      },
      "tags": ["Spring", "Java", "Backend"],
      "likeCount": 15,
      "commentCount": 3,
      "createdAt": "2024-12-23T10:00:00"
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

### 2.2 게시글 상세 조회

댓글 목록이 함께 조회됩니다.

|항목|내용|
|---|---|
|**URL**|`GET /posts/{post_id}`|
|**인증**|불필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|

**Response** #수정 

- `200 OK`

```json
{
  "postId": 1,
  "title": "Spring Boot 시작하기",
  "content": "## 소개\nSpring Boot는...",
  "thumbnailImage": "https://example.com/thumb.jpg",
  "author": {
    "userId": 1,
    "nickname": "홍길동",
    "profileImage": "https://example.com/profile.jpg"
  },
  "tags": ["Spring", "Java", "Backend"],
  "likeCount": 15,
  "isLiked": false,
  "comments": [
    {
      "commentId": 1,
      "content": "좋은 글이네요!",
      "author": {
        "userId": 2,
        "nickname": "김철수",
        "profileImage": "https://example.com/profile2.jpg"
      },
      "createdAt": "2024-12-23T11:00:00",
      "updatedAt": null,
      "replies": [
        {
          "replyId": 10,
          "content": "감사합니다!",
          "author": {
            "userId": 1,
            "nickname": "홍길동",
            "profileImage": "https://example.com/profile.jpg"
          },
          "createdAt": "2024-12-23T11:05:00",
          "updatedAt": null
        }
      ]
    }
  ],
  "createdAt": "2024-12-23T10:00:00",
  "updatedAt": null
}
```

**Error Response**

- `404 Not Found` - 게시글을 찾을 수 없음

---

### 2.3 게시글 작성

태그는 서비스 단에서 자동 생성/관리됩니다.

|항목|내용|
|---|---|
|**URL**|`POST /posts`|
|**인증**|필요|

**Request Body**

```json
{
  "title": "Spring Boot 시작하기",
  "content": "## 소개\\nSpring Boot는...",
  "thumbnailImage": "<https://example.com/thumb.jpg>",
  "tags": ["Spring", "Java", "Backend"]
}
```

**Response**

- `201 Created`

```json
{
  "postId": 1,
  "title": "Spring Boot 시작하기",
  "content": "## 소개\\nSpring Boot는...",
  "thumbnailImage": "<https://example.com/thumb.jpg>",
  "author": {
    "userId": 1,
    "nickname": "홍길동",
    "profileImage": "<https://example.com/profile.jpg>"
  },
  "tags": ["Spring", "Java", "Backend"],
  "createdAt": "2024-12-23T10:00:00"
}
```

**Error Response**

- `400 Bad Request` - 유효성 검증 실패
- `401 Unauthorized` - 로그인 필요

---

### 2.4 게시글 수정

|항목|내용|
|---|---|
|**URL**|`PUT /posts/{post_id}`|
|**인증**|필요 (작성자만 가능)|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|

**Request Body**

```json
{
  "title": "수정된 제목",
  "content": "수정된 내용입니다.",
  "thumbnailImage": "<https://example.com/new-thumb.jpg>",
  "tags": ["Spring", "수정됨"]
}
```

**Response**

- `200 OK`

```json
{
  "postId": 1,
  "title": "수정된 제목",
  "content": "수정된 내용입니다.",
  "thumbnailImage": "<https://example.com/new-thumb.jpg>",
  "author": {
    "userId": 1,
    "nickname": "홍길동",
    "profileImage": "<https://example.com/profile.jpg>"
  },
  "tags": ["Spring", "수정됨"],
  "updatedAt": "2024-12-23T12:00:00"
}
```

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `403 Forbidden` - 작성자가 아님
- `404 Not Found` - 게시글을 찾을 수 없음

---

### 2.5 게시글 삭제

|항목|내용|
|---|---|
|**URL**|`DELETE /posts/{post_id}`|
|**인증**|필요 (작성자만 가능)|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|

**Response**

- `204 No Content`

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `403 Forbidden` - 작성자가 아님
- `404 Not Found` - 게시글을 찾을 수 없음

---

## Sprint 3: 댓글 & 좋아요

### 3.1 댓글 작성

|항목|내용|
|---|---|
|**URL**|`POST /posts/{post_id}/comments`|
|**인증**|필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|

**Request Body**

```json
{
  "content": "좋은 글이네요!",
  "parentCommentId": null
}
```

> parentCommentId: 대댓글인 경우 부모 댓글 ID 입력

**Response**

- `201 Created`

```json
{
  "commentId": 1,
  "content": "좋은 글이네요!",
  "author": {
    "userId": 2,
    "nickname": "김철수",
    "profileImage": "<https://example.com/profile2.jpg>"
  },
  "parentCommentId": null,
  "createdAt": "2024-12-23T11:00:00"
}
```

**Error Response**

- `400 Bad Request` - 유효성 검증 실패
- `401 Unauthorized` - 로그인 필요
- `404 Not Found` - 게시글 또는 부모 댓글을 찾을 수 없음

---

### 3.2 댓글 수정

|항목|내용|
|---|---|
|**URL**|`PUT /posts/{post_id}/comments/{comment_id}`|
|**인증**|필요 (작성자만 가능)|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|
|comment_id|Long|댓글 ID|

**Request Body**

```json
{
  "content": "수정된 댓글입니다."
}
```

**Response**

- `200 OK`

```json
{
  "commentId": 1,
  "content": "수정된 댓글입니다.",
  "author": {
    "userId": 2,
    "nickname": "김철수",
    "profileImage": "<https://example.com/profile2.jpg>"
  },
  "updatedAt": "2024-12-23T12:00:00"
}
```

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `403 Forbidden` - 작성자가 아님
- `404 Not Found` - 댓글을 찾을 수 없음

---

### 3.3 댓글 삭제

|항목|내용|
|---|---|
|**URL**|`DELETE /posts/{post_id}/comments/{comment_id}`|
|**인증**|필요 (작성자만 가능)|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|
|comment_id|Long|댓글 ID|

**Response**

- `200 OK`

```json
{
  "success": true,
  "message": "댓글 삭제 성공",
  "data": null
}
```

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `403 Forbidden` - 작성자가 아님
- `404 Not Found` - 댓글을 찾을 수 없음

---

### 3.4 좋아요

|항목|내용|
|---|---|
|**URL**|`POST /posts/{post_id}/like`|
|**인증**|필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|

**Response**

- `201 Created`

```json
{
  "postId": 1,
  "likeCount": 16,
  "message": "좋아요 완료"
}
```

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `404 Not Found` - 게시글을 찾을 수 없음
- `409 Conflict` - 이미 좋아요한 게시글

---

### 3.5 좋아요 취소

|항목|내용|
|---|---|
|**URL**|`DELETE /posts/{post_id}/like`|
|**인증**|필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|

**Response**

- `200 OK`

```json
{
  "postId": 1,
  "likeCount": 15,
  "message": "좋아요 취소 완료"
}
```

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `404 Not Found` - 게시글을 찾을 수 없음 또는 좋아요 기록 없음

---
## 3.6 답글(대댓글) 작성 #추가

| 항목      | 내용                                                    |
| ------- | ----------------------------------------------------- |
| **URL** | `POST /posts/{post_id}/comments/{comment_id}/replies` |
| **인증**  | 필요                                                    |

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|
|comment_id|Long|부모 댓글 ID(최상위 댓글)|

**Request Body**

```json
{
  "content": "감사합니다!"
}
```

**Response**

- `201 Created`
```json
{
  "replyId": 10,
  "content": "감사합니다!",
  "author": {
    "userId": 1,
    "nickname": "홍길동",
    "profileImage": "https://example.com/profile.jpg"
  },
  "parentCommentId": 1,
  "createdAt": "2024-12-23T11:05:00"
}
```

**Error Response**

- `400 Bad Request`
    - 유효성 검증 실패 (content 비어있음 등)
    - 답글 depth 제한 위반 (부모 댓글이 답글인 경우 등)
- `401 Unauthorized` - 로그인 필요
- `404 Not Found`
    - 게시글을 찾을 수 없음
    - 부모 댓글을 찾을 수 없음
    - 부모 댓글이 해당 게시글의 댓글이 아님

---

## 3.7 답글(대댓글) 수정 #추가

|항목|내용|
|---|---|
|**URL**|`PUT /posts/{post_id}/comments/{comment_id}/replies/{reply_id}`|
|**인증**|필요 (작성자만 가능)|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|
|comment_id|Long|부모 댓글 ID|
|reply_id|Long|답글 ID|

**Request Body**
```json
{
  "content": "수정된 답글입니다."
}
```

**Response**

- `200 OK`
```json
{
  "replyId": 10,
  "content": "수정된 답글입니다.",
  "author": {
    "userId": 1,
    "nickname": "홍길동",
    "profileImage": "https://example.com/profile.jpg"
  },
  "parentCommentId": 1,
  "updatedAt": "2024-12-23T12:10:00"
}
```

**Error Response**

- `400 Bad Request` - 유효성 검증 실패 (content 비어있음 등)
- `401 Unauthorized` - 로그인 필요
- `403 Forbidden` - 작성자가 아님
- `404 Not Found`
    - 답글을 찾을 수 없음
    - 부모 댓글을 찾을 수 없음
    - 게시글을 찾을 수 없음
    - reply_id가 해당 comment_id의 답글이 아님 (또는 post 불일치

---

## 3.8 답글(대댓글) 삭제 #추가

|항목|내용|
|---|---|
|**URL**|`DELETE /posts/{post_id}/comments/{comment_id}/replies/{reply_id}`|
|**인증**|필요 (작성자만 가능)|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|post_id|Long|게시글 ID|
|comment_id|Long|부모 댓글 ID|
|reply_id|Long|답글 ID|

**Response**

- `200 OK`

```json
{
  "success": true,
  "message": "답글 삭제 성공",
  "data": null
}
```

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `403 Forbidden` - 작성자가 아님
- `404 Not Found`
    - 답글을 찾을 수 없음
    - 부모 댓글을 찾을 수 없음
    - 게시글을 찾을 수 없음
    - reply_id가 해당 comment_id의 답글이 아님 (또는 post 불일치)


> [!NOTE] 공통 제약/검증
> - 답글은 **1-depth만 허용**: `comment_id`는 반드시 **최상위 댓글**이어야 함
> - `{comment_id}`는 반드시 `{post_id}`의 댓글이어야 함
> - `{reply_id}`는 반드시 `{comment_id}`의 답글이어야 함


---

## Sprint 4: 팔로우

### 4.1 팔로워 목록 조회

나를 팔로우하는 사용자 목록을 조회합니다.

|항목|내용|
|---|---|
|**URL**|`GET /users/{user_id}/followers`|
|**인증**|불필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|user_id|Long|사용자 ID|

**Query Parameters**

|파라미터|타입|필수|기본값|설명|
|---|---|---|---|---|
|page|Integer|X|0|페이지 번호|
|size|Integer|X|20|페이지당 개수|

**Response**

- `200 OK`

```json
{
  "content": [
    {
      "userId": 2,
      "nickname": "김철수",
      "profileImage": "<https://example.com/profile2.jpg>",
      "bio": "백엔드 개발자입니다.",
      "isFollowing": true
    },
    {
      "userId": 3,
      "nickname": "이영희",
      "profileImage": "<https://example.com/profile3.jpg>",
      "bio": "프론트엔드 개발자입니다.",
      "isFollowing": false
    }
  ],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

**Error Response**

- `404 Not Found` - 사용자를 찾을 수 없음

---

### 4.2 팔로잉 목록 조회

내가 팔로우하는 사용자 목록을 조회합니다.

|항목|내용|
|---|---|
|**URL**|`GET /users/{user_id}/followings`|
|**인증**|불필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|user_id|Long|사용자 ID|

**Query Parameters**

|파라미터|타입|필수|기본값|설명|
|---|---|---|---|---|
|page|Integer|X|0|페이지 번호|
|size|Integer|X|20|페이지당 개수|

**Response**

- `200 OK`

```json
{
  "content": [
    {
      "userId": 4,
      "nickname": "박지민",
      "profileImage": "<https://example.com/profile4.jpg>",
      "bio": "풀스택 개발자입니다.",
      "isFollowing": true
    }
  ],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2
  }
}
```

**Error Response**

- `404 Not Found` - 사용자를 찾을 수 없음

---

### 4.3 팔로우

|항목|내용|
|---|---|
|**URL**|`POST /users/{user_id}/follow`|
|**인증**|필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|user_id|Long|팔로우할 사용자 ID|

**Response**

- `201 Created`

```json
{
  "followingId": 4,
  "followingNickname": "박지민",
  "message": "팔로우 완료"
}
```

**Error Response**

- `400 Bad Request` - 자기 자신을 팔로우할 수 없음
- `401 Unauthorized` - 로그인 필요
- `404 Not Found` - 사용자를 찾을 수 없음
- `409 Conflict` - 이미 팔로우 중

---

### 4.4 언팔로우

|항목|내용|
|---|---|
|**URL**|`DELETE /users/{user_id}/follow`|
|**인증**|필요|

**Path Parameters**

|파라미터|타입|설명|
|---|---|---|
|user_id|Long|언팔로우할 사용자 ID|

**Response**

- `200 OK`

```json
{
  "unfollowedId": 4,
  "unfollowedNickname": "박지민",
  "message": "언팔로우 완료"
}
```

**Error Response**

- `401 Unauthorized` - 로그인 필요
- `404 Not Found` - 사용자를 찾을 수 없음 또는 팔로우 관계 없음