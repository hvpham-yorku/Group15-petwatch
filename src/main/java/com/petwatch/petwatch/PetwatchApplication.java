package com.petwatch.petwatch;

import com.petwatch.petwatch.DAO.BookingDAO;
import com.petwatch.petwatch.DAO.PetOwnerDAO;
import com.petwatch.petwatch.DAO.PetSitterDAO;
import com.petwatch.petwatch.DAO.UserDAO;
import com.petwatch.petwatch.Model.Booking;
import com.petwatch.petwatch.Model.PetOwner;
import com.petwatch.petwatch.Model.PetSitter;
import com.petwatch.petwatch.Model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;



@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, SecurityAutoConfiguration.class })
public class PetwatchApplication {

	public static void main(String[] args) {

		SpringApplication.run(PetwatchApplication.class, args);

	}

}


