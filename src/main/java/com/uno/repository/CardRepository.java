package com.uno.repository;

import com.uno.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {
    //get top card by id
    Optional<Card> findById(Integer id);
}
