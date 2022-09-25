package security.ttaallkk.service.post;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.post.Category;
import security.ttaallkk.dto.request.CategoryCreateDto;
import security.ttaallkk.repository.post.CategoryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class CategoryService {

    private final CategoryRepository categoryRepository;
    
    /*
    @PostConstruct
    public void init(){ 
        Category c1 = Category.builder()
            .ctgName("자유 게시판")
            .ctgTag("free")
            .description("자유 게시판")
            .build();
        categoryRepository.save(c1);

        Category c2 = Category.builder()
            .ctgName("대화 게시판")
            .ctgTag("talk")
            .description("대화 게시판")
            .build();
        categoryRepository.save(c2);

        Category c3 = Category.builder()
            .ctgName("팁 게시판")
            .ctgTag("tip")
            .description("팁 게시판")
            .build();
        categoryRepository.save(c3);

        Category c4 = Category.builder()
            .ctgName("홍보 게시판")
            .ctgTag("ads")
            .description("홍보 게시판")
            .build();
        categoryRepository.save(c4);
    }
    */

    /**
     * 카테고리 생성
     * @param CategoryCreateDto
     * @return Category : 생성된 카테고리 정보
     */
    @Transactional
    public Category createCategory(CategoryCreateDto categoryCreateDto) {        
       Category category = Category.builder()
                .ctgName(categoryCreateDto.getCategoryName())
                .ctgTag(categoryCreateDto.getCategoryTag())
                .description(categoryCreateDto.getDescription())
                .build();
        
        return categoryRepository.save(category);
    }

    /**
     * 모든 카테고리 리스트 조회
     * @return List<Category>
     */
    @Transactional
    public List<Category> getAllCategorys() {
        List<Category> reusult = categoryRepository.findAll();
        return reusult;
    }
    
    /**
     * 모든 카테고리 삭제
     */
    @Transactional
    public void deleteAllCategory() {
        categoryRepository.deleteAll();
    }
}
