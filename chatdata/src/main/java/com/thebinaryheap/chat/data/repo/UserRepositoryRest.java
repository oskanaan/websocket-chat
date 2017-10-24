package com.thebinaryheap.chat.data.repo;

import com.thebinaryheap.chat.data.repo.documents.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "users", collectionResourceRel = "users")
public interface UserRepositoryRest extends PagingAndSortingRepository<User, Integer> {
  User findByUsername(String username);
}
