package de.hhu.propra.sharingplatform.faker;

import com.github.javafaker.Faker;
import de.hhu.propra.sharingplatform.dao.ItemRepo;
import de.hhu.propra.sharingplatform.dao.OfferRepo;
import de.hhu.propra.sharingplatform.dao.UserRepo;
import de.hhu.propra.sharingplatform.model.Item;
import de.hhu.propra.sharingplatform.model.Offer;
import de.hhu.propra.sharingplatform.model.User;
import de.hhu.propra.sharingplatform.service.OfferService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataFaker implements ServletContextInitializer {

    private final Environment env;

    private final UserRepo userRepo;

    private final ItemRepo itemRepo;

    private final OfferRepo offerRepo;

    private final OfferService offerService;

    private Logger log = Logger.getLogger(DataFaker.class.getName());

    private Faker faker;

    @Autowired
    public DataFaker(Environment env, UserRepo userRepo, ItemRepo itemRepo,
        OfferRepo offerRepo, OfferService offerService) {
        this.env = env;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.offerRepo = offerRepo;
        this.offerService = offerService;
        Random rnd = new Random();
        rnd.setSeed(1337);
        this.faker = new Faker(Locale.ENGLISH, rnd);
    }

    public DataFaker(long seed, Environment env, UserRepo userRepo, ItemRepo itemRepo,
        OfferRepo offerRepo, OfferService offerService) {
        this.env = env;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.offerRepo = offerRepo;
        this.offerService = offerService;
        Random rnd = new Random();
        rnd.setSeed(seed);
        this.faker = new Faker(Locale.ENGLISH, rnd);
    }

    @Override
    @Transactional
    public void onStartup(ServletContext servletContext) {
        int dataSize = 100;

        log.info("Generating Database");
        UserFaker userFaker = new UserFaker(faker);
        ItemFaker itemFaker = new ItemFaker(faker);

        log.info("    Creating User...");
        List<User> users = new ArrayList<>();
        userFaker.createUsers(users, dataSize / 5);

        log.info("    Creating Items...");
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < (dataSize / 8); i++) {
            User user = getRandomUser(users);
            itemFaker.createItems(items, user, dataSize / 15);
        }

        log.info("    Persist Items...");
        persistItem(items);
        log.info("    Persist Users...");
        persistUser(users);

        log.info("    Creating Offers...");
        for (int i = 0; i < (dataSize / 3); i++) {
            User user = getRandomUser(users);
            Item item = getRandomItem(items);

            if (item.getOwner().getId() != user.getId()) {
                offerService.create(item.getId(), user,
                    LocalDateTime.of(30, 2, 12, 12, 56),
                    LocalDateTime.of(30, 3, 10, 13, 43));

            } else {
                i--;
            }
        }

        List<Offer> offers = (List<Offer>) offerRepo.findAll();
        for (int i = 0; i < (dataSize / 6); i++) {
            Offer offer = getRandomOffer(offers);
            if (!(offer.isAccept() || offer.isDecline())) {
                if (faker.number().numberBetween(0, 1) == 1) {
                    offerService.acceptOffer(offer.getId(), offer.getItem().getOwner());
                } else {
                    offerService.declineOffer(offer.getId(), offer.getItem().getOwner());
                }
            }
            else{
                i--;
            }
        }

        log.info("Done!");
    }

    private void persistUser(List<User> users) {
        for (User user : users) {
            userRepo.save(user);
        }
    }

    private void persistItem(List<Item> items) {
        for (Item item : items) {
            itemRepo.save(item);
        }
    }

    private User getRandomUser(List<User> users) {
        return users.get(faker.number().numberBetween(0, users.size() - 1));
    }

    private Item getRandomItem(List<Item> items) {
        return items.get(faker.number().numberBetween(0, items.size() - 1));
    }

    private Offer getRandomOffer(List<Offer> offers) {
        return offers.get(faker.number().numberBetween(0, offers.size() - 1));
    }
}
