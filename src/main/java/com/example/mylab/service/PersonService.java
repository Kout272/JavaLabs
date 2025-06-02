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
    private static final String ALL_PERSONS_KEY = "all_persons";
    private static final String PERSONS_BY_COUNTRY_PREFIX = "persons_by_country_";

    private final PersonRepository personRepository;
    private final RequestCounter requestCounter;
    private final CommonCache commonCache;

    @Autowired
    public PersonService(
            PersonRepository personRepository,
            RequestCounter requestCounter,
            CommonCache commonCache) {
        this.personRepository = personRepository;
        this.requestCounter = requestCounter;
        this.commonCache = commonCache;
    }

    public List<Person> findAll() {
        requestCounter.increment("PersonService.findAll");
        List<Person> cached = commonCache.get(ALL_PERSONS_KEY, List.class);
        if (cached != null) {
            return cached;
        }

        List<Person> persons = personRepository.findAll();
        cachePersons(persons);
        commonCache.put(ALL_PERSONS_KEY, persons);
        return persons;
    }

    public Optional<Person> findById(Integer id) {
        requestCounter.increment("PersonService.findById");
        Person cached = commonCache.getById(CACHE_NAME, id, Person.class);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<Person> person = personRepository.findById(id);
        person.ifPresent(this::cachePerson);
        return person;
    }

    public Person create(Person person) {
        requestCounter.increment("PersonService.create");
        Person saved = personRepository.save(person);
        cachePerson(saved);
        invalidateAllPersonsCache();
        return saved;
    }

    public List<Person> createAll(List<Person> persons) {
        requestCounter.increment("PersonService.createAll");
        List<Person> savedPersons = persons.stream()
                .map(personRepository::save)
                .peek(this::cachePerson)
                .collect(Collectors.toList());
        invalidateAllPersonsCache();
        return savedPersons;
    }

    public Person update(Integer id, Person personDetails) {
        requestCounter.increment("PersonService.update");
        return personRepository.findById(id)
                .map(existing -> {
                    clearPersonCache(existing);
                    existing.setName(personDetails.getName());
                    existing.setSurname(personDetails.getSurname());
                    Person updated = personRepository.save(existing);
                    cachePerson(updated);
                    invalidateAllPersonsCache();
                    return updated;
                })
                .orElse(null);
    }

    public List<Person> updateAll(List<Person> personUpdates) {
        requestCounter.increment("PersonService.updateAll");
        return personUpdates.stream()
                .map(update -> personRepository.findById(update.getId())
                        .map(existing -> {
                            clearPersonCache(existing);
                            existing.setName(update.getName());
                            existing.setSurname(update.getSurname());
                            Person updated = personRepository.save(existing);
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
        personRepository.findById(id).ifPresent(person -> {
            clearPersonCache(person);
            invalidateAllPersonsCache();
            personRepository.deleteById(id);
        });
    }

    public void deleteAll(List<Integer> ids) {
        requestCounter.increment("PersonService.deleteAll");
        List<Person> persons = personRepository.findAllById(ids);
        persons.forEach(this::clearPersonCache);
        invalidateAllPersonsCache();
        personRepository.deleteAllById(ids);
    }

    public List<Person> findByCountryName(String countryName) {
        requestCounter.increment("PersonService.findByCountryName");
        String cacheKey = PERSONS_BY_COUNTRY_PREFIX + countryName;
        List<Person> cached = commonCache.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        List<Person> persons = personRepository.findPersonsByCountryName(countryName);
        commonCache.put(cacheKey, persons);
        cachePersons(persons);
        return persons;
    }

    private void cachePerson(Person person) {
        commonCache.putWithId(CACHE_NAME, person.getId(), person);
    }

    private void cachePersons(List<Person> persons) {
        persons.forEach(this::cachePerson);
    }

    private void clearPersonCache(Person person) {
        commonCache.removeById(CACHE_NAME, person.getId());
    }

    private void invalidateAllPersonsCache() {
        commonCache.put(ALL_PERSONS_KEY, null);
    }
}