package com.example.hyejulog.jwt.exception;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // 인증되지 않은 사용자가 리소스에 접근할 때 사용되는 객체 !!
    // AuthenticationEntryPoint 인터페이스를 구현하여, 인증되지 않은 사용자가 보호된 리소스에 접근할 때의 동작을 정의
    // 시큐리티가 인증되지 않은 사용자가(인증해야만 사용할 수 있는) 리소스에 접근하려할때 인터페이스인 AuthenticationEntryPoint를 동작시킴

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 호출
        String exception = (String) request.getAttribute("exception"); // 예외 정보를 요청에서 추출

        if (isRestRequest(request)) {
            // 요청이 RESTful API 요청인지 확인
            handleRestResponse(request, response, exception); // RESTful 요청 처리
        } else {
            handlePageResponse(request, response, exception); // 페이지 요청 처리
        }
    }

    // RestController 와 Controller에서 요청했을 때 응답이 다르기 때문에 어떤 요청인지를 구분
    // RESTful인지 일반 요청인지 구분해서 다르게 동작하도록 구분
    private boolean isRestRequest(HttpServletRequest request) {
        String requestWithHeader = request.getHeader("X-Requested-With"); // 요청 헤더에서 "X-Requested-With" 값을 가져옴
        return "XMLHttpRequest".equals(requestWithHeader) || request.getRequestURI().startsWith("/api/"); // RESTful 요청인지 확인
    }

    // RESTful 요청 처리
    private void handleRestResponse(HttpServletRequest request, HttpServletResponse response, String exception) throws IOException {
        log.error("Rest Request - Commence Get Exception : {}", exception); // 예외 정보 로깅

        if (exception != null) {
            if (exception.equals(JwtExceptionCode.INVALID_TOKEN.getCode())) {
                log.error("entry point >> invalid token");
                setResponse(response, JwtExceptionCode.INVALID_TOKEN); // 유효하지 않은 토큰 예외 처리
            } else if (exception.equals(JwtExceptionCode.EXPIRED_TOKEN.getCode())) {
                log.error("entry point >> expired token");
                setResponse(response, JwtExceptionCode.EXPIRED_TOKEN); // 만료된 토큰 예외 처리
            } else if (exception.equals(JwtExceptionCode.UNSUPPORTED_TOKEN.getCode())) {
                log.error("entry point >> unsupported token");
                setResponse(response, JwtExceptionCode.UNSUPPORTED_TOKEN); // 지원되지 않는 토큰 예외 처리
            } else if (exception.equals(JwtExceptionCode.NOT_FOUND_TOKEN.getCode())) {
                log.error("entry point >> not found token");
                setResponse(response, JwtExceptionCode.NOT_FOUND_TOKEN); // 토큰을 찾을 수 없는 예외 처리
            } else {
                setResponse(response, JwtExceptionCode.UNKNOWN_ERROR); // 알 수 없는 오류 처리
            }
        } else {
            setResponse(response, JwtExceptionCode.UNKNOWN_ERROR); // 예외 정보가 없는 경우 알 수 없는 오류 처리
        }
    }

    // 페이지 요청 처리
    // 페이지로 요청이 들어왔을 때 인증되지 않은 사용자라면 무조건 /loginform으로 리다이렉트 시킴
    private void handlePageResponse(HttpServletRequest request, HttpServletResponse response, String exception) throws IOException {
        log.error("Page Request - Commence Get Exception : {}", exception); // 예외 정보 로깅
//        if (exception != null) {
//            // 추가적인 페이지 요청에 대한 예외 처리 로직을 여기에 추가할 수 있다
//        }
        response.sendRedirect("/loginform"); // 로그인 페이지로 리다이렉트
    }

    // 예외 응답 설정
    private void setResponse(HttpServletResponse response, JwtExceptionCode exceptionCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8"); // 응답의 Content-Type 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 응답 상태를 401(Unauthorized)로 설정

        HashMap<String, Object> errorInfo = new HashMap<>(); // 에러 정보 맵 생성
        errorInfo.put("message", exceptionCode.getMessage()); // 에러 메시지 추가
        errorInfo.put("code", exceptionCode.getCode()); // 에러 코드 추가
        Gson gson = new Gson(); // Gson 객체 생성
        String responseJson = gson.toJson(errorInfo); // 에러 정보를 JSON 형식으로 변환
        response.getWriter().print(responseJson); // 응답에 JSON 데이터 출력
    }
}
