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
     * ?????? ??????
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

        // ?????? ?????? ??? ??????????????? ?????? : ?????? ?????? ????????? ??? ?????????, ????????? ?????? ?????? ?????????
        String deviceToken = (comment.getParent() == null)
                ? post.getWriter().getDeviceToken()
                : comment.getParent().getWriter().getDeviceToken();
        createFcmByCommentToSingleDevice(deviceToken, comment);

        return CommentResponseDto.convertCommentToDto(comment);
    }

    /**
     * ????????? id??? ?????? ??????
     * @param postId
     * @return List<CommentResponseDto>
     */
    public List<CommentResponseDto> findCommentByPostId(Long postId) {
        return CommentResponseDto.convertCommentStructure(commentRepositorySupport.findCommentByPostId(postId));
    }

    
    /**
     * ???????????? ????????? ?????? ?????? ??????
     * @param postId ????????? ?????????
     * @param pageable
     * @return Page<CommentPagingResponseDto>
     */
    public Page<CommentPagingResponseDto> findCommentByPostIdForPaging(Long postId, Pageable pageable) {
        Page<CommentPagingResponseDto> page = commentRepositorySupport.findCommentByPostIdForPaging(postId, pageable);
        return page;
    }

    /**
     * ???????????? ?????? ?????? ???????????? ????????? ?????? ?????? ??????
     * @param postId ????????? ?????????
     * @param parentId ?????? ?????? ?????????
     * @param pageable
     * @return
     */
    public Page<CommentPagingResponseDto> findCommentChildrenByParentIdForPaging(Long parentId, Long postId, Pageable pageable) {
        Page<CommentPagingResponseDto> page = commentRepositorySupport.findCommentChildrenByParentIdForPaging(parentId, postId, pageable);
        return page;
    }

    /**
     * ????????? uid??? ?????? ??????
     * @param uid
     * @return List<CommentResponseDto>
     */
    public List<CommentCommonDto> findCommentByWriterUid(String uid) {
        return CommentCommonDto.convertCommentCommonDto(commentRepositorySupport.findCommentByWriterUid(uid));
    }
    
    /**
     * ?????? ?????? ???????????? (?????? ????????? ?????? ????????? ?????? ???????????? ?????? ?????? + ??????????????? ?????? ????????? ?????? ??????)
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
     * ?????? ??????(?????? ????????? ?????? ????????? ?????? ???????????? ?????? ?????? + ??????????????? ?????? ????????? ?????? ??????)
     * @param commentId
     */
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findCommentByIdWithParent(commentId).orElseThrow(CommentNotFoundException::new);
        Boolean isOwner = validationIsOwner(comment);
        if(!comment.getIsDeleted()) {
            if(isOwner) {
                comment.updateCommentIsDeleted(true); //?????? ????????? ????????? ?????? ?????? ????????? ????????????, content ?????? ????????????????????? ??????
            }else{
                throw new PermissionDeniedException(); //?????? ????????? ????????? ????????????
            }
        }else{
            throw new CommentIsAlreadyRemovedException();
        }
    }

    /**
     * ????????????(??? ?????? ?????? ?????? ?????????)?????? ?????? FCM Message ?????? ??????
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
     * ?????????????????? COMMENT, ?????????????????? CHILDRENCOMMENT ?????? ???????????? ??????
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
     * ?????? ?????? or ??????????????? ??????
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
     * ?????? ????????? ?????? ?????? ??????
     * @param comment (?????? ??????)
     * @return comment (??????????????? ??????)
     */
    private Comment getDeletableParentComment(Comment comment) {
        Comment parent = comment.getParent(); //?????? ????????? ?????? ??????
        if(parent != null && parent.getChildren().size() == 1 && parent.getIsDeleted() == true)
            return getDeletableParentComment(parent); //????????? ??????, ????????? ??????????????? ??????????????????, ??????????????? ????????? ??????????????? ?????? ????????????
        return comment; //??????????????? ?????? ??????
    }
}
