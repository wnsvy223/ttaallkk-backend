package security.ttaallkk.controller.post;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.post.Category;
import security.ttaallkk.dto.request.CategoryCreateDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.service.post.CategoryService;

@Controller
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Log4j2
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 게시판 카테고리 생성
     * @param categoryCreateDto
     * @return Response
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 카테고리 생성은 관리자만 가능
    @PostMapping
    public ResponseEntity<Response> createComment(@RequestBody CategoryCreateDto categoryCreateDto) {
       
        categoryService.createCategory(categoryCreateDto);
       
        Response response = Response.builder()
            .status(200)
            .message("게시판 카테고리 생성 성공").build();
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 카테고리 리스트 조회
     * @return List<Category>
     */
    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAllCategorys() {
        List<Category> result = categoryService.getAllCategorys();

        return ResponseEntity
                .status(200)
                .body(result);
    }

    /**
     * 모든 카테고리 삭제
     * @return Response
     */
    @DeleteMapping("/all")
    public ResponseEntity<Response> deleteAllCategory() {
        categoryService.deleteAllCategory();
        
        Response response = Response.builder()
            .status(200)
            .message("카테고리 전체 삭제 성공").build();
        return ResponseEntity.ok(response);
    }
}
