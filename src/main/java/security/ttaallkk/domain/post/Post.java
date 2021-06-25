package security.ttaallkk.domain.post;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue
    @Column(name="post_id")
    private Long id;

    private String writer;

    private String writerName;

    private String profileImg;

    private String title;

    @Lob //Larg Object
    private String content;

    @CreatedDate
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime dateTime;

    private Integer likeCnt;

}
