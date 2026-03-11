package com.library.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.library.entity.Category;
import com.library.library.mapper.CategoryMapper;
import com.library.library.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public Category getCategoryByName(String name) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, name);
        queryWrapper.eq(Category::getDeleted, 0);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<String> listCategoryNames() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getDeleted, 0);
        queryWrapper.orderByAsc(Category::getSort);
        return this.list(queryWrapper).stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }
}
