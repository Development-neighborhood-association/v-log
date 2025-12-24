# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 빌드 및 실행

```bash
./gradlew build        # 빌드
./gradlew bootRun      # 실행
./gradlew test         # 테스트
./gradlew clean build  # 클린 빌드

# Docker로 MySQL 실행
docker-compose up -d   # MySQL 컨테이너 시작 (port 13306)
```

## 기술 스택

- **Backend**: Spring Boot 3.5.9 / Java 21
- **Database**: Spring Data JPA + MySQL (port 13306)
- **Security**: Spring Security (세션 기반 인증)
- **Validation**: Jakarta Validation (@Valid, @NotBlank)
- **Build**: Gradle

## 패키지 구조

```
com.likelion.vlog
├── config/
│   ├── SecurityConfig.java           # Spring Security 설정 (세션 기반)
│   ├── CustomUserDetails.java        # UserDetails 구현체
│   └── CustomUserDetailsService.java
├── controller/
│   ├── PostController.java           # 게시글 CRUD API
│   └── AuthDemoController.java       # 데모 인증 (Sprint 1 완성 전까지 사용)
├── service/
│   └── PostService.java              # 게시글 비즈니스 로직
├── repository/
│   ├── PostRepository.java           # 게시글 (페이징/필터링 JPQL)
│   ├── TagRepository.java
│   ├── TagMapRepository.java         # @Modifying 벌크 삭제
│   ├── UserRepository.java
│   ├── BlogRepository.java
│   ├── LikeRepository.java           # countByPosts() 벌크 쿼리
│   └── CommentRepository.java        # countByPosts() 벌크 쿼리
├── dto/
│   ├── request/
│   │   ├── PostCreateRequest.java
│   │   └── PostUpdateRequest.java
│   └── response/
│       ├── PostResponse.java         # 상세 조회 (댓글 포함)
│       ├── PostListResponse.java     # 목록 조회 (summary 100자)
│       ├── CommentResponse.java
│       ├── AuthorResponse.java
│       └── PageResponse.java         # 제네릭 페이징 래퍼
├── exception/
│   ├── NotFoundException.java        # 404 - 정적 팩토리 메서드 제공
│   ├── ForbiddenException.java       # 403 - 권한 없음
│   └── GlobalExceptionHandler.java   # @RestControllerAdvice
└── entity/
    ├── BaseEntity.java               # createdAt/updatedAt (JPA Auditing)
    ├── User.java                     # extends BaseEntity
    ├── Blog.java                     # User FK 소유 (연관관계 주인)
    ├── Post.java                     # create(), update() 메서드
    ├── Comment.java                  # create(), createReply(), update()
    ├── Tag.java                      # create() 메서드
    ├── TagMap.java                   # create() 메서드
    ├── Like.java                     # create() 메서드
    └── Follow.java                   # create() 메서드
```

## API 엔드포인트

### 게시글 API (`/api/v1/posts`)

| Method | Endpoint | 설명 | 인증 | 쿼리 파라미터 |
|--------|----------|------|------|--------------|
| GET | `/api/v1/posts` | 목록 조회 | X | `page`, `size`, `tag`, `blogId` |
| GET | `/api/v1/posts/{id}` | 상세 조회 | X | - |
| POST | `/api/v1/posts` | 작성 | O | - |
| PUT | `/api/v1/posts/{id}` | 수정 | O (작성자만) | - |
| DELETE | `/api/v1/posts/{id}` | 삭제 | O (작성자만) | - |

### 데모 인증 API (`/api/v1/auth`) - Sprint 1 완성 전까지 사용

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/demo-login?userId=1` | SecurityContext에 인증 정보 저장 |
| POST | `/api/v1/auth/demo-logout` | 세션 무효화 |
| GET | `/api/v1/auth/demo-me` | 현재 로그인 상태 확인 |

## 주요 설계 패턴

### Entity 관계
```
User (1) ── (1) Blog (1) ── (*) Post (1) ── (*) TagMap (*) ── (1) Tag
   │                           │
   │                           ├── (*) Comment (self-referencing: 대댓글)
   │                           │
   │                           └── (*) Like
   │                                  │
   └───────────────────────────────────┘
