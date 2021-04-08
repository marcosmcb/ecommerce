package com.example.demo.controller;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.Optional;

import static com.example.demo.utils.TestUtils.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations="classpath:application.properties")
@AutoConfigureJsonTesters
public class CartControllerTest {


    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<ModifyCartRequest> json;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void shouldAddItemToCart() throws Exception {
        ModifyCartRequest cartRequest = createModifyCart(10L, 10);
        User user = createUser();
        Item item = createItem(10L);
        when(userRepository.findByUsername(userNameMock)).thenReturn(user);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        mvc.perform(
                post(new URI("/api/cart/addToCart"))
                        .content(json.write(cartRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.username").value(user.getUsername()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(cartRequest.getQuantity())))
                .andDo(print());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(cartRepository, times(1)).save(any());
    }

    @Test
    public void shouldNotFindUserByName() throws Exception {
        ModifyCartRequest cartRequest = createModifyCart(10L, 10);

        mvc.perform(
                post(new URI("/api/cart/addToCart"))
                        .content(json.write(cartRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(itemRepository, times(0)).findById(anyLong());
        verify(cartRepository, times(0)).save(any());
    }

    @Test
    public void shouldNotFindItemById() throws Exception {
        ModifyCartRequest cartRequest = createModifyCart(10L, 10);
        User user = createUser();
        when(userRepository.findByUsername(userNameMock)).thenReturn(user);

        mvc.perform(
                post(new URI("/api/cart/addToCart"))
                        .content(json.write(cartRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(cartRepository, times(0)).save(any());
    }


    @Test
    public void shouldRemoveItemFromCart() throws Exception {
        ModifyCartRequest addToCartRequest = createModifyCart(10L, 10);
        User user = createUser();
        Item item = createItem(10L);
        when(userRepository.findByUsername(userNameMock)).thenReturn(user);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        mvc.perform(
                post(new URI("/api/cart/addToCart"))
                        .content(json.write(addToCartRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        ModifyCartRequest removeFromCartRequest = createModifyCart(10L, 3);
        mvc.perform(
                post(new URI("/api/cart/removeFromCart"))
                        .content(json.write(removeFromCartRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.username").value(user.getUsername()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(addToCartRequest.getQuantity() - removeFromCartRequest.getQuantity())))
                .andDo(print());

        verify(userRepository, times(2)).findByUsername(user.getUsername());
        verify(itemRepository, times(2)).findById(item.getId());
        verify(cartRepository, times(2)).save(any());
    }


}
