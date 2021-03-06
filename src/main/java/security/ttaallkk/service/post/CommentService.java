package security.ttaallkk.service.post;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import security.ttaallkk.common.authentication.AuthenticationHelper;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.notification.NotificationType;
import security.ttaallkk.domain.post.Comment;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.dto.querydsl.CommentCommonDto;
import security.ttaallkk.dto.request.CommentCreateDto;
import security.ttaallkk.dto.request.CommentUpdateDto;
import security.ttaallkk.dto.response.CommentPagingResponseDto;
import security.ttaallkk.dto.response.CommentResponseDto;
import security.ttaallkk.exception.UidNotFoundException;
import security.ttaallkk.repository.member.MemberRepository;
import security.ttaallkk.repository.post.CommentRepository;
import security.ttaallkk.repository.post.CommentRepositorySupport;
import security.ttaallkk.repository.post.PostRepository;
import security.ttaallkk.service.notification.FcmService;
import security.ttaallkk.exception.CommentIsAlreadyRemovedException;
import security.ttaallkk.exception.CommentNotFoundException;
import security.ttaallkk.exception.PermissionDeniedException;
import security.ttaallkk.exception.PostNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final AuthenticationHelper authenticationHelper;
    private final FcmService fcmService;
    
    /**
     * 댓글 생성
     * @param commentCreateDto
     * @return CommentResponseDto
     */
    @Transactional
    public CommentResponseDto createComment(CommentCreateDto commentCreateDto) {
        Member member = memberRepository.findMemberByUid(commentCreateDto.getWriterUid()).orElseThrow(UidNotFoundException::new);
        
        Post post = postRepository.findPostByPostId(commentCreateDto.getPostId()).orElseThrow(PostNotFoundException::new);
        
        Comment comment = commentRepository.save(
            Comment.createComment(
                post,
                member, 
                commentCreateDto.getParentId() != null ? commentRepository.findById(commentCreateDto.getParentId()).orElseThrow(CommentNotFoundException::new) : null,
                commentCreateDto.getContent()
            )
        );

        // 댓글 생성 시 푸시메시지 전송 : 부모 댓글 없으면 글 작성자, 있으면 부모 댓글 작성자
        String deviceToken = (comment.getParent() == null)
                ? post.getWriter().getDeviceToken()
                : comment.getParent().getWriter().getDeviceToken();
        createFcmByCommentToSingleDevice(deviceToken, comment);

        return CommentResponseDto.convertCommentToDto(comment);
    }

    /**
     * 게시글 id로 댓글 조회
     * @param postId
     * @return List<CommentResponseDto>
     */
    public List<CommentResponseDto> findCommentByPostId(Long postId) {
        return CommentResponseDto.convertCommentStructure(commentRepositorySupport.findCommentByPostId(postId));
    }

    
    /**
     * 게시글의 최상위 부모 댓글 조회
     * @param postId 게시글 아이디
     * @param pageable
     * @return Page<CommentPagingResponseDto>
     */
    public Page<CommentPagingResponseDto> findCommentByPostIdForPaging(Long postId, Pageable pageable) {
        Page<CommentPagingResponseDto> page = commentRepositorySupport.findCommentByPostIdForPaging(postId, pageable);
        return page;
    }

    /**
     * 게시글의 부모 댓글 아이디와 연관된 자식 댓글 조회
     * @param postId 게시글 아이디
     * @param parentId 부모 댓글 아이디
     * @param pageable
     * @return
     */
    public Page<CommentPagingResponseDto> findCommentChildrenByParentIdForPaging(Long parentId, Long postId, Pageable pageable) {
        Page<CommentPagingResponseDto> page = commentRepositorySupport.findCommentChildrenByParentIdForPaging(parentId, postId, pageable);
        return page;
    }

    /**
     * 작성자 uid로 댓글 조회
     * @param uid
     * @return List<CommentResponseDto>
     */
    public List<CommentCommonDto> findCommentByWriterUid(String uid) {
        return CommentCommonDto.convertCommentCommonDto(commentRepositorySupport.findCommentByWriterUid(uid));
    }
    
    /**
     * 댓글 내용 업데이트 (댓글 권한이 있는 사용자 또는 관리자만 수정 가능 + 삭제상태가 아닌 댓글만 수정 가능)
     * @param commentUpdateDto
     */
    @Transactional
    public void updateCommentContent(CommentUpdateDto commentUpdateDto, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        Boolean isOwner = validationIsOwner(comment);
        if(!comment.getIsDeleted()) {
            if(isOwner) {
                comment.updateCommentContent(commentUpdateDto.getContent());
            }else{
                throw new PermissionDeniedException();
            }
        }else{
            throw new CommentIsAlreadyRemovedException();
        }
    }

    /**
     * 댓글 삭제(댓글 권한이 있는 사용자 또는 관리자만 삭제 가능 + 삭제상태가 아닌 댓글만 삭제 가능)
     * @param commentId
     */
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findCommentByIdWithParent(commentId).orElseThrow(CommentNotFoundException::new);
        Boolean isOwner = validationIsOwner(comment);
        if(!comment.getIsDeleted()) {
            if(isOwner) {
                comment.updateCommentIsDeleted(true); //추후 데이터 확인을 위해 삭제 상태로 업데이트, content 값은 데이터베이스에 유지
            }else{
                throw new PermissionDeniedException(); //댓글 주인이 아니면 권한오류
            }
        }else{
            throw new CommentIsAlreadyRemovedException();
        }
    }

    /**
     * 단일타겟(글 또는 부모 댓글 작성자)에게 보낼 FCM Message 객체 생성
     * @param deviceToken
     * @param comment
     */
    private void createFcmByCommentToSingleDevice(String deviceToken, Comment comment) {
        if(StringUtils.isNotEmpty(deviceToken)) {
            Message message = Message.builder()
                    .putData("title", comment.getWriter().getDisplayName())
                    .putData("body", comment.getContent())
                    .putData("notificationType", getNotificationType(comment))
                    .putData("postId", comment.getPost().getId().toString())
                    .putData("categoryTag", comment.getPost().getCategory().getCtgTag())
                    .setToken(deviceToken)
                    .setWebpushConfig(
                        WebpushConfig.builder()
                            .putHeader("ttl", "300")
                            .build()
                        )
                    .build();
            fcmService.sendFcm(message);
        }
    }

    /**
     * 루트댓글이면 COMMENT, 자식댓글이면 CHILDRENCOMMENT 값의 알림타입 설정
     * @param comment
     * @return notificationType
     */
    private String getNotificationType(Comment comment) {
        String notificationType = (comment.getParent() == null) 
            ? NotificationType.COMMENT.getTypeName()
            : NotificationType.CHILDRENCOMMENT.getTypeName();
    
        return notificationType;
    };

    /**
     * 댓글 주인 or 관리자인지 체크
     * @param comment
     * @return Boolean
     */
    private Boolean validationIsOwner(Comment comment) {
        if(authenticationHelper.isOwnerEmail(comment.getWriter().getEmail()) || authenticationHelper.isAdmin()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 삭제 가능한 상위 댓글 조회
     * @param comment (현재 댓글)
     * @return comment (삭제해야할 댓글)
     */
    private Comment getDeletableParentComment(Comment comment) {
        Comment parent = comment.getParent(); //현재 댓글의 부모 댓글
        if(parent != null && parent.getChildren().size() == 1 && parent.getIsDeleted() == true)
            return getDeletableParentComment(parent); //부모가 있고, 부모의 자식댓글이 현재댓글이고, 부모댓글의 상태가 삭제상태일 경우 재귀호출
        return comment; //삭제해야할 댓글 반환
    }
}
