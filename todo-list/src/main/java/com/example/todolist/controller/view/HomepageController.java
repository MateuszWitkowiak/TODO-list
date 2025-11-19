package com.example.todolist.controller.view;

import org.springframework.stereotype.Controller;

@Controller
public class HomepageController {
    public String Home(){
        return "index";
    }
}
