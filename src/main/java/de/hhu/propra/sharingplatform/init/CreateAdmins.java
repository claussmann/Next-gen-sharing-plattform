package de.hhu.propra.sharingplatform.init;

import de.hhu.propra.sharingplatform.dao.UserRepo;
import de.hhu.propra.sharingplatform.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import java.util.logging.Logger;

@Configuration
public class CreateAdmins {

    @Autowired
    private UserRepo userRepo;

    private Logger log = Logger.getLogger(CreateAdmins.class.getName());

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        if (userRepo.count() != 0) {
            log.info("Admin already present.");
            return;
        }
        User user = new User();
        user.setName("System Administrator");
        user.setAddress("NA");
        user.setAccountName("admin");
        user.setEmail("admin@example.com");
        user.setPropayId("adminpropay");
        user.setRole("admin");
        user.setBan(false);
        user.setDeleted(false);
        user.setPassword("admin");

        userRepo.save(user);

        log.info("Created admin account");
    }
}
