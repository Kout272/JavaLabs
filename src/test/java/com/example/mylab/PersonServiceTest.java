package com.example.mylab;

import com.example.mylab.cache.CommonCache;
import com.example.mylab.model.Person;
import com.example.mylab.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository repository;

    @InjectMocks
    private PersonService personService;

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1);
        person.setName("John");
        person.setSurname("Doe");
    }

    @Test
    void findAll_ReturnsCachedPersons_WhenCacheExists() {
        List<Person> cachedPersons = Arrays.asList(person);
        when(CommonCache.get("all_persons", List.class)).thenReturn(cachedPersons);

        List<Person> result = personService.findAll();

        assertEquals(cachedPersons, result);
        verify(repository, never()).findAll();
    }

    @Test
    void findAll_ReturnsPersonsFromRepository_WhenCacheIsEmpty() {
        List<Person> persons = Arrays.asList(person);
        when(CommonCache.get("all_persons", List.class)).thenReturn(null);
        when(repository.findAll()).thenReturn(persons);

        List<Person> result = personService.findAll();

        assertEquals(persons, result);
        verify(CommonCache).put("all_persons", persons);
        verify(repository).findAll();
    }

    @Test
    void findById_ReturnsCachedPerson_WhenCacheExists() {
        when(CommonCache.getById("persons", 1, Person.class)).thenReturn(person);

        Optional<Person> result = personService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(person, result.get());
        verify(repository, never()).findById(anyInt());
    }

    @Test
    void findById_ReturnsPersonFromRepository_WhenCacheIsEmpty() {
        when(CommonCache.getById("persons", 1, Person.class)).thenReturn(null);
        when(repository.findById(1)).thenReturn(Optional.of(person));

        Optional<Person> result = personService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(person, result.get());
        verify(CommonCache).putWithId("persons", 1, person);
        verify(repository).findById(1);
    }

    @Test
    void create_SavesAndCachesPerson() {
        when(repository.save(person)).thenReturn(person);

        Person result = personService.create(person);

        assertEquals(person, result);
        verify(repository).save(person);
        verify(CommonCache).putWithId("persons", person.getId(), person);
        verify(CommonCache).put("all_persons", null);
    }

    @Test
    void createAll_SavesAndCachesMultiplePersons() {
        List<Person> persons = Arrays.asList(person);
        when(repository.save(any(Person.class))).thenReturn(person);

        List<Person> result = personService.createAll(persons);

        assertEquals(1, result.size());
        assertEquals(person, result.get(0));
        verify(repository, times(1)).save(person);
        verify(CommonCache).putWithId("persons", person.getId(), person);
        verify(CommonCache).put("all_persons", null);
    }

    @Test
    void update_UpdatesExistingPerson() {
        Person updatedDetails = new Person();
        updatedDetails.setName("Timur");
        updatedDetails.setSurname("Panov");
        when(repository.findById(1)).thenReturn(Optional.of(person));
        when(repository.save(any(Person.class))).thenReturn(person);

        Person result = personService.update(1, updatedDetails);

        assertEquals(person, result);
        assertEquals("Timur", person.getName());
        assertEquals("Panov", person.getSurname());
        verify(CommonCache).removeById("persons", 1);
        verify(CommonCache).putWithId("persons", 1, person);
        verify(CommonCache).put("all_persons", null);
    }

    @Test
    void update_ReturnsNull_WhenPersonNotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        Person result = personService.update(1, person);

        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    void updateAll_UpdatesMultiplePersons() {
        Person update = new Person();
        update.setId(1);
        update.setName("Timur");
        update.setSurname("Panov");
        when(repository.findById(1)).thenReturn(Optional.of(person));
        when(repository.save(any(Person.class))).thenReturn(person);

        List<Person> result = personService.updateAll(Arrays.asList(update));

        assertEquals(1, result.size());
        assertEquals("Timur", result.get(0).getName());
        verify(CommonCache).removeById("persons", 1);
        verify(CommonCache).putWithId("persons", 1, person);
        verify(CommonCache).put("all_persons", null);
    }

    @Test
    void delete_RemovesPerson_WhenExists() {
        when(repository.findById(1)).thenReturn(Optional.of(person));

        personService.delete(1);

        verify(CommonCache).removeById("persons", 1);
        verify(CommonCache).put("all_persons", null);
        verify(repository).deleteById(1);
    }

    @Test
    void deleteAll_RemovesMultiplePersons() {
        List<Integer> ids = Arrays.asList(1);
        when(repository.findAllById(ids)).thenReturn(Arrays.asList(person));

        personService.deleteAll(ids);

        verify(CommonCache).removeById("persons", 1);
        verify(CommonCache).put("all_persons", null);
        verify(repository).deleteAllById(ids);
    }

    @Test
    void findByCountryName_ReturnsCachedPersons_WhenCacheExists() {
        List<Person> cachedPersons = Arrays.asList(person);
        when(CommonCache.get("persons_by_country_USA", List.class)).thenReturn(cachedPersons);

        List<Person> result = personService.findByCountryName("USA");

        assertEquals(cachedPersons, result);
        verify(repository, never()).findPersonsByCountryName(anyString());
    }

    @Test
    void findByCountryName_ReturnsPersonsFromRepository_WhenCacheIsEmpty() {
        List<Person> persons = Arrays.asList(person);
        when(CommonCache.get("persons_by_country_USA", List.class)).thenReturn(null);
        when(repository.findPersonsByCountryName("USA")).thenReturn(persons);

        List<Person> result = personService.findByCountryName("USA");

        assertEquals(persons, result);
        verify(CommonCache).put("persons_by_country_USA", persons);
        verify(CommonCache).putWithId("persons", person.getId(), person);
    }
}