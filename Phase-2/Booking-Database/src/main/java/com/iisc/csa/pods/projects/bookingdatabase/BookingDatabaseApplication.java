package com.iisc.csa.pods.projects.bookingdatabase;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.h2.tools.Server;

import java.sql.SQLException;


@SpringBootApplication
public class BookingDatabaseApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        startH2Server();
        SpringApplication.run(BookingDatabaseApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        startH2Server();
        return application.sources(BookingDatabaseApplication.class);
    }
    /**
     * Method to start H2 server on 8084 port for Booking Service to connect to.
     */
    private static void startH2Server() {
        try {
            Server h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "8084").start();
            if (h2Server.isRunning(true)) {
                System.out.println("H2 server was started and is running.");
            } else {
                throw new RuntimeException("Could not start H2 server.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to start H2 server: ", e);
        }
    }
}
