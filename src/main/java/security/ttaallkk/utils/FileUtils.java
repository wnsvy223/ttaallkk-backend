package security.ttaallkk.utils;

import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    private static final Tika tika = new Tika();

    /**
     * Tika 라이브러리를 이용한 파일 mimeType 유효성 체크
     * @param inputStream
     * @return boolean
     */
    public static boolean validateFileMime(InputStream inputStream) {
        try {
            //허용된 이미지 확장자 화이트 리스트
            List<String> fileWhiteList = Arrays.asList("image/jpeg", "image/pjpeg", "image/png", "image/gif", "image/bmp", "image/x-windows-bmp");

            String mimeType = tika.detect(inputStream);

            boolean isValid = fileWhiteList.stream().anyMatch(notValidType -> notValidType.equalsIgnoreCase(mimeType));

            return isValid;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
