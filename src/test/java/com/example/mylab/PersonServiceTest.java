package com.example.mylab;

import com.example.mylab.cache.CommonCache;
import com.example.mylab.counter.RequestCounter;
import com.example.mylab.model.Person;
import com.example.mylab.repository.PersonRepository;
import com.example.mylab.service.PersonService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private RequestCounter requestCounter;

    @Mock
    private CommonCache commonCache;

    @InjectMocks
    private PersonService personService;

    @Test
    void FindAllShouldReturnPersonsFromCacheWhenCacheExists() {
        List<Person> cachedPersons = List.of(new Person("Ivan", "Ivanov"));
        when(commonCache.get("all_persons", List.class)).thenReturn(cachedPersons);

        List<Person> result = personService.findAll();

        assertEquals(1, result.size());
        verify(commonCache).get("all_persons", List.class);
        verify(personRepository, never()).findAll();
    }

    @Test
    void FindAllShouldFetchFromDbAndCacheWhenCacheEmpty() {
        List<Person> dbPersons = List.of(new Person("Ivan", "Ivanov"));
        when(commonCache.get("all_persons", List.class)).thenReturn(null);
        when(personRepository.findAll()).thenReturn(dbPersons);

        List<Person> result = personService.findAll();

        assertEquals(1, result.size());
        verify(commonCache).put("all_persons", dbPersons);
        verify(requestCounter).increment("PersonService.findAll");
    }

    @Test
    void FindByIdShouldReturnCachedPersonWhenExistsInCache() {
        Person cachedPerson = new Person("Ivan", "Ivanov");
        when(commonCache.getById("persons", 1, Person.class)).thenReturn(cachedPerson);

        Optional<Person> result = personService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Ivan", result.get().getName());
        verify(personRepository, never()).findById(any());
    }

    @Test
    void CreateShouldSavePersonAndInvalidateCache() {
        Person newPerson = new Person("Timur", "Panov");
        Person savedPerson = new Person("Timur", "Panov");
        savedPerson.setId(1);

        when(personRepository.save(newPerson)).thenReturn(savedPerson);

        Person result = personService.create(newPerson);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(commonCache).putWithId("persons", 1, savedPerson);
        verify(commonCache).put("all_persons", null);
        verify(requestCounter).increment("PersonService.create");
    }

    @Test
    void UpdateShouldUpdatePersonAndCache() {
        Person existingPerson = new Person("Ivan", "Ivanov");
        Person updatedDetails = new Person("Ivan", "Updated");

        when(personRepository.findById(1)).thenReturn(Optional.of(existingPerson));
        when(personRepository.save(existingPerson)).thenReturn(updatedDetails);

        Person result = personService.update(1, updatedDetails);

        assertEquals("Updated", result.getSurname());
        verify(commonCache).removeById("persons", 1);
        verify(commonCache).putWithId("persons", 1, updatedDetails);
    }

    @Test
    void DeleteShouldRemovePersonAndCache() {
        Person personToDelete = new Person("Ivan", "Ivanov");
        when(personRepository.findById(1)).thenReturn(Optional.of(personToDelete));

        personService.delete(1);

        verify(commonCache).removeById("persons", 1);
        verify(personRepository).deleteById(1);
    }
}