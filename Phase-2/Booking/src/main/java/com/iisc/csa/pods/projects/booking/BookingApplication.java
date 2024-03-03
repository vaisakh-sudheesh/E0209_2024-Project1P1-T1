package com.iisc.csa.pods.projects.booking;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.support.DatabaseStartupValidator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.stream.Stream;

@SpringBootApplication
@EnableTransactionManagement
public class BookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingApplication.class, args);
	}

	/**
	 * Bean to wait for Booking Database service to be up and running.
	 *
	 * This will help ensure that there is a delay from in case the
	 * Booking-Database service takes sometime to initialize.
	 *
	 * @param dataSource
	 * @return DatabaseStartupValidator instance
	 */
	@Bean
	public DatabaseStartupValidator databaseStartupValidator (DataSource dataSource){
		var dsv = new DatabaseStartupValidator();
		dsv.setDataSource(dataSource);
		dsv.setValidationQuery(DatabaseDriver.H2.getValidationQuery());
		return dsv;
	}

	/**
	 * Bean to make use of DatabaseStartupValidator to wait until Booking-Database
	 * instance is ready to connect and service calls.
	 * @Beann
	 */
	@Bean
	public static BeanFactoryPostProcessor dependsOnPostProcessor() {
		return bf -> {
			String[] jpa = bf.getBeanNamesForType(EntityManagerFactory.class);
			Stream.of(jpa).map(bf::getBeanDefinition).forEach(it -> it.setDependsOn("databaseStartupValidator"));
		};
	}
}
