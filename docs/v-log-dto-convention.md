# Welcome to the v-log wiki!

## DTO / ApiResponse / Controller 반환 컨벤션

---

## 1) DTO 폴더 구조

DTO는 **도메인(리소스) 단위**로 디렉터리를 구성한다.

```text
dto
├── auth
│   ├── LoginRequest.java
│   └── SignupRequest.java
├── users
├── posts
├── comments
├── follow
├── followers
├── followings
├── like
└── common
    └── ApiResponse.java
```

- `dto/common`에는 **공통 응답 래퍼, 공통 페이지/에러 모델 등**만 둔다.
- 개별 기능 DTO는 **반드시 해당 리소스 폴더 하위**에 둔다.

---

## 2) DTO 네이밍 컨벤션 (리소스 폴더 구조에 최적화)

현재 구조에서는  
“엔드포인트 + HTTP 메서드”를 클래스명에 모두 포함하기보다는,

- **리소스 폴더**로 엔드포인트의 상위 경로를 표현하고
- **클래스명**에서 행위 + 메서드 + Request/Response를 표현하는 방식이 가장 일관된다.

---

### 2.1 Request DTO

**형식**

```
{Action}{HttpMethod}Request
```

**예시**

- `LoginPostRequest` → `POST /auth/login`
- `SignupPostRequest` → `POST /auth/signup`
- `UserUpdatePutRequest` → `PUT /users/{id}`
- `CommentCreatePostRequest` → `POST /comments`

> 단, 현재처럼 `LoginRequest`, `SignupRequest`로 이미 굳어진 경우  
> **메서드가 명확하고 변형 가능성이 낮은 auth 범위에서는 메서드 생략을 허용**한다.  
> (단, users / posts 등 규모가 커지는 리소스에서는 메서드 포함을 권장)

---

### 2.2 Response DTO

**형식**

```
{Resource}{HttpMethod}Response
또는
{Action}{HttpMethod}Response
```

**예시**

- `UserGetResponse` → `GET /users/{id}`, `GET /users/me`
- `PostGetResponse`
- `LoginPostResponse` (필요 시)

---

## 3) DTO 생성 시 주석 규칙

DTO 생성 시 **클래스 상단에 한 줄 주석을 반드시 추가**한다.

**형식**

```java
/**
 * POST /auth/login 요청 객체
 */
```

**예시**

```java
/**
 * GET /users/{id} 응답 객체
 */
public class UserGetResponse {
    ...
}
```

---

## 4) 정적 팩토리 메서드 컨벤션 (`from` / `of`)

### 4.1 `from`

**의미**

> 입력(외부/다른 타입)으로부터 만들어진다

- 타입 변환 / 매핑이 중심일 때 사용한다.

**예시**

```java
public static UserGetResponse from(User user) { ... }
public static LoginPostResponse from(Token token) { ... }
```

---

### 4.2 `of`

**의미**

> 구성요소(값)들을 모아 만든다

- 값 조립 / 팩토리 역할이 명확할 때 사용한다.

**예시**

```java
public static UserGetResponse of(Long id, String nickname) { ... }
```

---

## 5) API 응답은 항상 ApiResponse<T>로 래핑한다

프로젝트의 모든 컨트롤러 응답은 아래 형태를 따른다.

### 5.1 반환 타입 규칙

- 성공/실패 포함하여 **항상 `ResponseEntity<ApiResponse<...>>` 형태로 반환**한다.
- 데이터가 없는 경우에도 `ApiResponse.success(message)` 형태로 반환한다.

**예시 (데이터 있음)**

```java
return ResponseEntity.ok(
    ApiResponse.success("유저 조회 성공", userGetResponse)
);
```

**예시 (데이터 없음)**

```java
return ResponseEntity.ok(
    ApiResponse.success("로그아웃 성공")
);
```

**예시 (에러 메시지)**

```java
return ResponseEntity.badRequest().body(
    ApiResponse.error("잘못된 요청입니다.")
);
```

### 5.2 컨트롤러 반환 예시

```java
ResponseEntity<ApiResponse<UserGetResponse>>
```

**권장 패턴**

- `ok() + ApiResponse.success(...)`
- 상태코드가 필요한 경우 `status(HttpStatus.X)` 사용
- 에러는 `ApiResponse.error(...)`로 통일

---

## 6) ApiResponse<T> 표준 구현 (공통)

`com.likelion.vlog.dto.common.ApiResponse`를 표준 응답 래퍼로 사용한다.

### 성공 응답

- `ApiResponse.success(message, data)`
- `ApiResponse.success(message)` (data = null)

### 실패 응답

- `ApiResponse.error(message)` (data = null)
