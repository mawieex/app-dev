  package com.appdev.MindFlow.controller;
  
  import org.springframework.stereotype.Controller; 
  import org.springframework.ui.Model; 
  import org.springframework.web.bind.annotation.GetMapping; 
  import org.springframework.web.bind.annotation.PostMapping; 
  import org.springframework.web.bind.annotation.RequestParam;
 
  @Controller public class MindFlowController {
 
 @GetMapping("/welcome") public String welcome () { return "home";}
  
  @GetMapping("/user/home") public String login() { return "login"; }
  
  @PostMapping("user/validate") public String
  validateUser(@RequestParam("email") String email,
  
  @RequestParam("password") String password, Model model) {
 model.addAttribute("email", email); model.addAttribute("password", password);
 return "welcome"; }
  
  @GetMapping("/user/register") public String register() { return "register"; }
  
  }
  
  


