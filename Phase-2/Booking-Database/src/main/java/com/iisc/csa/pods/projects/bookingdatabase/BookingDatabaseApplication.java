package com.iisc.csa.pods.projects.bookingdatabase;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.h2.tools.Server;

import java.sql.SQLException;
import jakarta.annotation.PostConstruct;


@SpringBootApplication
public class BookingDatabaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingDatabaseApplication.class, args);
    }

    @Value("${PODSPROJECT_BOOKINGDB_SERV_PORT_H2JPA:8083}")
    private String jpaPort;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server inMemoryH2DatabaseServer() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", jpaPort);
    }
}
