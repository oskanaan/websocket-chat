package com.thebinaryheap.chat.server.rest.controller;

import com.thebinaryheap.chat.data.repo.UserRepositoryRest;
import com.thebinaryheap.chat.data.repo.documents.ChatHistory;
import com.thebinaryheap.chat.data.repo.documents.User;
import com.thebinaryheap.chat.server.ChatWebSocketConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
//@ContextConfiguration(classes={WebConfig.class, ChatWebSocketConfig.class})
public class TestUserController {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private UserRepositoryRest userRepository;

  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  private List<User> repoUsers;

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {

    this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
        .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
        .findAny()
        .orElse(null);

    assertNotNull("the JSON message converter must not be null",
        this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setUp(){
    repoUsers = new ArrayList<>();
    when(userRepository.save(any(User.class))).then((Answer<Void>) invocationOnMock -> {
      User user = (User) invocationOnMock.getArguments()[0];
      for(User repoUser : repoUsers){
        if(repoUser.getUsername().equals(user.getUsername())){
          repoUsers.remove(repoUser);
          repoUsers.add(user);
          return null;
        }
      }
      repoUsers.add(user);
      return null;
    });
    when(userRepository.findAll()).thenReturn(repoUsers);
    when(userRepository.findByUsername(any())).thenAnswer((Answer<User>) invocationOnMock -> {
      for(User user : repoUsers){
        if(user.getUsername().equals(invocationOnMock.getArguments()[0])){
          return user;
        }
      }
      return null;
    });
  }

  @Test
  public void testPutMethod() throws Exception {
    User testUser = new User();
    testUser.setUsername("user123!");

    mvc.perform(put("/V1/users")
                   .content(json(testUser))
                    .contentType(contentType)
               )
        .andExpect(status().isCreated());

    mvc.perform(get("/V1/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$[0].username", is("user123!")));
  }

  @Test
  public void testPutMethod_withEmptyParams() throws Exception {
    User testUser = new User();

    mvc.perform(put("/V1/users")
                   .content(json(testUser))
                    .contentType(contentType)
               )
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testGetUserByUsername() throws Exception {
    setupUser1AndUser2();

    mvc.perform(get("/V1/users/user1!"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$.username", is("user1!")));

    mvc.perform(get("/V1/users/user2@"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$.username", is("user2@")));
  }

  @Test
  public void testUser1GetChatHistory() throws Exception {
    setupUser1AndUser2();

    ChatHistory historyEntry = new ChatHistory();
    historyEntry.setId(1);
    historyEntry.setMessage("Some message to user 2");
    historyEntry.setTargetUsername("user2@");

    mvc.perform(put("/V1/users/user1!/chatHistory")
            .content(json(historyEntry))
            .contentType(contentType)
    )
            .andExpect(status().isCreated());


    mvc.perform(get("/V1/users/user1!/chatHistory"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$[0].targetUsername", is("user2@")));
  }

  private void setupUser1AndUser2() throws Exception {
    User testUser1 = new User();
    testUser1.setUsername("user1!");

    User testUser2 = new User();
    testUser2.setUsername("user2@");

    mvc.perform(put("/V1/users")
                .content(json(testUser1))
                .contentType(contentType)
               )
            .andExpect(status().isCreated());

    mvc.perform(put("/V1/users")
                .content(json(testUser2))
                .contentType(contentType)
               )
            .andExpect(status().isCreated());
  }

  protected String json(Object o) throws IOException {
    MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
    this.mappingJackson2HttpMessageConverter.write(
        o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
    return mockHttpOutputMessage.getBodyAsString();
  }
}
