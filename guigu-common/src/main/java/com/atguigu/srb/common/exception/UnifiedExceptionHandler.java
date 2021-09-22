package com.atguigu.srb.common.exception;

import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Component
@RestControllerAdvice
public class UnifiedExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public R handlerException(Exception e){
        System.out.println(e.getMessage());
        return R.error();
    }


    @ExceptionHandler(BadSqlGrammarException.class)
    public R yxhandlerBadSqlGrammarException(BadSqlGrammarException e){
        log.error(e.getMessage(),e);
        return R.setResult(ResponseEnum.BAD_SQL_GRAMMAR_ERROR);
    }
}
