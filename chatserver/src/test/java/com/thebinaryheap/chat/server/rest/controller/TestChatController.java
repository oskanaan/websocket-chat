package com.thebinaryheap.chat.server.rest.controller;

import com.thebinaryheap.chat.data.repo.UserRepositoryRest;
import com.thebinaryheap.chat.server.ChatWebSocketConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.concurrent.*;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Ignore

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes={ChatWebSocketConfig.class})
public class TestChatController {

  static final String WEBSOCKET_URI = "ws://localhost:8080/chat/chat-websocket";
  static final String WEBSOCKET_TOPIC = "/topic";
  BlockingQueue<String> blockingQueue;
  WebSocketStompClient stompClient;

  @MockBean
  private UserRepositoryRest userRepository;

  @Before
  public void setup() {
    blockingQueue = new LinkedBlockingDeque<>();
    stompClient = new WebSocketStompClient(new SockJsClient(
            asList(new WebSocketTransport(new StandardWebSocketClient()))));
  }

  @Test
  public void shouldReceiveAMessageFromTheServer() throws Exception {
    StompSession session = stompClient.connect(WEBSOCKET_URI, new StompSessionHandlerAdapter() {})
                                      .get(1, TimeUnit.SECONDS);
    session.subscribe(WEBSOCKET_TOPIC, new DefaultStompFrameHandler());

    String message = "MESSAGE TEST";
    session.send(WEBSOCKET_TOPIC, message.getBytes());

    Assert.assertEquals(message, blockingQueue.poll(1, TimeUnit.SECONDS));
  }

  class DefaultStompFrameHandler implements StompFrameHandler {
    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
      return byte[].class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
      blockingQueue.offer(new String((byte[]) o));
    }
  }




}
