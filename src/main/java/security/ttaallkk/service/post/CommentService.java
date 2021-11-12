package security.ttaallkk.service.post;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.member.Member;
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
import security.ttaallkk.exception.CommentNotFoundException;
import security.ttaallkk.exception.PostNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class CommentService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    
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
        return commentRepositorySupport.findCommentByWriterUid(uid);
    }
    
    /**
     * 댓글 내용 업데이트
     * @param commentUpdateDto
     */
    @Transactional
    public void updateCommentContent(CommentUpdateDto commentUpdateDto, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        Boolean isOwner = validationIsOwner(comment);
        if(isOwner){
            comment.updateCommentContent(commentUpdateDto.getContent());
        }
    }

    /**
     * 댓글 삭제(댓글 주인 or 관리자만 삭제가능)
     * @param commentId
     */
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findCommentByIdWithParent(commentId).orElseThrow(CommentNotFoundException::new);
        Boolean isOwner = validationIsOwner(comment);
        if(!isOwner) throw new RuntimeException("Permission Denied"); //댓글 주인이 아니면 권한오류
        if(comment.getChildren().size() > 0){
            comment.updateCommentIsDeleted(true); //자식댓글이 있으면 삭제상태로 업데이트
        }else{
            commentRepository.delete(getDeletableParentComment(comment)); //자식댓글이 없으면 DB에서 삭제처리
        }
    }

    /**
     * 댓글 주인 or 관리자인지 체크
     * @param comment
     * @return Boolean
     */
    private Boolean validationIsOwner(Comment comment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(comment.getWriter().getEmail().equals(authentication.getName()) || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
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
