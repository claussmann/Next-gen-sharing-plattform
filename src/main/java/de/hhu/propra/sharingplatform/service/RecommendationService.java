package de.hhu.propra.sharingplatform.service;

import de.hhu.propra.sharingplatform.dao.ItemRentalRepo;
import de.hhu.propra.sharingplatform.dao.ItemRepo;
import de.hhu.propra.sharingplatform.dao.contractdao.BorrowContractRepo;
import de.hhu.propra.sharingplatform.model.User;
import de.hhu.propra.sharingplatform.model.contracts.BorrowContract;
import de.hhu.propra.sharingplatform.model.items.Item;

import java.util.*;
import java.util.Map.Entry;

import de.hhu.propra.sharingplatform.model.items.ItemRental;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
public class RecommendationService {

    private final BorrowContractRepo borrowContractRepo;

    private final ItemRepo itemRepo;

    private final ItemRentalRepo itemRentalRepo;

    private int numberOfItems;

    @Autowired
    public RecommendationService(BorrowContractRepo borrowContractRepo, ItemRepo itemRepo,
                                 ItemRentalRepo itemRentalRepo) {
        this.borrowContractRepo = borrowContractRepo;
        this.itemRepo = itemRepo;
        this.numberOfItems = 3;
        this.itemRentalRepo = itemRentalRepo;
    }

    /**
     * uses the users who bought x also bought y schema.
     *
     * @param itemId Item to find recommendations for
     * @return returns list of Items
     */

    public List<Item> findRecommendations(long itemId) {
        List<Item> list = itemRepo.findAllByDeletedIsFalse();
        List<Item> ret = new ArrayList<>();

        Random random = new Random();

        ret.add(list.get(random.nextInt(list.size())));
        ret.add(list.get(random.nextInt(list.size())));
        ret.add(list.get(random.nextInt(list.size())));

        return ret;

        //TODO: Fix this:
        /*Item item = (Item) itemRepo.findById(itemId).get();
        List<BorrowContract> contracts = borrowContractRepo.findAllByItem(item);
        List<User> otherBorrowers = findOtherBorrowers(contracts);
        Map<Item, Integer> map = fillMap(otherBorrowers);

        return findBestItems(map, itemId);*/
    }

    /**
     * Looks for the best matches. If not enough are available by K-nearest neighbours random ones
     * will be filled
     *
     * @param map to read Items and values from
     * @return array List of best suggestions
     */

    private List<Item> findBestItems(Map<Item, Integer> map, long itemId) {

        List<Entry<Item, Integer>> entrys = findGreatest(map);
        List<Item> suggestions = new ArrayList<>();

        for (Entry<Item, Integer> entry : entrys) {
            if (entry.getKey().getId() != itemId) {
                suggestions.add(entry.getKey());
            }
        }

        while (suggestions.size() > numberOfItems) {
            suggestions.remove(0);
        }

        return suggestions.size() < numberOfItems ? fillList(suggestions) : suggestions;
    }

    private List<Item> fillList(List<Item> suggestions) {
        List<ItemRental> allItems = (List<ItemRental>) itemRentalRepo.findAll();
        Random random = new Random();
        while (suggestions.size() < numberOfItems) {
            int index = random.nextInt() & Integer.MAX_VALUE; // zero out the sign bit
            index %= allItems.size(); // get index in range
            ItemRental randomSuggestion = allItems.get(index);
            if (!suggestions.contains(randomSuggestion)) {
                suggestions.add(randomSuggestion);
            }
        }
        return suggestions;
    }

    private Map<Item, Integer> fillMap(List<User> otherBorrowers) {
        // Maps the Items with the values of their frequency
        Map<Item, Integer> map = new HashMap<>();
        for (User otherBorrower : otherBorrowers) {
            List<Item> borrowedItems = findBorrowedItems(otherBorrower.getId());
            putBorrowedItems(map, borrowedItems);
        }
        return map;
    }

    List<Item> findBorrowedItems(long userId) {
        List<BorrowContract> allContracts = (List<BorrowContract>) borrowContractRepo.findAll();
        List<Item> items = new ArrayList<>();

        for (BorrowContract contract : allContracts) {
            if (contract.getBorrower().getId() == userId) {
                items.add(contract.getItem());
            }
        }
        return items;
    }

    private void putBorrowedItems(Map<Item, Integer> map,
                                  List<Item> borrowedItems) {
        for (Item borrowedItem : borrowedItems) {
            map.put(borrowedItem, map.getOrDefault(borrowedItem, 1));
        }
    }

    private List<User> findOtherBorrowers(List<BorrowContract> contracts) {
        List<User> otherBorrowers = new ArrayList<>();
        for (BorrowContract contract : contracts) {
            otherBorrowers.add(contract.getBorrower());
        }
        return otherBorrowers;
    }

    /**
     * Thank you stackoverflow.
     *
     * @param map map to find highest values of
     * @return numberOfItem recommendations
     */
    <K, V extends Comparable<? super V>> List<Entry<K, V>> findGreatest(Map<K, V> map) {
        int suggestionCount = numberOfItems + 1;
        Comparator<? super Entry<K, V>> comparator = (Comparator<Entry<K, V>>) (e0, e1) -> {
            V v0 = e0.getValue();
            V v1 = e1.getValue();
            return v0.compareTo(v1);
        };
        PriorityQueue<Entry<K, V>> highest = new PriorityQueue<>(suggestionCount, comparator);
        for (Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > suggestionCount) {
                highest.poll();
            }
        }

        List<Entry<K, V>> result = new ArrayList<>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }
}
