-- 테스트용 사용자 생성
INSERT INTO users (email, password, nickname, created_at) VALUES ('test@test.com', 'password123', '테스트유저', NOW());

-- 테스트용 블로그 생성 (Blog가 User FK를 가지므로 양방향 관계 자동 설정)
INSERT INTO blogs (user_id, title, created_at) VALUES (1, '테스트 블로그', NOW());
