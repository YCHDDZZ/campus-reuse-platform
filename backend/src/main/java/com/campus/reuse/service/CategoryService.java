package com.campus.reuse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campus.reuse.dto.request.*;
import com.campus.reuse.entity.*;
import com.campus.reuse.enums.*;
import com.campus.reuse.exception.AppException;
import com.campus.reuse.mapper.*;
import com.campus.reuse.security.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;

    CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> listEnabled() {
        return categoryMapper.selectList(new QueryWrapper<Category>()
                .eq("enabled", 1)
                .orderByAsc("sort_no"));
    }

    public List<Category> listAll() {
        return categoryMapper.selectList(new QueryWrapper<Category>().orderByAsc("sort_no"));
    }

    @Transactional
    public Category save(CategoryRequest request) {
        Category category = new Category();
        category.name = request.name;
        category.sortNo = request.sortNo;
        category.enabled = request.enabled;
        category.createdAt = LocalDateTime.now();
        category.updatedAt = LocalDateTime.now();
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public Category update(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new AppException("分类不存在");
        }
        category.name = request.name;
        category.sortNo = request.sortNo;
        category.enabled = request.enabled;
        category.updatedAt = LocalDateTime.now();
        categoryMapper.updateById(category);
        return category;
    }

    @Transactional
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
