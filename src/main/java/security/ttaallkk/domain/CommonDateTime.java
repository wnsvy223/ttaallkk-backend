package security.ttaallkk.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.SortableField;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;


/**
 * 생성일자, 수정일자 자동적용 Entity 클래스
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class CommonDateTime {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Field(index = Index.NO)
    @SortableField
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    @Field(index = Index.NO)
    @SortableField
    private LocalDateTime modifiedAt;
}
