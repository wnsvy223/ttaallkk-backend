package security.ttaallkk.service.file;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import security.ttaallkk.dto.common.FileCommonDto;
import security.ttaallkk.exception.FileUploadFailureException;
import security.ttaallkk.exception.InvalidFileMimeTypeException;
import security.ttaallkk.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@Service
public class FileStorageService {
    
    @Value("${upload.image.location.root}")
    public String rootPath;

    @Value("${upload.image.location.profile}")
    public String profileStoragePath;
    
    @Value("${upload.image.location.post}")
    public String postStoragePath;

    @PostConstruct
    void postConstruct() {
        makeDir(rootPath); //루트 디렉토리 생성
        makeDir(profileStoragePath); //프로필 이미지 업로드 디렉토리 생성
        makeDir(postStoragePath); //게시글 이미지 업로드 디렉토리 생성
    }

    /**
     * 파일 저장용 디렉토리 생성
     * @param path
     */
    private void makeDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 프로필 이미지 파일 업로드
     * @param file
     * @param uid
     * @return fileName
     */
    public String storeProfileImage(MultipartFile file, String uid) {
        String fileName = StringUtils.cleanPath(uid + "_" + file.getOriginalFilename());
        File saveFile = new File(profileStoragePath, fileName);
        try {
            boolean isFileValid = FileUtils.validateFileMime(file.getInputStream()); //파일 확장자 mime 유효성 체크
            if (isFileValid) {
                file.transferTo(saveFile);
            } else {
                throw new InvalidFileMimeTypeException();
            }
        } catch(IOException e) {
            throw new FileUploadFailureException(e);
        }
        return fileName;
    }

    /**
     * 게시글 이미지 파일 업로드
     * @param file
     * @param postId
     * @return fileName
     */
    public String storePostImage(MultipartFile file, Long postId) {
        //TODO: 게시글 파일 업로드
        String fileName = StringUtils.cleanPath(postId + "_" + file.getOriginalFilename());
        return fileName;
    }

    /**
     * 파일 다운로드
     * @param fileName
     * @return Resource
     * @throws FileNotFoundException
     */
    public Resource loadFile(String fileName) throws FileNotFoundException {
        try {
            Path file = Paths.get(rootPath).toAbsolutePath().normalize().resolve(fileName).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not find file");
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not download file");
        }
    }

    /**
     * 컨탠츠 문자열값에 포함된 HTML 또는 마크다운 이미지 태그 추출하여 리스트 추가 후 반환
     * @param content
     * @return List<FileCommonDto>
     */
    public List<FileCommonDto> extractDataUrlFromMarkdown(String content) {
        Matcher matcher = Pattern.compile("(?i)&amp;lt;img.*?src=[\"|'](?<htmlImageUrl>.*?)[\"|']|!\\[.*?\\]\\((?<markdownImageUrl>.*?)\\)").matcher(content);
        List<FileCommonDto> list = new ArrayList<>();
        while(matcher.find()){
            String imagePath = matcher.group("htmlImageUrl"); //HTML 이미지 태그
            String markdownImagePath = matcher.group("markdownImageUrl"); //마크다운 이미지 태그
            if(imagePath != null){
                extractImageFromDataUrl(imagePath, list);
            }
            if(markdownImagePath != null){
                extractImageFromDataUrl(markdownImagePath, list);
            }
        }
        return list;
    }

    /**
     * HTML 또는 마크다운 문자열에서 base64 data url을 추출하여 이미지 파일 배열에 추가
     * @param dataUrl
     * @param list
     */
    public void extractImageFromDataUrl(String dataUrl, List<FileCommonDto> list) {
        String dataUrlRegex = "data:[^/]+/([^;]+);base64[^?]+";
        Matcher matchUrl = Pattern.compile(dataUrlRegex).matcher(dataUrl);
        if(matchUrl.find()){
            String base64 = dataUrl.substring(dataUrl.indexOf(",") + 1); //base64 문자열만 추출
            String uuid = RandomStringUtils.random(30, 32, 127, true, true);
            String fileName = uuid + ".jpg";
            FileCommonDto fileCommonDto = FileCommonDto.builder()
                .fileName(fileName)
                .fileBase64String(base64)
                .dataUrl(dataUrl)
                .build();
            list.add(fileCommonDto);
            createFileFromBase64(base64, fileName, postStoragePath);
        }
    }

    /**
     * Base64데이터를 이미지 파일로 변환
     * @param base64
     * @param filename
     * @param path
     */
    public void createFileFromBase64(String base64, String filename, String path) {
        byte decode[] = Base64.getMimeDecoder().decode(base64);
        FileOutputStream fileOutputStream;
        try{
            File file = new File(path + filename);
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(decode);
            fileOutputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 파일 접근 url 생성하여 반환
     * @param path
     * @param fileName
     * @return downloadUrl
     */
    public String getDownloadUrl(String path, String fileName) {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String scheme = httpServletRequest.isSecure() ? "https" : "http";

        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                                                .scheme(scheme)
                                                .path(path)
                                                .path(fileName)
                                                .toUriString();
        return downloadUrl;
    }

    /**
     * 파일 삭제
     */
    public void removeFile(String pathName) {
        new File(pathName).delete();
    }
}
