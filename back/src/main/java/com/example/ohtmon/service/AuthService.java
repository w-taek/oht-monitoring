package com.example.ohtmon.service;

import com.example.ohtmon.config.JwtUtil;
import com.example.ohtmon.domain.User;
import com.example.ohtmon.dto.AuthDto;
import com.example.ohtmon.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        log.info("로그인 성공: username={}, role={}", user.getUsername(), user.getRole());

        return AuthDto.LoginResponse.builder()
                .token(token)
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