```

### 정적 팩토리 메서드 (Entity)
```java
// 게시글
Post.create(title, content, blog)
post.update(title, content)

// 태그
Tag.create(title)
TagMap.create(post, tag)

// 댓글
Comment.create(user, post, content)
Comment.createReply(user, post, parent, content)
comment.update(content)

// 좋아요/팔로우
Like.create(user, post)
Follow.create(follower, following)
```

### 커스텀 예외 (정적 팩토리)
```java
NotFoundException.post(postId)      // "게시글을 찾을 수 없습니다. id=1"
NotFoundException.user(userId)      // "사용자를 찾을 수 없습니다. id=1"
NotFoundException.blog(userId)      // "블로그를 찾을 수 없습니다. userId=1"
ForbiddenException.postUpdate()     // "게시글 수정 권한이 없습니다."
ForbiddenException.postDelete()     // "게시글 삭제 권한이 없습니다."
```

### 전역 예외 핸들러 응답 형식
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "게시글을 찾을 수 없습니다. id=999",
  "timestamp": "2025-12-23T21:45:00"
}
```

### 트랜잭션 설정
- `@Transactional(readOnly = true)` - Service 클래스 기본값 (성능 최적화)
- `@Transactional` - 쓰기 메서드에만 별도 지정

### N+1 문제 해결
- 게시글 목록 조회 시 좋아요/댓글 수를 **벌크 쿼리**로 한번에 조회
- `LikeRepository.countByPosts()`, `CommentRepository.countByPosts()`

## 코딩 컨벤션

### Entity
- 모든 Entity는 `BaseEntity` 상속 - `createdAt`/`updatedAt` JPA Auditing
- `@Setter` 사용 금지 - 비즈니스 메서드로 상태 변경 (`post.update()`)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - JPA 프록시용
- `FetchType.LAZY` 전역 적용 - N+1 예방
- 정적 팩토리 메서드로 객체 생성 (`Post.create()`)

### Repository
- 벌크 연산 시 `@Modifying` + `@Query` 필수
- 여러 Entity의 집계는 벌크 쿼리 사용 (`countByPosts()`)

### Service
- 예외는 커스텀 예외 사용 (`NotFoundException`, `ForbiddenException`)
- `IllegalArgumentException` 사용 금지

### Controller
- `@AuthenticationPrincipal CustomUserDetails`로 인증 정보 획득
- 성공 응답: 200 OK, 201 Created, 204 No Content

## 문서

- `docs/API_SPEC.md` - API 명세서
- `docs/sequence-diagrams.puml` - PlantUML 시퀀스 다이어그램

### PlantUML 실행 방법
```bash
# VS Code: PlantUML 확장 설치 후 Alt+D (미리보기)
# CLI: brew install plantuml && plantuml docs/sequence-diagrams.puml
# 온라인: http://www.plantuml.com/plantuml/uml/
```

## 향후 작업 (TODO)

- [ ] Sprint 1: 회원가입/로그인 API (AuthDemoController 대체)
- [ ] Sprint 3: 대댓글 조회 (CommentResponse에 children 추가)
- [ ] 운영 환경 프로파일 분리 (`application-prod.yaml`)
- [ ] `ddl-auto: create` → `validate` 변경 (운영)

## 코드 리뷰 히스토리

### 2025-12-23 리팩토링 완료
**높음 우선순위 (완료)**
- TagMapRepository `@Modifying` 추가
- AuthDemoController SecurityContext 연동
- PostService N+1 문제 해결 (벌크 쿼리)

**중간 우선순위 (완료)**
- User-Blog 양방향 매핑 정리
- 커스텀 예외 생성 (NotFoundException, ForbiddenException)
- GlobalExceptionHandler 추가
- Entity @Setter 제거, 비즈니스 메서드 추가

**낮음 우선순위 (완료)**
- 패키지명 `entity.entity` → `entity` 변경
- Like, Follow, Comment 정적 팩토리 메서드 추가
- BaseEntity 추출 (JPA Auditing)
