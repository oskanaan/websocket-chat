package com.thebinaryheap.chat.server.rest.controller;

import com.thebinaryheap.chat.server.message.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ChatController {

  @MessageMapping("/send/{sourceUser}/to/{targetUser}")
  @SendTo("/topic/chatMessage/{targetUser}")
  public ChatMessage receiveMessage(@PathVariable String sourceUser, @PathVariable String targetUser, ChatMessage message){
    return message;
  }
}
