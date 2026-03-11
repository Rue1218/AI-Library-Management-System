package com.library.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.library.dto.BookDTO;
import com.library.library.entity.Book;
import com.library.library.vo.PageVO;

import java.util.List;
import java.util.Map;

public interface BookService extends IService<Book> {
    PageVO<Book> searchBooks(String keyword, String category, Integer page, Integer pageSize);
    
    BookDTO getBookDetail(Long bookId);
    
    void addBook(BookDTO bookDTO);
    
    void updateBook(Long bookId, BookDTO bookDTO);
    
    void deleteBook(Long bookId);
    
    List<String> listCategories();
    
    /**
     * 统计图书总数
     */
    long countBooks();
    
    /**
     * 获取图书分类分布
     */
    List<Map<String, Object>> getCategoryDistribution();
}