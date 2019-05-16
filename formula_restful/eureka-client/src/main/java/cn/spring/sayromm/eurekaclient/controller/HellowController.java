package cn.spring.sayromm.eurekaclient.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HellowController {

    @RequestMapping(value="/hello")
    public String hello(){
        return "Hellow World!";
    }
}
