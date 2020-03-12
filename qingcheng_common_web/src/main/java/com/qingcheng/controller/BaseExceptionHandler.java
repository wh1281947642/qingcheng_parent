package com.qingcheng.controller;

import com.qingcheng.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * <p>
 * <code>BaseExceptionHandler</code>
 * </p>
 *  公众异常处理类
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/03/12 16:37
 */
@ControllerAdvice //控制器通知类
public class BaseExceptionHandler {

    private Logger logger= LoggerFactory.getLogger(BaseExceptionHandler.class);

    @ResponseBody
    @ExceptionHandler(Exception.class) //指定检测的异常类型
    public Result error(Exception e){
        System.out.println("调用了异常处理");
        e.printStackTrace();
        logger.error(e.getMessage());
        return new Result(1,e.getMessage());
    }

}
