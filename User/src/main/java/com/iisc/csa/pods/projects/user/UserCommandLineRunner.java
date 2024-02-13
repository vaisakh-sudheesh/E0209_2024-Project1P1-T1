package com.iisc.csa.pods.projects.user;

import com.iisc.csa.pods.projects.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserCommandLineRunner implements CommandLineRunner {
    private final UserRepository userrepo;

    public UserCommandLineRunner(UserRepository userrep){
        this.userrepo = userrep;
    }

    @Override
    public void run(String ... args) throws Exception {
//        System.out.println("Populating database");
//        UserData user = new UserData("test","test@gmail.com");
//        this.userrepo.save(user);
    }
}
