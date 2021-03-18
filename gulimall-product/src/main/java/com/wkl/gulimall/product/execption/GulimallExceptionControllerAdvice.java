package com.wkl.gulimall.product.execption;

import com.wkl.common.exception.BizCodeEnum;
import com.wkl.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

//@RestController
//@ControllerAdvice(basePackages = "com.wkl.gulimall.product.controller")
//合并上两个注解
//要处理的包
@RestControllerAdvice(basePackages = "com.wkl.gulimall.product.controller")
@Slf4j
public class GulimallExceptionControllerAdvice {


    /**
     * 这个统一处理MethodArgumentNotValidException类报出的异常
     * @return
     */
    @ExceptionHandler(value= MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}，异常类型：{}",e.getMessage(),e.getClass());
        //处理过程与之前相似，从参数中取出异常异常信息并返回
        HashMap<Object, Object> map = new HashMap<>();
        BindingResult bindingResult = e.getBindingResult();
        bindingResult.getFieldErrors().forEach((fieldError)->{
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        //返回信息时不用手动指定状态码了，使用自定义的枚举类，是有某种规范的
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode()
                , BizCodeEnum.VAILD_EXCEPTION.getMsg()).put("data", map);
    }

    /**
     * 处理所有抛出的异常
     * @param throwable
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("错误：",throwable);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(),BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
