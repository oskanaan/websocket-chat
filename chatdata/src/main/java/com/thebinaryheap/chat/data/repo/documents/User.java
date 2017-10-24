package com.thebinaryheap.chat.data.repo.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "user")
public class User{
  @Id
  @NotNull
  private String username;
  private String password;

  private List<ChatHistory> chatHistoryList;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<ChatHistory> getChatHistoryList() {
    return chatHistoryList;
  }

  public void setChatHistoryList(List<ChatHistory> chatHistoryList) {
    this.chatHistoryList = chatHistoryList;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
