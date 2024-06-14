package com.sparta.oneandzerobest.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j(topic = "Request Info")
@Aspect
@Component
@RequiredArgsConstructor
public class ApiRequestLogAop {

    @Autowired
    private HttpServletRequest request;

    @Pointcut("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *)")
    private void controller() {}


    @Before("controller()")
    public void executeLogging() {
        String url = request.getRequestURI();
        String method = request.getMethod();

        log.info("Request URL : "+url);
        log.info("Request Method :"+method);
    }

}
