package com.likelion.vlog.controller;

import com.likelion.vlog.config.CustomUserDetails;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 스프린트 1 완성 전까지 사용하는 데모용 인증 컨트롤러
 * - SecurityContext에 Authentication 설정하여 Spring Security와 연동
 * - 세션에 SecurityContext 저장하여 요청 간 인증 상태 유지
 * - 스프린트 1 완성 후 삭제 예정
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthDemoController {

    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/demo-login")
    public ResponseEntity<?> demoLogin(
            @RequestParam Long userId,
            HttpServletRequest request,
            HttpServletResponse response) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // CustomUserDetails 생성 후 SecurityContext에 설정
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        // SecurityContext 생성 및 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 세션에 SecurityContext 저장 (요청 간 인증 상태 유지)
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "nickname", user.getNickname(),
                "message", "데모 로그인 성공"
        ));
    }

    @PostMapping("/demo-logout")
    public ResponseEntity<?> demoLogout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @GetMapping("/demo-me")
    public ResponseEntity<?> demoMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("message", "로그인 상태가 아닙니다."));
        }

        User user = userDetails.getUser();
        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));
    }
}