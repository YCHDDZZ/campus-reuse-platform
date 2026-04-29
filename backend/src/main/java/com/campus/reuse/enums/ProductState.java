package com.campus.reuse.enums;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

public enum ProductState {
    DRAFT, AVAILABLE, RESERVED, SOLD, OFFLINE, DONATING, FROZEN, AUDITING
}
