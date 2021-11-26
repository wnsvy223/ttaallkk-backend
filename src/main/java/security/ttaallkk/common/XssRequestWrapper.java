package security.ttaallkk.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.nhncorp.lucy.security.xss.XssSaxFilter;
import lombok.extern.log4j.Log4j2;

/**
* Request body로 들어오는 json 요청값에 대하여 XSS필터링 적용
*/
@Log4j2
public class XssRequestWrapper extends HttpServletRequestWrapper{

    private byte[] body;

    public XssRequestWrapper(HttpServletRequest request) throws IOException{
        super(request);
        try {
            String inputLine;
            InputStream inputStream = request.getInputStream(); //HTTP 요청값을 InputStream을 통해 바이트 단위로 받음
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8")); //한글문자 그대로 받기위해 InputStreamReader를 사용하여 InputStream을을 UTF-8로 인코딩하여 버퍼에 저장
            StringBuffer stringBuffer = new StringBuffer();
            while ((inputLine = bufferedReader.readLine()) != null) { 
                stringBuffer.append(inputLine); //BufferedReader를 통해 버퍼를 순회하며 StringBuffer에 append하여 body데이터 가공 
            }
            bufferedReader.close();
            String result = cleanXss(stringBuffer.toString()); //가공된 StringBuffer값에서 XSS 필터링 처리
            body = result.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
 
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = cleanXss(values[i]);
        }
 
        return encodedValues;
    }
 
    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return cleanXss(value);
    }
 
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return cleanXss(value);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.body);
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public void setReadListener(ReadListener listener) {
                // TODO Auto-generated method stub
            }
        };
    }

    // XSS필터링 처리 함수 (feat. Naver Lucy Filter)
    private String cleanXss(String value) {
        if(value != null) {
            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
            value = value.replaceAll("'", "&#39;");
            value = value.replaceAll("&", "&amp;");
            value = value.replaceAll("eval\\((.*)\\)", "");
            value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
            
            XssSaxFilter lucyFilter = XssSaxFilter.getInstance("lucy-xss-sax.xml", true);
            value = lucyFilter.doFilter(value);
        }
        return value;
    }
}
