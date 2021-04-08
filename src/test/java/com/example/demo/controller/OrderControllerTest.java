package com.example.demo.controller;

import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.utils.TestUtils;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.utils.TestUtils.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<UserOrder> json;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private CartRepository cartRepository;


    private User createUserCart() {
        User user = createUser();
        user.setCart(TestUtils.createCart());
        user.getCart().setUser(user);
        return user;
    }

    @Test
    public void shouldSubmitOrder() throws Exception {
        User user = createUserCart();
        when(userRepository.findByUsername(userNameMock)).thenReturn(user);

        mvc.perform(post(new URI("/api/order/submit/" + userNameMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(user.getCart().getTotal()))
                .andDo(print());

        verify(userRepository, times(1)).findByUsername(userNameMock);
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    public void shouldFailToSubmit_userNotFound() throws Exception {
        String badUsername = "badUserName";
        mvc.perform(post(new URI("/api/order/submit/" + badUsername)))
                .andExpect(status().isNotFound())
                .andDo(print());
        verify(userRepository, times(1)).findByUsername(badUsername);
        verify(orderRepository, times(0)).save(any());
    }


    @Test
    public void shouldGetOrdersByUsername() throws Exception {
        User user = createUserCart();
        List<UserOrder> userOrders = new ArrayList<>();
        userOrders.add(UserOrder.createFromCart(user.getCart()));

        when(orderRepository.findByUser(user)).thenReturn(userOrders);
        when(userRepository.findByUsername(userNameMock)).thenReturn(user);
        mvc.perform(get(new URI("/api/order/history/" + userNameMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user.id").value(user.getId()))
                .andExpect(jsonPath("$[0].user.username").value(user.getUsername()))
                .andExpect(jsonPath("$[0].total").value(user.getCart().getTotal()))
                .andDo(print());

        verify(userRepository, times(1)).findByUsername(userNameMock);
        verify(orderRepository, times(1)).findByUser(user);
    }
}
