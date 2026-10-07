package com.apexf1.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(BackendApplication.class, args);

        // Print a clickable link in the terminal (VS Code turns URLs into links)
        Environment env = ctx.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String url = "http://localhost:" + port;
        System.out.println();
        System.out.println("========================================================");
        System.out.println("  APEX F1 is running!  Open:  " + url);
        System.out.println("  Dashboard : " + url + "/dashboard.html");
        System.out.println("  Pit Wall  : " + url + "/pitwall.html");
        System.out.println("  FSP       : " + url + "/fsp.html");
        System.out.println("  Manage    : " + url + "/manage.html");
        System.out.println("========================================================");
        System.out.println();
    }
}
