package security.ttaallkk.domain.post;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import security.ttaallkk.domain.CommonDateTime;
import security.ttaallkk.domain.member.Member;

@Entity
@Getter
@DynamicUpdate
public class Comment extends CommonDateTime{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long id;

    @Lob
    private String content;

    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_writer_id",  referencedColumnName = "uid")
    private Member writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    public static Comment createComment(Post post, Member writer, Comment parent, String content) {
        Comment comment = new Comment();
        comment.post = post;
        comment.writer = writer;
        comment.parent = parent;
        comment.content = content;
        comment.isDeleted = false;
        
        return comment;
    }

    public void updateCommentIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
        this.content = "삭제된 댓글입니다.";
    }

    public void updateCommentContent(String content) {
        this.content = content;
    }
}
