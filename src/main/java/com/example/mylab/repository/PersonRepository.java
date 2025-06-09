package com.example.mylab.repository;

import com.example.mylab.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person,Integer>
{
    @Query("SELECT p FROM Person p JOIN p.countries c WHERE c.name = :countryName")
    List<Person> findPersonsByCountryName(@Param("countryName") String countryName);
}