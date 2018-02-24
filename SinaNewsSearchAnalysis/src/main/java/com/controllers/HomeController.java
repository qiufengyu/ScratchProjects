package com.controllers;

import com.models.Role;
import com.models.SearchResult;
import com.models.User;
import com.service.UserService;
import offline.utils.Category;
import offline.utils.DatabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    private DatabaseUtil dbUtil = new DatabaseUtil();

    @RequestMapping(value="/home", method = RequestMethod.GET)
    public ModelAndView home() throws SQLException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        boolean isAdmin = false;
        for(Role r: user.getRoles()) {
            if (r.getRole().equalsIgnoreCase("ADMIN"))
                isAdmin = true;
        }
        modelAndView.addObject("isAdmin", isAdmin);
        System.out.println(user.getUsername());
        modelAndView.addObject("welcome", "欢迎 " + user.getUsername() + " ( " + user.getEmail() + " )！");
        modelAndView.setViewName("home");
        List<SearchResult> resultList = new ArrayList<>();
        ResultSet rs = dbUtil.getLatestNews();
        while(rs.next()) {
            SearchResult sr = new SearchResult();
            sr.setId(rs.getInt("id"));
            sr.setTitle(rs.getString("title"));
            sr.setCategory(rs.getInt("category"));
            sr.setUrl(rs.getString("url"));
            sr.setCategoryText(Category.int2Cate(sr.getCategory()));
            resultList.add(sr);
        }
        modelAndView.addObject("latestNews", resultList);
        return modelAndView;
    }
}
