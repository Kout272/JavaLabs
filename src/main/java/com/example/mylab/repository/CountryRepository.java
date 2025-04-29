package com.example.mylab.repository;

import com.example.mylab.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country,Integer> {
    public Optional findByName(String name);
    public Optional findByCode(String code);

}
