package com.controllers;

import com.models.SearchResult;
import com.models.User;
import com.service.UserService;
import offline.analysis.NewsClassification;
import offline.utils.Category;
import offline.utils.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.security.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class NewsController {

    static final Logger logger = LogManager.getLogger(NewsController.class.getName());

    private DatabaseUtil dbUtil = new DatabaseUtil();
    private NewsClassification nc = new NewsClassification();

    @RequestMapping(value="/news/{id}", method = RequestMethod.GET)
    public ModelAndView news(@PathVariable("id") String id) throws SQLException {
        ModelAndView modelAndView = new ModelAndView();
        int idInteger = Integer.valueOf(id);
        ResultSet rs = dbUtil.selectNewsById(idInteger);
        if(rs.next()) {
            String title = rs.getString("title");
            java.sql.Blob blob = rs.getBlob("content");
            String content = new String(blob.getBytes(1l, (int) blob.length()));
            String[] contentList = content.split("\n");
            int category = rs.getInt("category");
            // 转化成北京时间
            long timestamp = rs.getInt("timestamp") * 1000l;
            Date date = new Date(timestamp);
            DateFormat df = new SimpleDateFormat("yyyy年MM月dd日 hh:mm", Locale.CHINA);
            String url = rs.getString("url");
            String categoryText = Category.int2Cate(category);
            modelAndView.addObject("title", title);
            modelAndView.addObject("category", category);
            modelAndView.addObject("categoryText", categoryText);
            modelAndView.addObject("date", df.format(date));
            modelAndView.addObject("url", url);
            modelAndView.addObject("content", Arrays.asList(contentList));
        }
        else {
            modelAndView.addObject("errorMessage", "访问错误！");
        }

        modelAndView.setViewName("news");
        return modelAndView;
    }

    @RequestMapping(value="/category/{cate}", method = RequestMethod.GET)
    public ModelAndView category(@PathVariable("cate") String cate) throws SQLException {
        ModelAndView modelAndView = new ModelAndView();
        int cateInteger = Integer.valueOf(cate);
        ResultSet rs = dbUtil.selectNewsByCategory(cateInteger);
        List<SearchResult> resultList = new ArrayList<>();
        while(rs.next()) {
            SearchResult sr = new SearchResult();
            sr.setId(rs.getInt("id"));
            sr.setTitle(rs.getString("title"));
            sr.setCategory(rs.getInt("category"));
            sr.setUrl(rs.getString("url"));
            sr.setCategoryText(Category.int2Cate(sr.getCategory()));
            resultList.add(sr);
        }

        if (resultList.size() < 1) {
            modelAndView.addObject("errorMessage", "访问错误！");
        }
        else {
            modelAndView.addObject("latestNews", resultList);
        }
        modelAndView.setViewName("category");
        return modelAndView;
    }

    @RequestMapping(value="/admin/news/{id}", method = RequestMethod.GET)
    public ModelAndView adminNews(@PathVariable("id") String id) throws SQLException, IOException {
        ModelAndView modelAndView = new ModelAndView();
        int idInteger = Integer.valueOf(id);
        ResultSet rs = dbUtil.selectNewsById(idInteger);
        if(rs.next()) {
            String title = rs.getString("title");
            java.sql.Blob blob = rs.getBlob("content");
            String content = new String(blob.getBytes(1l, (int) blob.length()));
            String[] contentList = content.split("\n");
            int category = rs.getInt("category");
            // 转化成北京时间
            long timestamp = rs.getInt("timestamp") * 1000l;
            Date date = new Date(timestamp);
            DateFormat df = new SimpleDateFormat("yyyy年MM月dd日 hh:mm", Locale.CHINA);
            String url = rs.getString("url");
            String categoryText = Category.int2Cate(category);
            modelAndView.addObject("title", title);
            modelAndView.addObject("category", category);
            modelAndView.addObject("categoryText", categoryText);
            modelAndView.addObject("date", df.format(date));
            modelAndView.addObject("url", url);
            modelAndView.addObject("content", Arrays.asList(contentList));
            // 预测该新闻的类别
            int c = nc.test(title, 0);
            modelAndView.addObject("predictedCategory", c);
            modelAndView.addObject("predictedCategoryText", Category.int2Cate(c));
            modelAndView.addObject("newsId", idInteger);
        }
        else {
            modelAndView.addObject("errorMessage", "访问错误！");
        }
        modelAndView.setViewName("admin/news");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/news/change", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity changeNewsCategory(@RequestParam(name = "newsId") String newsId,
                                             @RequestParam(name = "categorySelect") String categorySelect) throws SQLException {
        int newsIdInt = Integer.valueOf(newsId);
        int cateSelInt = Integer.valueOf(categorySelect);
        logger.info("修改 ID = " + newsId +" 的新闻为【"+ Category.int2Cate(cateSelInt) +"】");
        if (dbUtil.updateCategory(newsIdInt, cateSelInt) > 0) {
            return new ResponseEntity(HttpStatus.ACCEPTED);
        }
        else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }


}
