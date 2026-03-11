package com.library.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.library.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Category getCategoryByName(String name);
    List<String> listCategoryNames();
}
