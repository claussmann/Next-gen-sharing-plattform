package de.hhu.propra.sharingplatform.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hhu.propra.sharingplatform.dao.ItemRentalRepo;
import de.hhu.propra.sharingplatform.dao.ItemRepo;
import de.hhu.propra.sharingplatform.dao.contractdao.BorrowContractRepo;
import de.hhu.propra.sharingplatform.model.Offer;
import de.hhu.propra.sharingplatform.model.User;
import de.hhu.propra.sharingplatform.model.contracts.BorrowContract;
import de.hhu.propra.sharingplatform.model.items.Item;
import de.hhu.propra.sharingplatform.model.items.ItemRental;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class RecommendationServiceTest {

    @MockBean
    BorrowContractRepo borrowContractRepo;

    @MockBean
    ItemRepo itemRepo;

    @MockBean
    ItemRentalRepo itemRentalRepo;

    private RecommendationService recommendationService;

    @Before
    public void setUp() {
        this.recommendationService = new RecommendationService(borrowContractRepo, itemRepo,
            itemRentalRepo);
    }

    @Ignore
    @Test
    public void recommendsThreeItems() {
        when(itemRepo.findById(anyLong())).thenReturn(Optional.of(mock(ItemRental.class)));
        List<BorrowContract> contracts = createFakeContracts();
        when(borrowContractRepo.findAllByItem(any())).thenReturn(contracts);
        List<ItemRental> allItems = createFakeItems();
        when(itemRentalRepo.findAll()).thenReturn(allItems);

        assertEquals(3, recommendationService.findRecommendations(1337L).size());
    }

    //TODO returns 3 items tests

    @Test
    public void findBorrowedItem() {
        List<BorrowContract> contracts = createFakeContracts();
        when(borrowContractRepo.findAll()).thenReturn(contracts);
        List<Item> items = recommendationService.findBorrowedItems(5L);

        assertEquals(1, items.size());
        assertEquals(contracts.get(5).getItem(), items.get(0));
    }


    @Test
    public void findGreatest() {
        this.recommendationService.setNumberOfItems(1);
        Map<Item, Integer> map = new HashMap<>();
        Item itemOne = new ItemRental(mock(User.class));
        itemOne.setId(1337L);
        Item itemTwo = new ItemRental(mock(User.class));
        itemTwo.setId(7331L);
        map.put(itemOne, 10);
        map.put(itemTwo, 2);

        assertEquals(1, recommendationService.findGreatest(map).size());
        assertEquals(itemOne, recommendationService.findGreatest(map).get(0).getKey());
    }

    private List<User> createFakerUser() {
        List<User> users = new ArrayList<>();
        for (long i = 0; i < 20; i++) {
            User user = new User();
            user.setId(i);
            users.add(user);
        }
        return users;
    }

    public List<ItemRental> createFakeItems() {
        List<User> users = createFakerUser();
        List<ItemRental> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ItemRental item = new ItemRental(users.get(i));
            item.setId((long) i);
            item.setBail(10 * i);
            items.add(item);
        }
        return items;
    }

    public List<BorrowContract> createFakeContracts() {
        List<User> users = createFakerUser();
        List<ItemRental> items = createFakeItems();
        List<BorrowContract> contracts = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusDays(3);
            Offer offer = new Offer(items.get(i), users.get(i), start, end);
            BorrowContract contract = new BorrowContract(offer);
            contracts.add(contract);
        }

        for (int i = 0; i < 4; i++) {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusDays(3);
            Offer offer = new Offer(items.get(i), users.get(4 - i), start, end);
            BorrowContract contract = new BorrowContract(offer);
            contracts.add(contract);
        }

        return contracts;
    }
}