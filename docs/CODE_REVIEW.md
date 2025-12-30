# Code Review Report

> 작성일: 2025-12-30
> 리뷰어: Claude Code
> 대상: v-log 프로젝트 전체

---

## 종합 점수표

| 영역 | 점수 | 상태 | 주요 이슈 |
|-----|------|------|----------|
| PostController/Service | 81/100 | 양호 | N+1 쿼리 |
| CommentController/Service | 87/100 | 양호 | - |
| AuthController/Service | 72/100 | 개선 필요 | 예외 처리 |
| **UserController/Service** | **55/100** | **긴급 수정** | **권한 검증 부재** |
| LikeController/Service | 68/100 | 개선 필요 | 예외 처리 |
| FollowController/Service | 88/100 | 양호 | - |

---

## 상세 리뷰

### 1. PostController/Service (81/100)

**장점**:
- 정적 팩토리 메서드 사용 (`Post.of()`)
- 권한 검증 로직 구현
- QueryDSL을 활용한 동적 쿼리

**개선 필요**:
```java
// PostService.java:80-86 - 잠재적 N+1 문제
List<CommentWithRepliesResponse> comments = commentRepository
    .findAllByPostWithChildren(post)  // fetch join 사용 중이나
    .stream()
    .map(CommentWithRepliesResponse::from)  // children 접근 시 추가 쿼리 가능
    .toList();
```

**권장 사항**:
- 태그 조회 시 fetch join 추가
- 댓글 조회 쿼리 최적화 확인

---

### 2. CommentController/Service (87/100)

**장점**:
- 대댓글 구조 잘 설계됨
- 부모-자식 관계 검증 로직 완비
- `ForbiddenException` 적절히 사용

**구조**:
```
Comment
├── parent (ManyToOne, nullable)
└── children (OneToMany, orphanRemoval=true)
```

**개선 필요**:
- `validateCommentIsNotReply()` 메서드 명칭이 혼란스러움
  - 실제로는 "대댓글이 아닌지 확인"하는 메서드

---

### 3. AuthController/Service (72/100)

**장점**:
- 세션 기반 인증 적절히 구현
- 비밀번호 암호화 (BCrypt)

**개선 필요**:

```java
// AuthService.java - IllegalArgumentException 사용
if (userRepository.existsByEmail(request.getEmail())) {
    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    // → throw new DuplicateException("...")으로 변경 필요
}
```

```java
// AuthService.java - 비밀번호 불일치
if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    // → 인증 관련 전용 예외 고려
}
```

---

### 4. UserController/Service (55/100) - CRITICAL

**심각한 문제**: 권한 검증 부재

```java
// UserController.java - 권한 검증 없음!
@PutMapping("/{userId}")
public ResponseEntity<UserResponse> updateUser(
    @PathVariable Long userId,
    @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(userService.updateUser(userId, request));
}
// 누구나 다른 사용자 정보 수정 가능!
```

**수정 필요**:
1. `@AuthenticationPrincipal UserDetails userDetails` 파라미터 추가
2. UserService에 본인 확인 로직 추가
3. `ForbiddenException` 활용

**예외 처리 문제**:
```java
// UserService.java
throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
// → throw new NotFoundException("...")
```

---

### 5. LikeController/Service (68/100)

**장점**:
- 좋아요 토글 로직 구현
- Post 엔티티에 like()/unlike() 메서드 구현

**개선 필요**:

```java
// LikeService.java - 다양한 예외 혼재
throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
// → NotFoundException, DuplicateException으로 통일
```

---

### 6. FollowController/Service (88/100)

**장점**:
- 팔로우/언팔로우 로직 명확
- 자기 자신 팔로우 방지

**개선 필요**:
```java
// FollowService.java
if (follower.getId().equals(following.getId())) {
    throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
    // → throw new ForbiddenException("...")으로 변경
}
```

---

## 공통 개선 사항

### 1. 예외 처리 표준화

모든 서비스에서 커스텀 예외만 사용:
- `NotFoundException` - 리소스 없음
- `ForbiddenException` - 권한 없음
- `DuplicateException` - 중복

### 2. 엔티티 표준화

```java
// User.java - BaseEntity 미상속, @Setter 사용 중
public class User {  // extends BaseEntity 누락
    @Setter  // 제거 필요
    private String nickname;
}
```

### 3. N+1 쿼리 최적화

Post 조회 시 연관 엔티티:
- Blog (LAZY) - 현재 필요 시마다 조회
- TagMapList (LAZY) - 태그 접근 시 N+1

```java
// 권장: fetch join 사용
@Query("SELECT p FROM Post p " +
       "LEFT JOIN FETCH p.blog b " +
       "LEFT JOIN FETCH p.tagMapList tm " +
       "LEFT JOIN FETCH tm.tag " +
       "WHERE p.id = :id")
Optional<Post> findByIdWithDetails(@Param("id") Long id);
```

---

## 테스트 커버리지

| 영역 | 테스트 수 | 상태 |
|-----|----------|------|
| Repository | 35+ | 양호 |
| Service | 30+ | 양호 |
| Controller | 20+ | 양호 |
| **총합** | **85+** | - |

---

## 우선순위별 액션 아이템

### 긴급 (이번 주)
1. [ ] UserController 권한 검증 추가
2. [ ] UserService 본인 확인 로직 추가

### 높음 (이번 스프린트)
3. [ ] 예외 처리 표준화 (모든 서비스)
4. [ ] User 엔티티 BaseEntity 상속

### 보통 (다음 스프린트)
5. [ ] N+1 쿼리 최적화
6. [ ] 테스트 코드 보강
