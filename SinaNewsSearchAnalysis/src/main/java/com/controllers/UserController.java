package com.controllers;

import com.models.Role;
import com.models.User;
import com.service.UserService;
import com.validator.UserValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @RequestMapping(value={"/", "/login"}, method = RequestMethod.GET)
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @RequestMapping(value="/registration", method = RequestMethod.GET)
    public ModelAndView registration(){
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult,
                                      HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findByUsername(user.getUsername());
        if (userExists != null) {
            bindingResult
                    .rejectValue("username", "error.user",
                            "* 用户名已存在，请重试。");
        }
        userExists = userService.findByEmail(user.getEmail());
        if (userExists != null ) {
            bindingResult
                    .rejectValue("email", "error.user",
                            "* 电子邮件已存在，请重试。");
        }
        // 检查是否是合法的电子邮件地址
        if ( !EmailValidator.getInstance().isValid(user.getEmail()) ) {
            bindingResult
                    .rejectValue("email", "error.user",
                            "* 请使用合法的电子邮件地址");
        }


        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration");
        } else {
            String rawPassword = user.getPassword();
            userService.save(user);
            // 开始自动登录，只能以普通用户身份登录
            List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
            grantedAuthorities.add(new SimpleGrantedAuthority("USER"));
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(), rawPassword, grantedAuthorities);
            request.getSession();
            try {
                token.setDetails(new WebAuthenticationDetails(request));
                SecurityContextHolder.getContext().setAuthentication(token);
                request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            } catch( AuthenticationException e ){
                bindingResult
                        .rejectValue("username", "error.user",
                                "* 注册失败！");
                modelAndView.setViewName("registration");
                return modelAndView;
            }
            modelAndView.setViewName("redirect:home");
        }
        return modelAndView;
    }
}
