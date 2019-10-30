package com.rybickim.javaquiz.data;

import com.rybickim.javaquiz.domain.Categories;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoryCrudRepository
        extends CrudRepository<Categories, Long> {

    @Query("SELECT c FROM Categories c JOIN FETCH c.questions")
    List<Categories> findFirstByCategory(Pageable pageable);

    Categories findFirstByCategoryName(String categoryName);
}
