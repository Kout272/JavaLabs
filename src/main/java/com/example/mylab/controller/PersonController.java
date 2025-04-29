package com.example.mylab.controller;

import com.example.mylab.model.Person;
import com.example.mylab.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/persons")
public class PersonController {
    @Autowired
    private PersonService personService;

    @GetMapping
    public List<Person> getAllPersons() {
        return personService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable Integer id) {
        Optional<Person> person = personService.findById(id);
        return person.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public Person createPerson(@RequestBody Person person) {
        return personService.create(person);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(@PathVariable Integer id, @RequestBody Person personDetails) {
        Person updatedPerson = personService.update(id, personDetails);
        return updatedPerson != null ? ResponseEntity.ok(updatedPerson) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Integer id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-country")
    public ResponseEntity<List<Person>> getPersonsByCountry(@RequestParam String country) {
        List<Person> persons = personService.findByCountryName(country);
        if (persons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(persons);
    }
}