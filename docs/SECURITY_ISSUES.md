# Security Issues Report

> 작성일: 2025-12-30
> 상태: 긴급 수정 필요

---

## CRITICAL - 즉시 수정 필요

### 1. UserController 권한 검증 부재

**위치**: `src/main/java/com/likelion/vlog/controller/UserController.java`

**문제점**:
- 사용자 수정/삭제 API에 권한 검증이 없음
- **누구나 다른 사용자의 정보를 수정/삭제할 수 있음**

**현재 코드**:
```java
@PutMapping("/{userId}")
public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId,
                                                @RequestBody UserUpdateRequest request) {
    // 권한 검증 없이 바로 수정!
    return ResponseEntity.ok(userService.updateUser(userId, request));
}
```

**수정 방안**:
```java
@PutMapping("/{userId}")
public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId,
                                                @RequestBody UserUpdateRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
    // 본인 확인 후 수정
    return ResponseEntity.ok(userService.updateUser(userId, request, userDetails.getUsername()));
}
```

**위험도**: CRITICAL
**영향 범위**: 전체 사용자 데이터

---

## HIGH - 개선 필요

### 2. 예외 처리 표준화 미흡

**문제점**:
서비스별로 다른 예외 클래스 사용 → 일관되지 않은 에러 응답

| Service | 현재 사용 | 권장 |
|---------|----------|------|
| AuthService | `IllegalArgumentException` | `DuplicateException` |
| UserService | `IllegalArgumentException` | `NotFoundException`, `ForbiddenException` |
| LikeService | `IllegalArgumentException`, `IllegalStateException` | `NotFoundException`, `DuplicateException` |
| FollowService | `IllegalArgumentException` | `ForbiddenException` |

**수정 예시 (AuthService)**:
```java
// Before
if (userRepository.existsByEmail(email)) {
    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
}

// After
if (userRepository.existsByEmail(email)) {
    throw new DuplicateException("이미 존재하는 이메일입니다: " + email);
}
```

---

## MEDIUM - 권장 사항

### 3. CSRF 비활성화

**위치**: `SecurityConfig.java`

```java
.csrf(csrf -> csrf.disable())
```

**현재 상태**: REST API이므로 허용됨
**권장**: JWT 또는 토큰 기반 인증 도입 시 재검토

### 4. 세션 설정 강화

**현재 상태**:
- 세션 타임아웃: 기본값 (30분)
- 동시 세션: 제한 없음

**권장**:
```java
.sessionManagement(session -> session
    .maximumSessions(1)
    .maxSessionsPreventsLogin(true)
)
```

---

## 수정 우선순위

1. **즉시**: UserController 권한 검증 추가
2. **이번 스프린트**: 예외 처리 표준화
3. **다음 스프린트**: 세션 관리 강화

---

## 체크리스트

- [ ] UserController에 @AuthenticationPrincipal 추가
- [ ] UserService에 본인 확인 로직 추가
- [ ] AuthService IllegalArgumentException → DuplicateException
- [ ] LikeService 예외 처리 표준화
- [ ] FollowService 자기팔로우 ForbiddenException으로 변경
