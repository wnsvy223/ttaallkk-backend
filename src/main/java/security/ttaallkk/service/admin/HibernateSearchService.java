package security.ttaallkk.service.admin;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HibernateSearchService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public HibernateSearchService(EntityManager entityManager){
        super();
        this.entityManager = entityManager;
    }

    /**
     * Hibernate Search 초기화(인덱싱 갱신)
     */
    public void initializeHibernateSearch(){
        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
