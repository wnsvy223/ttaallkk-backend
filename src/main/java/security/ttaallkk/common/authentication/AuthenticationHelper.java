package security.ttaallkk.common.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelper {
    
    /**
     * 관리자 권한 체크
     * @return Boolean
     */
    public Boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
            return true;
        } else {
            return false;
        }
    }

     /**
     * 일반 or 익명 사용자 권한 체크
     * @return Boolean
     */
    public Boolean isNormalOrAnonymousUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER") || a.getAuthority().equals("ROLE_ANONYMOUS"))){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 이메일로 사용자 본인 권한 체크
     * @param email
     * @return Boolean
     */
    public Boolean isOwnerEmail(String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName().equals(email)){
            return true;
        } else {
            return false;
        }
    }
    
}
