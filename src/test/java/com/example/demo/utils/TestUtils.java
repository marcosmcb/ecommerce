package com.example.demo.utils;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.ModifyCartRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestUtils {

    public static final long userIdMock = 1010L;
    public static final String userNameMock = "Michael";

    /**
     *  Creates an example User
     *  @return an example User
     */
    public static User createUser() {
        User user = new User();
        user.setId(userIdMock);
        user.setUsername(userNameMock);
        user.setPassword("12345678");
        user.setCart(createCart());
        user.getCart().setItems(null);
        user.getCart().setUser(user);
        return user;
    }

    public static Cart createCart() {
        Cart cart = new Cart();
        cart.setId(0L);
        cart.setTotal(new BigDecimal(30.00));

        List<Item> items = IntStream.range(0, 10).mapToObj(i -> createItem(Long.valueOf(i))).collect(Collectors.toList());
        cart.setItems(items);
        return cart;
    }

    public static Item createItem(long id) {
        Item item = new Item();
        item.setId(id);
        item.setName("Product" + id);
        item.setPrice(new BigDecimal(2*id+10));
        return item;
    }

    public static ModifyCartRequest createModifyCart(long id, int quantity) {
        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setItemId(id);
        modifyCartRequest.setUsername(userNameMock);
        modifyCartRequest.setQuantity(quantity);
        return modifyCartRequest;
    }

}
