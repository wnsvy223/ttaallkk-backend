package security.ttaallkk.domain.post;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.CommonDateTime;
import security.ttaallkk.domain.member.Member;

import org.apache.lucene.analysis.ko.KoreanFilterFactory;
import org.apache.lucene.analysis.ko.KoreanTokenizerFactory;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Indexed
@DynamicUpdate
@AnalyzerDef(
    name = "koreanAnalyzer_post", 
    tokenizer = @TokenizerDef(factory = KoreanTokenizerFactory.class),
    filters = {@TokenFilterDef(factory = KoreanFilterFactory.class)})

public class Post extends CommonDateTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_id")
    @Field(name = "id_sort", index = Index.NO) //hibernate fulltext search에서 검색결과를 가장최신글순으로 정렬하기 위해 인덱싱없이 정렬만을 위한 필드설정
    @SortableField(forField = "id_sort") //필드설정 후 정렬에 사용될 필드 이름 지정
    private Long id;

    //FK로 사용될 값을 Member Entity의 PK인 id대신 uid사용을 위해 referencedColumnName설정(JPA에서 기본값으로 PK인 id를 사용하도록 되어있음)
    @ManyToOne(targetEntity = Member.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "writer", referencedColumnName = "uid", nullable = false)
    @IndexedEmbedded //연관된 필드중 ManyToOne쪽에 @IndexEmbedded, OneToMany쪽에 @Field 설정시 Full Text Search쿼리에서 해당 내부 필드값을 조건으로 검색가능(ex. writer.displayName)
    private Member writer;

    //게시글 분류 카테고리
    @ManyToOne(targetEntity = Category.class, fetch = FetchType.LAZY)
    @JoinColumn(name ="category_id")
    private Category category;

    //댓글
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    //댓글 카운트
    @Formula("(select count(*) from comment c where c.post_id = post_id)")
    private Integer commentCnt;

    //좋아요
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Like> likes = new HashSet<>();

    @Column(name = "title")
    @Field
    @Analyzer(definition = "koreanAnalyzer_post")
    private String title;

    @Lob //Larg Object
    @Column(name = "content")
    @Field
    @Analyzer(definition = "koreanAnalyzer_post")
    private String content;

    @Column(name = "likeCnt", nullable = false)
    private Integer likeCnt;

    @Column(name = "views", nullable = false)
    private Integer views;

    @Column(name = "postStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus postStatus;
    
    @Builder
    public Post(Member writer, String title, String content, PostStatus postStatus, Integer views, Integer likeCnt) {
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.postStatus = postStatus;
        this.views = views;
        this.likeCnt = likeCnt;
    }

    //조회수 증가
    public void updateViewsCount() {
        this.views++;
    }

    //게시글 수정
    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    //좋아요 카운트 증가
    public void increaseLikeCount() {
        this.likeCnt++;
    }

    //좋아요 카운트 감소
    public void decreaseLikeCount() {
        this.likeCnt--;
    }
}
