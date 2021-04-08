package com.example.demo.controller;


import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.ItemRepository;
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
import java.util.Optional;

import static com.example.demo.utils.TestUtils.createItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations="classpath:application.properties")
@AutoConfigureJsonTesters
public class ItemControllerTest {

    private static final long itemIdMock = 20L;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<UserOrder> json;

    @MockBean
    private ItemRepository itemRepository;


    @Test
    public void shouldGetItemById() throws Exception {
        Optional<Item> item = Optional.of(createItem(itemIdMock));
        when(itemRepository.findById(itemIdMock)).thenReturn(item);

        mvc.perform(get(new URI("/api/item/" + itemIdMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.get().getId()))
                .andExpect(jsonPath("$.name").value(item.get().getName()))
                .andDo(print());

        verify(itemRepository, times(1)).findById(itemIdMock);
    }

    @Test
    public void shouldNotFindItemById() throws Exception {
        long badItemId = 101010L;
        mvc.perform(get(new URI("/api/item/" + badItemId)))
                .andExpect(status().isNotFound())
                .andDo(print());

        verify(itemRepository, times(1)).findById(badItemId);
    }

    @Test
    public void shouldFindItemByName() throws Exception {
        Item item = createItem(100L);
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(itemRepository.findByName(item.getName())).thenReturn(items);
        mvc.perform(get(new URI("/api/item/name/" + item.getName())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(item.getId()))
                .andExpect(jsonPath("$[0].name").value(item.getName()))
                .andExpect(jsonPath("$[0].price").value(item.getPrice()))
                .andDo(print());

        verify(itemRepository, times(1)).findByName(item.getName());
    }

    @Test
    public void shouldNotFindItemByName() throws Exception {
        String badItemName = "badItemName";
        mvc.perform(get(new URI("/api/item/name/" + badItemName)))
                .andExpect(status().isNotFound())
                .andDo(print());

        verify(itemRepository, times(1)).findByName(badItemName);
    }
}
