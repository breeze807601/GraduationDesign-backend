package com.example.backend.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 捕获登录鉴权异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = NotLoginException.class)
    public Result<String> exceptionHandler(NotLoginException e) {
        return Result.error(401,e.getMessage());
    }
    /**
     * 捕获角色权限异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = NotRoleException.class)
    public Result<String> handlerException(NotRoleException e) {
        return Result.error(403,"没有权限！");
    }
    @ExceptionHandler(value = RuntimeException.class)
    public Result<String> handlerException(RuntimeException e) {
        return Result.error(e.getMessage());
    }
    /**
     * 捕获SQL异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            return Result.error(username + "已存在！");
        }else {
            return Result.error("未知错误！");
        }
    }
}