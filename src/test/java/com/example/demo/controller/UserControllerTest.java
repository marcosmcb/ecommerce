package com.example.demo.controller;

import static com.example.demo.utils.TestUtils.createUser;
import static com.example.demo.utils.TestUtils.userIdMock;
import static com.example.demo.utils.TestUtils.userNameMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations="classpath:application.properties")
@AutoConfigureJsonTesters
public class UserControllerTest {


    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<CreateUserRequest> json;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Test
    public void shouldNotFindUserById() throws Exception {
        long badUserId = 123456L;
        mvc.perform(get( new URI("/api/user/id/" + badUserId)))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(badUserId);
        verify(userRepository, times(0)).findByUsername(any());
    }

    @Test
    public void shouldFindUserById() throws Exception {
        when(userRepository.findById(userIdMock)).thenReturn(Optional.of(createUser()));

        mvc.perform(get(new URI("/api/user/id/" + userIdMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userIdMock))
                .andExpect(jsonPath("$.username").value(userNameMock))
                .andDo(print());

        verify(userRepository, times(1)).findById(userIdMock);
        verify(userRepository, times(0)).findByUsername(any());
    }

    @Test
    public void shouldNotFindUserByName() throws Exception {
        String badUsername = "bad";
        mvc.perform(get(new URI("/api/user/" + badUsername)))
                .andExpect(status().isNotFound());

        verify(userRepository, times(0)).findById(any());
        verify(userRepository, times(1)).findByUsername(badUsername);
    }

    @Test
    public void shouldFindUserByName() throws Exception {
        when(userRepository.findByUsername(userNameMock)).thenReturn(createUser());

        mvc.perform(get( new URI("/api/user/" + userNameMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userIdMock))
                .andExpect(jsonPath("$.username").value(userNameMock))
                .andDo(print());

        verify(userRepository, times(0)).findById(any());
        verify(userRepository, times(1)).findByUsername(userNameMock);
    }


    @Test
    public void shouldCreateUser() throws Exception {
        CreateUserRequest user = createUserRequest();
        mvc.perform(
                post(new URI("/api/user/create"))
                    .content(json.write(user).getJson())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.username").value(userNameMock))
                .andDo(print());

        verify(userRepository, times(0)).findById(any());
        verify(userRepository, times(0)).findByUsername(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void shouldFailValidatePassword_notMatchingConfirmPassword() throws Exception {
        CreateUserRequest user = createUserRequest();
        user.setConfirmPassword("123456");

        mvc.perform(
                post(new URI("/api/user/create"))
                        .content(json.write(user).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(userRepository, times(0)).save(any());
    }

    @Test
    public void shouldFailValidatePassword_invalidLength() throws Exception {
        CreateUserRequest user = createUserRequest();
        user.setPassword("123");
        user.setConfirmPassword("123");

        mvc.perform(
                post(new URI("/api/user/create"))
                        .content(json.write(user).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
        verify(userRepository, times(0)).save(any());
    }


    private CreateUserRequest createUserRequest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setUsername(userNameMock);
        user.setPassword("12345678");
        user.setConfirmPassword("12345678");
        return user;
    }
}
