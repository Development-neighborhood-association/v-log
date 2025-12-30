# CLAUDE.md

## 빌드 및 실행

```bash
./gradlew build          # 빌드
./gradlew bootRun        # 실행
./gradlew test           # 테스트
docker-compose up -d     # MySQL (port 13306)
```

## 기술 스택

Spring Boot 3.5.9 / Java 21 / JPA + QueryDSL + MySQL / Spring Security (세션 기반)

## 패키지 구조

```
com.likelion.vlog
├── config/              # ProjectSecurityConfig
├── controller/          # Auth, Post, Comment, Like, Follow, User, Tag
├── service/             # 비즈니스 로직
├── repository/          # JPA + QueryDSL Custom Repository
├── entity/              # User, Blog, Post, Comment, Tag, TagMap, Like, Follow
├── dto/                 # auth/, posts/, comments/, like/, follows/, users/, tags/, common/
└── exception/           # NotFoundException, ForbiddenException, DuplicateException
```

## API 엔드포인트

### 인증 (`/api/v1/auth`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/signup` | 회원가입 | X |
| POST | `/login` | 로그인 | X |
| POST | `/logout` | 로그아웃 | O |

### 게시글 (`/api/v1/posts`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 목록 조회 (QueryDSL 동적 쿼리) | X |
| GET | `/{postId}` | 상세 조회 (댓글 포함) | X |
| POST | `/` | 작성 | O |
| PUT | `/{postId}` | 수정 | O (작성자) |
| DELETE | `/{postId}` | 삭제 | O (작성자) |

### 댓글 (`/api/v1/posts/{postId}/comments`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 목록 조회 (대댓글 포함) | X |
| POST | `/` | 댓글 작성 | O |
| PUT | `/{commentId}` | 댓글 수정 | O (작성자) |
| DELETE | `/{commentId}` | 댓글 삭제 | O (작성자) |
| POST | `/{commentId}/replies` | 답글 작성 | O |
| PUT | `/{commentId}/replies/{replyId}` | 답글 수정 | O (작성자) |
| DELETE | `/{commentId}/replies/{replyId}` | 답글 삭제 | O (작성자) |

### 좋아요 (`/api/v1/posts/{postId}/like`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 좋아요 정보 조회 | O |
| POST | `/` | 좋아요 추가 | O |
| DELETE | `/` | 좋아요 취소 | O |

### 팔로우 (`/api/v1/users/{userId}/follows`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/` | 팔로우 | O |
| DELETE | `/` | 언팔로우 | O |

### 사용자 (`/api/v1/users`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/{userId}` | 조회 | X |
| PUT | `/{userId}` | 수정 | O |
| DELETE | `/{userId}` | 탈퇴 | O |

### 태그 (`/api/v1/tags`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/{title}` | 태그명으로 조회 | X |

## Entity 관계

```
User (1) ── Blog (1) ── Post (*) ── TagMap (*) ── Tag (*)
  │                       │
  │                       ├── Comment (*) [self-reference]
  │                       └── Like (*) ── User (*)
  │
  └── Follow (*) [self-reference]
```

## 코딩 컨벤션

### Entity
- `BaseEntity` 상속 (createdAt, updatedAt)
- `@Setter` 금지, 정적 팩토리 메서드 사용

```java
Post post = Post.of(title, content, blog);
Comment comment = Comment.of(user, post, content);
Comment reply = Comment.ofReply(user, post, parent, content);
```

### Service
- 클래스: `@Transactional(readOnly = true)`
- 쓰기 메서드만: `@Transactional`
- 커스텀 예외: `NotFoundException`, `ForbiddenException`, `DuplicateException`

### Controller
- 인증 사용자: `@AuthenticationPrincipal UserDetails`
- `userDetails.getUsername()` = email

### API 응답
```java
return ResponseEntity.ok(ApiResponse.success("메시지", data));           // 성공
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("생성", data)); // 201
return ResponseEntity.noContent().build();                              // 204
```

### DTO 네이밍
- Request: `{Action}{HttpMethod}Request` (예: `CommentCreatePostRequest`)
- Response: `{Resource}{HttpMethod}Response` (예: `PostGetResponse`)
- 정적 메서드: `from(Entity)` 타입 변환, `of(값들)` 값 조립

## QueryDSL

**Custom Repository 구조**:
- `PostRepositoryCustom` 인터페이스
- `PostRepositoryImpl` 구현체
- `PostExpression` 조건 헬퍼

**동적 쿼리 지원**:
- keyword 검색, tag 필터, blogId 필터, 정렬, 페이징

## 구현 현황

### 완료
- 회원가입/로그인/로그아웃
- 게시글 CRUD (QueryDSL 동적 쿼리)
- 댓글/대댓글 CRUD
- 좋아요 추가/삭제/조회
- 팔로우/언팔로우
- 사용자 CRUD
- 해시태그

### 미구현
- 팔로워/팔로잉 목록 조회 API

## TODO

### Critical
- [ ] AuthService/UserService/LikeService/FollowService: `IllegalArgumentException` → 커스텀 예외
- [ ] UserController: 권한 검증 추가 (본인만 수정/삭제)

### Enhancement
- [ ] PostGetResponse에 likeCount, isLiked 필드 추가
- [ ] CORS 설정 (프론트엔드 연결 시)
- [ ] Hibernate DDL: 프로덕션 시 `validate`로 변경
