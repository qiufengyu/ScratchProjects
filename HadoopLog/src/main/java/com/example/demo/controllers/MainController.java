package com.example.demo.controllers;

import com.example.demo.data.SiteDataLoader;
import com.example.demo.data.TransDataLoader;
import com.example.demo.entity.SiteIP;
import com.example.demo.entity.TransIP;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
*  网页展示相关逻辑
* */
@Controller
public class MainController {
    SiteDataLoader sdl;
    Map<String, Map<String, Integer>> sMap;
    List<SiteIP> siteCountList;
    List<SiteIP> siteIPCountList;
    TransDataLoader tdl;
    Map<String, Map<String, Integer>> tMap;
    List<TransIP> transCountList;
    List<TransIP> transIPCountList;


    public MainController() throws IOException {
        sdl  = new SiteDataLoader();
        sMap = sdl.getMap();
        tdl = new TransDataLoader();
        tMap = tdl.getMap();
    }

    // 访问首页，显示 index.html 内容
    @RequestMapping(value = {"/", "/index", "/site"}, method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView homeModelAndView = new ModelAndView("index");
        siteCountList = sdl.getSiteCountList();
        homeModelAndView.addObject("siteCountList", siteCountList);
        return homeModelAndView;
    }

    @RequestMapping(value = {"/", "/index", "/site"}, method = RequestMethod.POST)
    public ModelAndView indexPost(@RequestParam(name = "domainstring") String domainString) {
        ModelAndView homeModelAndView = new ModelAndView("index");
        // 右侧表格
        siteCountList = sdl.getSiteCountList();
        homeModelAndView.addObject("siteCountList", siteCountList);
        // 检索对应访问网址的用户 IP
        siteIPCountList = sdl.getSiteIPCountList(domainString.trim());
        if(siteIPCountList != null && siteIPCountList.size() > 0) {
            int truncatedNum = Math.min(10, siteIPCountList.size());
            homeModelAndView.addObject("siteIPCountList", siteIPCountList.subList(0, truncatedNum));
            homeModelAndView.addObject("siteInput", domainString);
            homeModelAndView.addObject("tableTitle", new String(domainString + " 访问排名直方图"));
        }
        else {
            // 检索的内容不存在
            homeModelAndView.addObject("invalidSite", 1);
        }
        return homeModelAndView;
    }

    // 显示事件处理的内容
    @RequestMapping(value = {"/trans"}, method = RequestMethod.GET)
    public ModelAndView trans() {
        ModelAndView transModelAndView = new ModelAndView("trans");
        transCountList = tdl.getTransCountList();
        transModelAndView.addObject("transCountList", transCountList);
        return transModelAndView;
    }

    @RequestMapping(value = {"/trans"}, method = RequestMethod.POST)
    public ModelAndView transPost(@RequestParam(name = "transstring") String transString) {
        ModelAndView homeModelAndView = new ModelAndView("trans");
        transCountList = tdl.getTransCountList();
        homeModelAndView.addObject("transCountList", transCountList);
        /*
        for(TransIP tip: transIPCountList) {
            System.out.println(tip.getTrans() + tip.getIp() + tip.getCount());
        }
        */
         // 检索对应的事件
        transIPCountList = tdl.getTransIPCountList(transString.trim());
        if(transIPCountList != null && transIPCountList.size() > 0) {
            int truncatedNum = Math.min(10, transIPCountList.size());
            homeModelAndView.addObject("transIPCountList", transIPCountList.subList(0, truncatedNum));
            homeModelAndView.addObject("transInput", transString);
            homeModelAndView.addObject("tableTitle", new String(transString + " 事件排名直方图"));
        }
        else {
            homeModelAndView.addObject("invalidTrans", 1);
        }
        return homeModelAndView;
    }


}
