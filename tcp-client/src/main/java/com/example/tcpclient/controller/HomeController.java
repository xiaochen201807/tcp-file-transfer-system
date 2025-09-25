package com.example.tcpclient.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.tcpclient.service.ClientConfigService;

/**
 * 客户端主页控制器
 */
@Slf4j
@Controller
public class HomeController {
    
    @Value("${server.port:8081}")
    private int httpPort;
    
    @Autowired
    private ClientConfigService clientConfigService;
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "TCP协议客户端");
        model.addAttribute("version", "2.0.0");
        model.addAttribute("httpPort", httpPort);
        model.addAttribute("serverHost", clientConfigService.getTcpServerHost());
        model.addAttribute("serverPort", clientConfigService.getTcpServerPort());
        model.addAttribute("protocol", "Custom TCP Protocol (header+length+data)");
        
        return "home";
    }
}
