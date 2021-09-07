package security.ttaallkk.controller.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.exception.ExpiredJwtException;
import security.ttaallkk.exception.InvalidRefreshTokenException;
import security.ttaallkk.exception.PasswordNotMatchException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@RequiredArgsConstructor
@Log4j2
public class ExceptionController {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity badCredentials(Exception e) {
        Response response = Response.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("로그인 실패")
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity invalidRefreshToken(Exception e) {
        Response response = Response.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity ExpiredJwtException(Exception e) {
        Response response = Response.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("만료된 토큰입니다. 토큰을 갱신하세요")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity MethodArgumentNotValidException(Exception e) {
        Response response = Response.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("이메일 또는 비밀번호의 규격이 잘못된 요청입니다")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity PasswordNotMatchException(Exception e) {
        Response response = Response.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("비밀번호가 틀렸습니다. 비밀번호를 다시 확인하세요")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
