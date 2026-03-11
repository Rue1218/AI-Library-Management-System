package com.library.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.library.config.RequireRole;
import com.library.library.dto.BookDTO;
import com.library.library.service.BookService;
import com.library.library.vo.PageVO;
import com.library.library.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public Result<PageVO<com.library.library.entity.Book>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageVO<com.library.library.entity.Book> result = bookService.searchBooks(keyword, category, page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<BookDTO> getBookDetail(@PathVariable Long id) {
        BookDTO dto = bookService.getBookDetail(id);
        return Result.success(dto);
    }

    @PostMapping
    @RequireRole("0")
    public Result<Void> addBook(@Valid @RequestBody BookDTO bookDTO) {
        bookService.addBook(bookDTO);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RequireRole("0")
    public Result<Void> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO bookDTO) {
        bookService.updateBook(id, bookDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RequireRole("0")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success(null);
    }

    @GetMapping("/categories")
    public Result<List<String>> listCategories() {
        List<String> categories = bookService.listCategories();
        return Result.success(categories);
    }

    @GetMapping("/recommended")
    public Result<List<com.library.library.entity.Book>> getRecommendedBooks(
            @RequestParam(defaultValue = "8") Integer limit) {
        List<com.library.library.entity.Book> books = bookService.list(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.library.library.entity.Book>()
                .eq("deleted", 0)
                .gt("available_stock", 0)
                .last("LIMIT " + limit)
        );
        return Result.success(books);
    }
}
