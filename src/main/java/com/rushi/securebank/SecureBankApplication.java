package com.rushi.securebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ================================================================
 *  SecureBank Application Main Class - JAR Edition
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  This is the standard Spring Boot main class. It uses an
 *  EMBEDDED Tomcat server, so the JAR runs as a standalone app:
 *
 *      java -jar target/securebank.jar
 *
 *  No external server needed. App starts on port 8080 (default).
 * ================================================================
 */
@SpringBootApplication
public class SecureBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureBankApplication.class, args);

        System.out.println();
        System.out.println("============================================================");
        System.out.println("   SecureBank (JAR)  -  Banking by Rushi                   ");
        System.out.println("------------------------------------------------------------");
        System.out.println("   Standalone Spring Boot application with embedded Tomcat ");
        System.out.println("   Open in browser: http://localhost:8080                  ");
        System.out.println("   Press Ctrl + C to stop                                   ");
        System.out.println("============================================================");
        System.out.println();
    }
}
