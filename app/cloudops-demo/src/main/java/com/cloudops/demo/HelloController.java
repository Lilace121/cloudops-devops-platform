package com.cloudops.demo;


import org.springframework.web.bind.annotation.*;


@RestController
public class HelloController {


    @GetMapping("/")
    public String hello(){

        return "CloudOps DevOps Platform Running";

    }

}
