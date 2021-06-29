package security.ttaallkk.domain.post;


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
import javax.persistence.SequenceGenerator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.CommonDateTime;
import security.ttaallkk.domain.member.Member;

import org.apache.lucene.analysis.ko.KoreanFilterFactory;
import org.apache.lucene.analysis.ko.KoreanTokenizerFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Indexed
@AnalyzerDef(
    name = "koreanAnalyzer_post", 
    tokenizer = @TokenizerDef(factory = KoreanTokenizerFactory.class),
    filters = {@TokenFilterDef(factory = KoreanFilterFactory.class)})

public class Post extends CommonDateTime{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "post_seq")
    @SequenceGenerator(name="post_seq", sequenceName="POST_SEQ", allocationSize = 1)
    @Column(name="post_id")
    private Long id;

    //FK로 사용될 값을 Member Entity의 PK인 id대신 uid사용을 위해 referencedColumnName설정(JPA에서 기본값으로 PK인 id를 사용하도록 되어있음)
    @ManyToOne(targetEntity = Member.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private Member writer;

    @Column(name = "title")
    @Field
    @Analyzer(definition = "koreanAnalyzer_post")
    private String title;

    @Lob //Larg Object
    @Column(name = "content")
    @Field
    @Analyzer(definition = "koreanAnalyzer_post")
    private String content;

    @Column(name="likeCnt")
    private Integer likeCnt;

    @Builder
    public Post(Member writer, String title, String content){
        this.writer = writer;
        this.title = title;
        this.content = content;
    }
}