package com.library.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.library.entity.Book;
import com.library.library.entity.Category;
import com.library.library.service.BookService;
import com.library.library.service.CategoryService;
import com.library.library.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getDeleted, 0);
        queryWrapper.orderByAsc(Category::getId);
        List<Category> list = categoryService.list(queryWrapper);
        
        // 统计每个分类的图书数量
        for (Category category : list) {
            LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
            bookWrapper.eq(Book::getCategoryId, category.getId());
            bookWrapper.eq(Book::getDeleted, 0);
            Long count = bookService.count(bookWrapper);
            category.setBookCount(count.intValue());
        }
        
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.success(category);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Category category) {
        // 自动设置父分类ID为0（一级分类）
        category.setParentId(0L);
        categoryService.save(category);
        return Result.success(null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success(null);
    }

    @GetMapping("/{id}/books")
    public Result<List<Book>> getBooksByCategory(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.success(List.of());
        }
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Book::getCategoryId, id);
        List<Book> books = bookService.list(queryWrapper);
        // 填充分类名称
        for (Book book : books) {
            book.setCategory(category.getName());
        }
        return Result.success(books);
    }
}
