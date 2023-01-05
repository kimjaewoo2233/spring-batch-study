package com.example.springbatchexample.part6;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {
    Collection<User> findAllByUpdatedDate(LocalDate updatedDate);
}
