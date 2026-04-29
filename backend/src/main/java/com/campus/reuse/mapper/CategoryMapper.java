package com.campus.reuse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.campus.reuse.entity.Category;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
