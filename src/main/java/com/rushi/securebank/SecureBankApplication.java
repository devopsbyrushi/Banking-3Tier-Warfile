package com.rushi.securebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * ================================================================
 * SecureBank Application
 * Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 * WAR Deployment Version
 *
 * This application is configured to run in:
 *
 * ✔ External Apache Tomcat
 * ✔ Docker Container
 * ✔ Kubernetes (GKE)
 *
 * ================================================================
 */

@SpringBootApplication
public class SecureBankApplication extends SpringBootServletInitializer {

    /**
     * Required when deploying as WAR to external Tomcat
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SecureBankApplication.class);
    }

    /**
     * Main method (used only when running locally)
     */
    public static void main(String[] args) {

        SpringApplication.run(SecureBankApplication.class, args);

        System.out.println();
        System.out.println("======================================================");
        System.out.println("        SecureBank Application Started");
        System.out.println("======================================================");
        System.out.println(" Environment : Docker / Kubernetes");
        System.out.println(" Server      : Apache Tomcat 10");
        System.out.println(" Port        : 8080");
        System.out.println("======================================================");
        System.out.println();

    }
}
