package com.example.hyejulog.controller;

import com.example.hyejulog.domain.RefreshToken;
import com.example.hyejulog.domain.Role;
import com.example.hyejulog.domain.User;
import com.example.hyejulog.dto.UserLoginDto;
import com.example.hyejulog.dto.UserLoginResponseDto;
import com.example.hyejulog.jwt.util.JwtTokenizer;
import com.example.hyejulog.service.RefreshTokenService;
import com.example.hyejulog.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final RefreshTokenService refreshTokenService;

    // 회원가입 처리
    @PostMapping("/api/users/userreg")
    public ResponseEntity<?> userReg(@Valid @RequestBody User user,
                          BindingResult bindingResult) {
        // @RequestBody는 JSON -> User 객체로 변환

        if (bindingResult.hasErrors()) {
            // 유효성 검사 오류가 있을 경우, 오류 메시지를 JSON 형식으로 클라이언트에 반환
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("message", "이미 사용 중인 아이디입니다."));

        }

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("message", "이미 사용 중인 이메일입니다."));
        }

        userService.registerUser(user);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @GetMapping("/api/users/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam("username") String username) {
        boolean exists = userService.findByUsername(username).isPresent();

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/users/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
        boolean exists = userService.findByEmail(email).isPresent();

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid UserLoginDto userLoginDto,
                                BindingResult bindingResult, HttpServletResponse response) {
        // username, password가 null이거나 형식이 맞지 않을 때
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);  // 상태 코드 400 BAD REQUEST로 응답
        }

        // username과 password 값을 잘 받아왔다면 서비스에 가입한 사용자 인지 확인
        Optional<User> optionalUser = userService.findByUsername(userLoginDto.getUsername());

        if (optionalUser.isEmpty()) {
            // 사용자를 찾을 수 없을 때
            return new ResponseEntity("사용자를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get(); // Optional에서 User 객체 꺼내기

        // 요청 정보에서 얻어온 비밀번호와 서비스가 갖고 있는 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            // 비밀번호가 일치하지 않을 때
            return new ResponseEntity("비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
        }
        // 여기까지 왔다는 것은 유저도 있고, 비밀번호도 맞다는 뜻
        // 롤 객체를 꺼내서 롤의 이름만 리스트로 얻어오기
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        // 토큰 발급
        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getEmail(), user.getName(), user.getUsername(), roles);
        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getEmail(), user.getName(), user.getUsername(), roles);

        // 리프레시 토큰을 디비에 저장
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setValue(refreshToken);
        refreshTokenEntity.setUserId(user.getId());

        refreshTokenService.addRefreshToken(refreshTokenEntity);

        // 응답으로 보낼 값들을 준비
        UserLoginResponseDto loginResponseDto = UserLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .build();

        // 액세스 토큰을 쿠키에 추가
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);  // 보안 (쿠키 값을 자바스크립트에서 접근할 수 없음)
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000)); // 30분. 쿠키의 유지시간 단위는 초, JWT의 시간단위는 밀리세컨드

        // 리프레시 토큰을 쿠키에 추가
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.REFRESH_TOKEN_EXPIRE_COUNT / 1000)); // 7일

        // 응답에 쿠키 추가
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return new ResponseEntity(loginResponseDto, HttpStatus.OK); // 상태 코드 200 OK로 응답
    }

    @PostMapping("/refreshToken")
    public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 리프레시 토큰을 통해 새로운 액세스 토큰을 발급

        // 요청이 들어오면
        // 1. 쿠키로부터 리프레시 토큰을 얻어온다.
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;  // 쿠키 찾았으면 더 이상 반복문 수행할 필요 없으니 빠져나감
                }
            }
        }

        // 2-1. 리프레시 토큰이 없으면 -> 오류로 응답
        if (refreshToken == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);  // 상태 코드 400 BAD REQUEST로 응답
        }

        // 2-2. 리프레시 토큰이 있으면 -> 토큰으로부터 정보를 얻어온다
        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
        Long userId = Long.valueOf((Integer) claims.get("userId"));

        User user = userService.getUser(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾지 못했습니다."));  // 존재하지않으면 예외 발생

        // 3. 액세스 토큰 생성
        List roles = (List) claims.get("roles");
        String accessToken = jwtTokenizer.createAccessToken(userId, user.getEmail(), user.getName(), user.getUsername(), roles);

        // 4. 쿠키를 생성해서 response로 보내고
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000));

        response.addCookie(accessTokenCookie);

        // 5. 적절한 응답 결과(ResponseEntity)를 생성해서 응답한다
        UserLoginResponseDto responseDto = UserLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .name(user.getName())
                .userId(user.getId())
                .build();

        return new ResponseEntity(responseDto, HttpStatus.OK);  // 상태 코드 200 OK로 응답
    }

}
