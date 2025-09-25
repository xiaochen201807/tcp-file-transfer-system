package com.example.tcpserver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 */
@Controller
public class HomeController {
    
    @Value("${tcp.server.port:8888}")
    private int tcpPort;
    
    @Value("${server.port:8080}")
    private int httpPort;
    
    /**
     * 首页 - 返回简单的HTML页面
     */
    @GetMapping("/")
    @ResponseBody
    public String home() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>TCP File Transfer Server</title>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append("h1 { color: #333; text-align: center; }");
        html.append(".info { background: #e8f4fd; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        html.append(".api-list { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        html.append(".api-item { margin: 10px 0; }");
        html.append(".api-item a { color: #007bff; text-decoration: none; }");
        html.append(".api-item a:hover { text-decoration: underline; }");
        html.append(".status { display: inline-block; background: #28a745; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"container\">");
        html.append("<h1>TCP Protocol Server</h1>");

        html.append("<div class=\"info\">");
        html.append("<h3>服务器信息</h3>");
        html.append("<p><strong>TCP服务端口:</strong> ").append(tcpPort).append(" <span class=\"status\">运行中</span></p>");
        html.append("<p><strong>HTTP管理端口:</strong> ").append(httpPort).append(" <span class=\"status\">运行中</span></p>");
        html.append("<p><strong>协议:</strong> 自定义TCP协议 (header+length+data) + HTTP REST API</p>");
        html.append("</div>");

        html.append("<div class=\"api-list\">");
        html.append("<h3>管理接口</h3>");
        html.append("<div class=\"api-item\">");
        html.append("<strong>服务器状态:</strong> ");
        html.append("<a href=\"/admin/status\" target=\"_blank\">/admin/status</a>");
        html.append("</div>");
        html.append("<div class=\"api-item\">");
        html.append("<strong>连接的客户端:</strong> ");
        html.append("<a href=\"/admin/clients\" target=\"_blank\">/admin/clients</a>");
        html.append("</div>");
        html.append("<div class=\"api-item\">");
        html.append("<strong>协议统计:</strong> ");
        html.append("<a href=\"/admin/protocol/stats\" target=\"_blank\">/admin/protocol/stats</a>");
        html.append("</div>");
        html.append("<div class=\"api-item\">");
        html.append("<strong>文件统计:</strong> ");
        html.append("<a href=\"/admin/files/stats\" target=\"_blank\">/admin/files/stats</a>");
        html.append("</div>");
        html.append("<div class=\"api-item\">");
        html.append("<strong>模拟场景:</strong> ");
        html.append("<a href=\"/api/simulation/scenarios\" target=\"_blank\">/api/simulation/scenarios</a>");
        html.append("</div>");
        html.append("<div class=\"api-item\">");
        html.append("<strong>健康检查:</strong> ");
        html.append("<a href=\"/admin/health\" target=\"_blank\">/admin/health</a>");
        html.append("</div>");
        html.append("<div class=\"api-item\">");
        html.append("<strong>系统信息:</strong> ");
        html.append("<a href=\"/admin/system\" target=\"_blank\">/admin/system</a>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class=\"info\">");
        html.append("<h3>使用说明</h3>");
        html.append("<p>1. TCP客户端可以连接到端口 ").append(tcpPort).append(" 进行协议通信</p>");
        html.append("<p>2. 支持签到交易和业务交易两种类型</p>");
        html.append("<p>3. 点击上面的链接查看服务器状态和统计信息</p>");
        html.append("<p>4. 支持配置文件驱动的模拟场景测试</p>");
        html.append("<p>5. 文件存储在 <code>files/</code> 目录中</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
    
    /**
     * API信息
     */
    @GetMapping("/api/info")
    @ResponseBody
    public Map<String, Object> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serverName", "TCP Protocol Server");
        info.put("version", "2.0.0");
        info.put("tcpPort", tcpPort);
        info.put("httpPort", httpPort);
        info.put("endpoints", Map.of(
            "status", "/admin/status",
            "clients", "/admin/clients", 
            "protocolStats", "/admin/protocol/stats",
            "fileStats", "/admin/files/stats",
            "simulation", "/api/simulation/scenarios",
            "health", "/admin/health",
            "system", "/admin/system"
        ));
        return info;
    }
}
