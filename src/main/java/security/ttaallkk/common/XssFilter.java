package security.ttaallkk.common;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
		if(request.getMethod().equals("GET") || request.getMethod().equals("POST")){
			logger.info("========XSS 필터 진입========");
			filterChain.doFilter(new XssRequestWrapper(request), response); 
		}else{
			filterChain.doFilter(request, response);
		}
    } 
}
