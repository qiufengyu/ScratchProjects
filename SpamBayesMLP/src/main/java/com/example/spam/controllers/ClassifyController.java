package com.example.spam.controllers;

import com.example.spam.offline.Bayes;
import com.example.spam.offline.MLP;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;


@Controller
public class ClassifyController {

    private Bayes bayes = new Bayes();
    private MLP mlp = new MLP();

    @RequestMapping(value = {"/classify"}, method = RequestMethod.GET)
    public ModelAndView classifyDefault() {
        ModelAndView homeModelAndView = new ModelAndView("classify");
        return homeModelAndView;
    }

    @RequestMapping(value = {"/classify"}, method = RequestMethod.POST)
    public ModelAndView classify(@RequestParam(name = "emailText") String emailText) throws IOException {
        ModelAndView homeModelAndView = new ModelAndView("classify");
        homeModelAndView.addObject("holder", emailText);
        int bayesResult = bayes.testBayes(emailText);
        int mlpResult = mlp.test(emailText);
        homeModelAndView.addObject("result", mlpResult);
        if (bayesResult == mlpResult) {
            homeModelAndView.addObject("resultTextPrefix", "极有可能是");
        } else {
            homeModelAndView.addObject("resultTextPrefix", "有可能是");
        }
        return homeModelAndView;
    }



}
