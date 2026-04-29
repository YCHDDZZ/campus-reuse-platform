package com.campus.reuse.exception;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import com.campus.reuse.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Object>> handleApp(AppException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Object>> handleValid(MethodArgumentNotValidException exception) {
        StringBuilder builder = new StringBuilder("参数校验失败：");
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            // 尝试将字段名转换为中文描述
            String fieldName = fieldError.getField();
            String fieldDisplayName = getFieldDisplayName(fieldName);
            String defaultMessage = fieldError.getDefaultMessage();

            // 如果字段名和默认消息相同，说明没有提供具体错误信息，使用通用格式
            if (fieldName.equals(defaultMessage)) {
                builder.append(fieldDisplayName).append("不能为空;");
            } else {
                // 将字段名替换为中文描述
                builder.append(fieldDisplayName).append(defaultMessage).append(';');
            }
        }
        return ResponseEntity.badRequest().body(ApiResponse.fail(builder.toString()));
    }

    // 将字段名映射为中文描述
    private String getFieldDisplayName(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "title":
                return "商品标题";
            case "description":
                return "商品描述";
            case "price":
                return "商品价格";
            case "categoryid":
                return "商品分类";
            case "username":
                return "用户名";
            case "password":
                return "密码";
            case "realname":
                return "真实姓名";
            case "email":
                return "邮箱";
            case "phone":
                return "手机号";
            case "status":
                return "状态";
            case "note":
                return "备注";
            case "content":
                return "内容";
            case "images":
                return "商品图片";
            case "keyword":
                return "关键词";
            case "sortby":
                return "排序方式";
            case "auditstatus":
                return "审核状态";
            default:
                return fieldName; // 如果没有匹配项，返回原字段名
        }
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Object>> handleOther(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("系统异常：" + exception.getMessage()));
    }
}
