package security.ttaallkk.controller.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.common.Status;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.exception.CategoryNotFoundException;
import security.ttaallkk.exception.CommentIsAlreadyRemovedException;
import security.ttaallkk.exception.CommentNotFoundException;
import security.ttaallkk.exception.DisplayNameAlreadyExistException;
import security.ttaallkk.exception.EmailAlreadyExistException;
import security.ttaallkk.exception.ExpiredJwtException;
import security.ttaallkk.exception.InvalidRefreshTokenException;
import security.ttaallkk.exception.AuthenticatedFailureException;
import security.ttaallkk.exception.PasswordNotMatchException;
import security.ttaallkk.exception.PermissionDeniedException;
import security.ttaallkk.exception.PostNotFoundException;
import security.ttaallkk.exception.RefreshTokenGrantTypeException;
import security.ttaallkk.exception.TokenNotFoundException;
import security.ttaallkk.exception.UidNotFoundException;
import security.ttaallkk.exception.UidNotMatchedException;

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
                .message("로그인 실패.")
                .build();
        return ResponseEntity.badRequest().body(response);
    }
   
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity MethodArgumentNotValidException(Exception e) {
        Response response = Response.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("이메일 또는 비밀번호의 규격이 잘못된 요청입니다.")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ---------------------------------Custom Exception--------------------------------- //
    
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity CommentNotFoundException(Exception e) {
        Response response = Response.builder()
                .status(Status.COMMENT_NOT_FOUND)
                .message("존재하지 않는 댓글 정보 입니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(DisplayNameAlreadyExistException.class)
    public ResponseEntity DisplayNameAlreadyExistException(Exception e) {
        Response response = Response.builder()
                .status(Status.DISPLAYNAME_ALREADY_EXIST)
                .message("이미 존재하는 닉네임 입니다. 새로운 닉네임으로 시도해 보세요.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity EmailAlreadyExistException(Exception e) {
        Response response = Response.builder()
                .status(Status.EMAIL_ALREADY_EXIST)
                .message("이미 존재하는 이메일 입니다. 새로운 이메일로 시도해 보세요.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity ExpiredJwtException(Exception e) {
        Response response = Response.builder()
                .status(Status.TOKEN_EXPIRED)
                .message("만료된 토큰입니다. 토큰을 갱신하세요.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity invalidRefreshToken(Exception e) {
        Response response = Response.builder()
                .status(Status.TOKEN_INVLIED_REFRESHTOKEN)
                .message("유효하지 않은 리프래시 토큰입니다. 로그인을 통해 재인증 해주세요.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(AuthenticatedFailureException.class)
    public ResponseEntity AuthenticatedFailureException(Exception e) {
        Response response = Response.builder()
                .status(Status.AUTHENTICATED_FAILURE)
                .message("로그인 인증에 실패했습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity PasswordNotMatchException(Exception e) {
        Response response = Response.builder()
                .status(Status.PASSWORD_NOT_MATCHED)
                .message("비밀번호가 틀렸습니다. 비밀번호를 다시 확인하세요.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity PostNotFoundException(Exception e) {
        Response response = Response.builder()
                .status(Status.POST_NOT_FOUND)
                .message("존재하지 않는 게시글 정보 입니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RefreshTokenGrantTypeException.class)
    public ResponseEntity RefreshTokenGrantTypeException(Exception e) {
        Response response = Response.builder()
                .status(Status.TOKEN_GRANTTPYE_INVLIED)
                .message("잘못된 GrantType의 리프래시 토큰 입니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity TokenNotFoundException(Exception e) {
        Response response = Response.builder()
                .status(Status.TOKEN_NOT_FOUND)
                .message("존재하지 않는 토큰입니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(UidNotFoundException.class)
    public ResponseEntity UidNotFoundException(Exception e) {
        Response response = Response.builder()
                .status(Status.UID_NOT_FOUND)
                .message("존재하지 않는 사용자의 UID 입니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(UidNotMatchedException.class)
    public ResponseEntity UidNotMatchedException(Exception e) {
        Response response = Response.builder()
                .status(Status.UID_NOT_MATCHED)
                .message("요청 UID와 인증 UID가 일치하지 않습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity CategoryNotFoundException(Exception e) {
        Response response = Response.builder()
                .status(Status.CATEGORY_NOT_FOUND)
                .message("존재하지 않는 카테고리 입니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(CommentIsAlreadyRemovedException.class)
    public ResponseEntity CommentIsAlreadyRemovedException(Exception e) {
        Response response = Response.builder()
                .status(Status.COMMENT_ALREADY_REMOVED)
                .message("이미 삭제된 댓글은 수정할 수 없습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity PermissionDeniedException(Exception e) {
        Response response = Response.builder()
                .status(Status.PERMISSION_DENIED)
                .message("권한이 없습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
