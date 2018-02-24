package com.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MyErrorController implements ErrorController {

    private static final String PATH = "/error";

    private boolean debug;

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = PATH)
    public ModelAndView error(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error");
        Map<String, Object> errorAttributes = getErrorAttributes(request, debug);
        modelAndView.addObject("errorcode", response.getStatus());
        for(Map.Entry<String, Object> entry: errorAttributes.entrySet()) {
            modelAndView.addObject(entry.getKey(), (String) entry.getValue().toString());
        }
        return modelAndView;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }
}
