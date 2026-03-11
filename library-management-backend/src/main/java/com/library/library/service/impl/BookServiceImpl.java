package com.library.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.library.dto.BookDTO;
import com.library.library.entity.Book;
import com.library.library.entity.Category;
import com.library.library.exception.UnauthorizedException;
import com.library.library.mapper.BookMapper;
import com.library.library.service.BookService;
import com.library.library.service.CategoryService;
import com.library.library.vo.PageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageVO<Book> searchBooks(String keyword, String category, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(Book::getTitle, keyword)
                .or().like(Book::getAuthor, keyword)
                .or().like(Book::getIsbn, keyword));
        }
        
        if (category != null && !category.trim().isEmpty()) {
            // 根据分类名称查询分类ID
            Category cat = categoryService.getCategoryByName(category);
            if (cat != null) {
                queryWrapper.eq(Book::getCategoryId, cat.getId());
            }
        }

        queryWrapper.eq(Book::getDeleted, 0);
        queryWrapper.orderByDesc(Book::getCreateTime);

        long total = this.count(queryWrapper);
        List<Book> records = this.list(queryWrapper);
        
        // 填充分类名称
        for (Book book : records) {
            if (book.getCategoryId() != null) {
                Category cat = categoryService.getById(book.getCategoryId());
                if (cat != null) {
                    book.setCategory(cat.getName());
                }
            }
        }
        
        // 简单分页处理（实际应使用分页插件）
        if (page != null && pageSize != null) {
            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, records.size());
            if (fromIndex > records.size()) {
                records = List.of();
            } else {
                records = records.subList(fromIndex, toIndex);
            }
        }

        PageVO<Book> result = new PageVO<>();
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    @Override
    public BookDTO getBookDetail(Long bookId) {
        Book book = this.getById(bookId);
        if (book == null) {
            throw new UnauthorizedException("图书不存在");
        }
        // 填充分类名称
        if (book.getCategoryId() != null) {
            Category cat = categoryService.getById(book.getCategoryId());
            if (cat != null) {
                book.setCategory(cat.getName());
            }
        }
        BookDTO dto = new BookDTO();
        BeanUtils.copyProperties(book, dto);
        return dto;
    }

    @Override
    @Transactional
    public void addBook(BookDTO bookDTO) {
        Book book = new Book();
        BeanUtils.copyProperties(bookDTO, book);
        // 处理分类名称到分类ID的转换
        if (bookDTO.getCategory() != null && !bookDTO.getCategory().trim().isEmpty()) {
            Category cat = categoryService.getCategoryByName(bookDTO.getCategory());
            if (cat != null) {
                book.setCategoryId(cat.getId());
            }
        }
        // 确保初始可借库存不大于总库存
        if (book.getAvailableStock() > book.getTotalStock()) {
            book.setAvailableStock(book.getTotalStock());
        }
        this.save(book);
    }

    @Override
    @Transactional
    public void updateBook(Long bookId, BookDTO bookDTO) {
        Book book = this.getById(bookId);
        if (book == null) {
            throw new UnauthorizedException("图书不存在");
        }
        BeanUtils.copyProperties(bookDTO, book);
        // 处理分类名称到分类ID的转换
        if (bookDTO.getCategory() != null && !bookDTO.getCategory().trim().isEmpty()) {
            Category cat = categoryService.getCategoryByName(bookDTO.getCategory());
            if (cat != null) {
                book.setCategoryId(cat.getId());
            }
        }
        this.updateById(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = this.getById(bookId);
        if (book == null) {
            throw new UnauthorizedException("图书不存在");
        }
        // 物理删除：直接执行 SQL 删除
        baseMapper.physicallyDeleteById(bookId);
    }

    @Override
    public List<String> listCategories() {
        return categoryService.listCategoryNames();
    }

    @Override
    public long countBooks() {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Book::getDeleted, 0);
        return count(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getCategoryDistribution() {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Book::getDeleted, 0);
        List<Book> books = list(queryWrapper);
        
        // 按分类ID分组统计
        Map<Long, Long> categoryMap = books.stream()
                .filter(b -> b.getCategoryId() != null)
                .collect(Collectors.groupingBy(
                        Book::getCategoryId,
                        Collectors.counting()
                ));
        
        // 转换为分类名称
        return categoryMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    Category cat = categoryService.getById(entry.getKey());
                    String name = cat != null ? cat.getName() : "未分类";
                    map.put("name", name);
                    map.put("value", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}