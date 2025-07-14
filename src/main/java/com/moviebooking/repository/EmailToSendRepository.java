package com.moviebooking.repository;

import com.moviebooking.model.entity.EmailToSend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailToSendRepository extends JpaRepository<EmailToSend, Long> {
    List<EmailToSend> findBySentFalse();
}