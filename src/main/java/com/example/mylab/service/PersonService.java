package com.example.mylab.service;

import com.example.mylab.cache.CommonCache;
import com.example.mylab.counter.RequestCounter;
import com.example.mylab.model.Person;
import com.example.mylab.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private static final String CACHE_NAME = "persons";
    private static final String ALL_PERSONS_CACHE_KEY = "all_persons";

    private final PersonRepository repository;
    private final RequestCounter requestCounter;

    @Autowired
    public PersonService(PersonRepository repository, RequestCounter requestCounter) {
        this.repository = repository;
        this.requestCounter = requestCounter;
    }

    public List<Person> findAll() {
        requestCounter.increment("PersonService.findAll");
        List<Person> cached = CommonCache.get(ALL_PERSONS_CACHE_KEY, List.class);
        if (cached != null) {
            return cached;
        }

        List<Person> persons = repository.findAll();
        cachePersons(persons);
        CommonCache.put(ALL_PERSONS_CACHE_KEY, persons);
        return persons;
    }

    public Optional<Person> findById(Integer id) {
        requestCounter.increment("PersonService.findById");
        Person cached = CommonCache.getById(CACHE_NAME, id, Person.class);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<Person> person = repository.findById(id);
        person.ifPresent(this::cachePerson);
        return person;
    }

    public Person create(Person person) {
        requestCounter.increment("PersonService.create");
        Person saved = repository.save(person);
        cachePerson(saved);
        invalidateAllPersonsCache();
        return saved;
    }

    public List<Person> createAll(List<Person> persons) {
        requestCounter.increment("PersonService.createAll");
        List<Person> savedPersons = persons.stream()
                .map(repository::save)
                .peek(this::cachePerson)
                .collect(Collectors.toList());
        invalidateAllPersonsCache();
        return savedPersons;
    }

    public Person update(Integer id, Person personDetails) {
        requestCounter.increment("PersonService.update");
        return repository.findById(id)
                .map(existing -> {
                    clearPersonCache(existing);
                    existing.setName(personDetails.getName());
                    existing.setSurname(personDetails.getSurname());
                    Person updated = repository.save(existing);
                    cachePerson(updated);
                    invalidateAllPersonsCache();
                    return updated;
                })
                .orElse(null);
    }

    public List<Person> updateAll(List<Person> personUpdates) {
        requestCounter.increment("PersonService.updateAll");
        return personUpdates.stream()
                .map(update -> repository.findById(update.getId())
                        .map(existing -> {
                            clearPersonCache(existing);
                            existing.setName(update.getName());
                            existing.setSurname(update.getSurname());
                            Person updated = repository.save(existing);
                            cachePerson(updated);
                            return updated;
                        })
                        .orElse(null))
                .filter(person -> person != null)
                .peek(person -> invalidateAllPersonsCache())
                .collect(Collectors.toList());
    }

    public void delete(Integer id) {
        requestCounter.increment("PersonService.delete");
        repository.findById(id).ifPresent(person -> {
            clearPersonCache(person);
            invalidateAllPersonsCache();
            repository.deleteById(id);
        });
    }

    public void deleteAll(List<Integer> ids) {
        requestCounter.increment("PersonService.deleteAll");
        List<Person> persons = repository.findAllById(ids);
        persons.forEach(this::clearPersonCache);
        invalidateAllPersonsCache();
        repository.deleteAllById(ids);
    }

    public List<Person> findByCountryName(String countryName) {
        requestCounter.increment("PersonService.findByCountryName");
        String cacheKey = "persons_by_country_" + countryName;
        List<Person> cached = CommonCache.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        List<Person> persons = repository.findPersonsByCountryName(countryName);
        CommonCache.put(cacheKey, persons);
        cachePersons(persons);
        return persons;
    }

    private void cachePerson(Person person) {
        CommonCache.putWithId(CACHE_NAME, person.getId(), person);
    }

    private void cachePersons(List<Person> persons) {
        persons.forEach(this::cachePerson);
    }

    private void clearPersonCache(Person person) {
        CommonCache.removeById(CACHE_NAME, person.getId());
    }

    private void invalidateAllPersonsCache() {
        CommonCache.put(ALL_PERSONS_CACHE_KEY, null);
    }
}