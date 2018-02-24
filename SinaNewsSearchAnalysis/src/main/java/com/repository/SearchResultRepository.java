package com.repository;


import com.models.SearchResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchResultRepository extends JpaRepository<SearchResult, Integer> {
    SearchResult findById(int id);
}
