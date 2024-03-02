package com.iisc.csa.pods.projects.bookingdatabase;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.h2.tools.Server;

import java.sql.SQLException;


@SpringBootApplication
public class BookingDatabaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingDatabaseApplication.class, args);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server inMemoryH2DatabaseServer() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "8084");
    }
}
