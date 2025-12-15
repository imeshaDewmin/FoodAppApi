package com.selfstudy.foodapp.menu.service;

import com.selfstudy.foodapp.aws.AwsS3Service;
import com.selfstudy.foodapp.category.entity.Category;
import com.selfstudy.foodapp.category.repository.CategoryRepository;
import com.selfstudy.foodapp.exceptions.BadRequestException;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.menu.dto.MenuDto;
import com.selfstudy.foodapp.menu.entity.Menu;
import com.selfstudy.foodapp.menu.repository.MenuRepository;
import com.selfstudy.foodapp.response.Response;
import com.selfstudy.foodapp.review.dto.ReviewDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

    @Autowired
    public MenuRepository menuRepository;

    @Autowired
    public CategoryRepository categoryRepository;

    @Autowired
    private AwsS3Service awsS3Service;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Response<MenuDto> createMenu(MenuDto menuDto) {
        log.info("Inside createMenu()");

        Category category = categoryRepository.findById(menuDto.getCategoryId())
                .orElseThrow(()-> new NotFoundException("Category not found"));

        String imageUrl =null;

        MultipartFile imageFile = menuDto.getImage();

        if(imageFile == null || imageFile.isEmpty()){
            throw new BadRequestException("Image is required");
        }

        String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        URL s3Url = awsS3Service.uploadFile("menus/" + imageName, imageFile);
        imageUrl = s3Url.toString();

        Menu menu = Menu.builder()
                .name(menuDto.getName())
                .description(menuDto.getDescription())
                .imageUrl(imageUrl)
                .category(category)
                .price(menuDto.getPrice())
                    .build();

        Menu savedMenu = menuRepository.save(menu);

        return Response.<MenuDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu created successfully")
                .data(modelMapper.map(savedMenu,MenuDto.class))
                .build();
    }

    @Override
    public Response<MenuDto> updateMenu(MenuDto menuDto) {
        log.info("Inside updateMenu()");

        Menu menu = menuRepository.findById(menuDto.getId())
                .orElseThrow(()-> new NotFoundException("Menu not found"));

        //category update
        Category category = categoryRepository.findById(menuDto.getCategoryId())
                .orElseThrow(()-> new NotFoundException("Category not found"));

        if (category != null)
            menu.setCategory(category);

        //image update
        String imageUrl = menu.getImageUrl();

        MultipartFile imageFile = menuDto.getImage();

        if(imageFile != null && !imageFile.isEmpty()){
            //delete old image first
            if(imageUrl != null && !imageUrl.isEmpty()){
                String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                awsS3Service.deleteFile("menus/"+ keyName);
                log.info("Deleting old image from s3");
            }
            //upload the new image
            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            URL newImageUrl = awsS3Service.uploadFile("menus/" + imageName,imageFile);
            menu.setImageUrl(newImageUrl.toString());

        }

        //other field updates
        if(menuDto.getName() != null && !menuDto.getName().isEmpty())
            menu.setName(menuDto.getName());

        if(menuDto.getDescription() != null && !menuDto.getDescription().isEmpty())
            menu.setDescription(menuDto.getDescription());

        if(menuDto.getPrice() != null )
            menu.setPrice(menuDto.getPrice());

        //save the updated menu
        menuRepository.save(menu);

        return Response.<MenuDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu updated successfully")
                .build();
    }

    @Override
    public Response<MenuDto> getMenuById(Long id) {
        log.info("Inside getMenuById()");

        Menu menu = menuRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Menu not found"));

        MenuDto menuDto = modelMapper.map(menu,MenuDto.class);

        if(menuDto.getReviews() != null){
            menuDto.getReviews().sort(Comparator.comparing(ReviewDto::getId).reversed());
        }

        return Response.<MenuDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu retrieved successfully")
                .data(menuDto)
                .build();
    }

    @Override
    public Response<?> deleteMenu(Long id) {
        log.info("Inside deleteMenu()");

        Menu menu = menuRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Menu not found"));

        //delete the image from s3 if exists
        String imageUrl = menu.getImageUrl();
        if(imageUrl != null && !imageUrl.isEmpty()){
            String keyName = imageUrl.substring(imageUrl.lastIndexOf("/" + 1));
            awsS3Service.deleteFile("menus/" + keyName);
            log.info("deleted image from s3");
        }

        menuRepository.deleteById(menu.getId());

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu deleted successfully")
                .build();
    }

    @Override
    public Response<List<MenuDto>> getMenus(Long categoryId, String search) {
        log.info("Inside getMenus()");

        Specification<Menu> spec = buildSpecification(categoryId,search);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        List<Menu> menus = menuRepository.findAll(spec,sort);

        List<MenuDto> menuDto = menus.stream().
                map(menu -> modelMapper.map(menu, MenuDto.class)).toList();

        return Response.<List<MenuDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu List retrieved successfully")
                .data(menuDto)
                .build();
    }

    private Specification<Menu> buildSpecification(Long categoryId, String search) {
        return (root, query, cb) -> {
            // List to accumulate all WHERE conditions
            List<Predicate> predicates = new ArrayList<>();

            // Add category filter if categoryId is provided
            if (categoryId != null) {
                // Creates condition: category.id = providedCategoryId
                predicates.add(cb.equal(
                        root.get("category").get("id"), // Navigate to category->id
                        categoryId                      // Match provided category ID
                ));
            }

            // Add search term filter if a search text is provided
            if (search != null && !search.isBlank()) {
                // Prepare search term with wildcards for partial matching
                // Converts to lowercase for case-insensitive search
                String searchTerm = "%" + search.toLowerCase() + "%";

                // Creates OR condition for:
                // (name LIKE %term% OR description LIKE %term%)
                predicates.add(cb.or(
                        cb.like(
                                cb.lower(root.get("name")), // Convert name to lowercase
                                searchTerm                 // Match against search term
                        ),
                        cb.like(
                                cb.lower(root.get("description")), // Convert description to lowercase
                                searchTerm                        // Match against search term
                        )
                ));
            }

            // Combine all conditions with AND logic
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
