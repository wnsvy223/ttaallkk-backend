package security.ttaallkk.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
* Request로 들어오는 요청값에 대하여 XSS필터링 적용
*/
public class XssRequestWrapper extends HttpServletRequestWrapper{

    private byte[] body;

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            InputStream is = request.getInputStream();
            if (is != null) {
                StringBuffer stringBuffer = new StringBuffer();
                while(true) {
                    int data = is.read();
                    if (data < 0) break;
                    stringBuffer.append((char) data);
                }

                String result = cleanXss(stringBuffer.toString());
                body = result.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    // XSS필터링을 위한 escape 수행 함수
    private String cleanXss(String value) {
        value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
        value = value.replaceAll("'", "&#39;");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        //value = value.replaceAll("script", "");
        return value;
    }
}
