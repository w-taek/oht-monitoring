package com.example.ohtmon.config;

import com.example.ohtmon.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS preflight 요청 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        // V1.1: token이 없는 요청도 일단 통과 (MVP 호환)
        // 완전 차단 시 아래 블록의 return true → writeUnauthorized 로 변경
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // TODO: V1.1 완성 후 아래 주석 해제하여 인증 필수화
            // return writeUnauthorized(response, "인증 토큰이 필요합니다");
            return true;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return writeUnauthorized(response, "유효하지 않거나 만료된 토큰입니다");
        }

        // 요청 속성에 사용자 정보 저장
        request.setAttribute("username", jwtUtil.getUsername(token));
        request.setAttribute("role", jwtUtil.getRole(token));

        return true;
    }

    private boolean writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> body = ApiResponse.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }
}
