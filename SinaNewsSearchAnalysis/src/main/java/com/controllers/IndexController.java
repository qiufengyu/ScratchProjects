package com.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;



@Controller
@RequestMapping({"/index"})
public class IndexController {
    @RequestMapping("")
    public ModelAndView index() {
        ModelAndView indexModelAndView = new ModelAndView("index");
        List<Integer> integerList = new ArrayList<Integer>();
        for (int i = 0; i<10; ++i) {
            integerList.add(i*i);
        }

        indexModelAndView.addObject("list", integerList);
        return indexModelAndView;
    }
}
