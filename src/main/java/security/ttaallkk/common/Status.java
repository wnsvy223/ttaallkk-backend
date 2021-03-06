package security.ttaallkk.common;

public class Status {
    public static final Integer TOKEN_EXPIRED = 2000; //토큰만료
    public static final Integer TOKEN_INVLIED_REFRESHTOKEN = 2001; //유효하지 않은 리프래시토큰 
    public static final Integer TOKEN_GRANTTPYE_INVLIED = 2002; //유효하지 않은 토큰GRANT TYPE
    public static final Integer TOKEN_NOT_FOUND = 2003; //토큰 없음
    public static final Integer COMMENT_NOT_FOUND = 2004; //댓글 없음
    public static final Integer DISPLAYNAME_ALREADY_EXIST = 2005; //닉네임 이미 존재함
    public static final Integer EMAIL_ALREADY_EXIST = 2006; //이메일 이미 존재함
    public static final Integer PASSWORD_NOT_MATCHED = 2007; //비밀번호 맞지않음
    public static final Integer POST_NOT_FOUND = 2008; //게시글 없음
    public static final Integer UID_NOT_FOUND = 2009; //UID 없음
    public static final Integer UID_NOT_MATCHED = 2010; //UID가 일치하지 않음
    public static final Integer CATEGORY_NOT_FOUND = 2011; //카테고리가 존재하지 않음
    public static final Integer AUTHENTICATED_FAILURE = 2012; //로그인 인증 실패
    public static final Integer COMMENT_ALREADY_REMOVED = 2013; //댓글 이미 삭제됨
    public static final Integer PERMISSION_DENIED = 2014; //권한 없음
    public static final Integer POST_ALREADY_REMOVED = 2015; //게시글 이미 삭제됨
    public static final Integer MEMBER_NOT_FOUND = 2016; //해당하는 사용자 존재하지 않음
    public static final Integer FRIEND_ALREADY_EXIST = 2017; //친구관계 이미 존재함
    public static final Integer FRIEND_NOT_ALLOW_SELF = 2018; //자기 자신 친구 추가 불가
    public static final Integer FRIEND_RELATION_NOT_FOUND = 2020; //친구 관계 존재하지 않음
    public static final Integer FILE_UPLOAD_FAILURE = 2021; //파일 업로드 실패
    public static final Integer INVALIDE_FILE_MIME = 2022; //잘못된 파일 형식
}
