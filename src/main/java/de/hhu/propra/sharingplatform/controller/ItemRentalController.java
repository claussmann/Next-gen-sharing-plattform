package de.hhu.propra.sharingplatform.controller;

import de.hhu.propra.sharingplatform.model.User;
import de.hhu.propra.sharingplatform.model.items.ItemRental;
import de.hhu.propra.sharingplatform.service.ItemService;
import de.hhu.propra.sharingplatform.service.OfferService;
import de.hhu.propra.sharingplatform.service.RecommendationService;
import de.hhu.propra.sharingplatform.service.UserService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ItemRentalController extends BaseController {

    private final ItemService itemService;
    private final OfferService offerService;
    private final RecommendationService recommendationService;


    @Autowired
    public ItemRentalController(ItemService itemService, OfferService offerService,
        UserService userService, RecommendationService recommendationService) {
        super(userService);
        this.itemService = itemService;
        this.offerService = offerService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/item/rental/details/{itemId}")
    public String detailPage(Model model, @PathVariable long itemId, Principal principal) {
        ItemRental itemRental = (ItemRental) itemService.findItem(itemId);
        model.addAttribute("itemRental", itemRental);
        model.addAttribute("user", userService.fetchUserByAccountName(principal.getName()));
        boolean ownItem = itemService.userIsOwner(itemRental.getId(),
            userService.fetchUserIdByAccountName(principal.getName()));
        model.addAttribute("ownItem", ownItem);
        model.addAttribute("recItems", recommendationService.findRecommendations(itemId));
        return "itemDetails";
    }

    @GetMapping("/item/rental/new")
    public String newItem(Model model, Principal principal) {
        User user = userService.fetchUserByAccountName(principal.getName());
        model.addAttribute("itemRental", new ItemRental(user));
        model.addAttribute("user", user);
        return "itemRentalForm";
    }

    @PostMapping("/item/rental/new")
    public String inputItemData(Model model, ItemRental itemRental, Principal principal) {
        itemService
            .persistItem(itemRental, userService.fetchUserIdByAccountName(principal.getName()));
        return "redirect:/user/account/";
    }

    @PostMapping("/item/rental/remove/{itemId}")
    public String markItemAsRemoved(Model model, @PathVariable long itemId,
        Principal principal) {
        offerService.removeOffersFromDeletedItem(itemId);
        itemService.removeItem(itemId, userService.fetchUserIdByAccountName(principal.getName()));
        return "redirect:/user/account/";
    }

    @GetMapping("/item/rental/edit/{itemId}")
    public String editItem(Model model, @PathVariable long itemId, Principal principal) {
        ItemRental itemRental = (ItemRental) itemService.findItem(itemId);
        model.addAttribute("itemRental", itemRental);
        model.addAttribute("itemId", itemId);
        long userId = userService.fetchUserIdByAccountName(principal.getName());
        model.addAttribute("userId", userId);
        itemService.allowOnlyOwner(itemRental, userId);
        itemService.itemIsFree(itemId);
        return "itemRentalForm";
    }

    @PostMapping("/item/rental/edit/{itemId}")
    public String editItemData(Model model, ItemRental itemRental,
        @PathVariable long itemId,
        Principal principal) {
        long userId = userService.fetchUserIdByAccountName(principal.getName());
        itemService.editItem(itemRental, itemId, userId);
        return "redirect:/user/account";
    }
}
