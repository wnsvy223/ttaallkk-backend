package security.ttaallkk.service.file;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import security.ttaallkk.exception.FileUploadFailureException;
import security.ttaallkk.exception.InvalidFileMimeTypeException;
import security.ttaallkk.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;


@Service
public class FileStorageService {
    
    @Value("${upload.image.location.root}")
    private String rootPath;

    @Value("${upload.image.location.profile}")
    private String profileStoragePath;
    
    @Value("${upload.image.location.post}")
    private String postStoragePath;

    @PostConstruct
    void postConstruct() {
        makeDir(rootPath);
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
        makeDir(profileStoragePath); //프로필 이미지 업로드 디렉토리 생성
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
        makeDir(postStoragePath); //게시글 이미지 업로드 디렉토리 생성
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
     * 파일 삭제
     */
    public void removeFile(String pathName) {
        new File(pathName).delete();
    }
}
