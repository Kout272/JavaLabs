package com.example.mylab;

import com.example.mylab.cache.CommonCache;
import com.example.mylab.counter.RequestCounter;
import com.example.mylab.model.Person;
import com.example.mylab.repository.PersonRepository;
import com.example.mylab.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = mock(Person.class);
        when(testPerson.getId()).thenReturn(1);
        when(testPerson.getName()).thenReturn("Timur");
        when(testPerson.getSurname()).thenReturn("Panov");
    }

    @Test
    void findAll_ShouldReturnCachedPersons_WhenCacheExists() {
        when(commonCache.get("all_persons", List.class))
                .thenReturn(Collections.singletonList(testPerson));

        List<Person> result = personService.findAll();

        assertEquals(1, result.size());
        verify(personRepository, never()).findAll();
        verify(requestCounter).increment("PersonService.findAll");
    }

    @Test
    void findById_ShouldReturnCachedPerson_WhenExistsInCache() {
        when(commonCache.getById("persons", 1, Person.class))
                .thenReturn(testPerson);

        Optional<Person> result = personService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(testPerson, result.get());
        verify(personRepository, never()).findById(anyInt());
    }

    @Test
    void create_ShouldSaveNewPerson_AndUpdateCache() {
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        Person result = personService.create(testPerson);

        assertNotNull(result);
        verify(commonCache).putWithId("persons", 1, testPerson);
        verify(commonCache).put("all_persons", null);
    }

    @Test
    void update_ShouldModifyExistingPerson_AndClearCache() {
        Person updatedDetails = mock(Person.class);
        when(updatedDetails.getName()).thenReturn("Updated");
        when(updatedDetails.getSurname()).thenReturn("User");

        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        Person result = personService.update(1, updatedDetails);

        assertEquals("Updated", testPerson.getName());
        verify(commonCache).removeById("persons", 1);
        verify(commonCache).put("all_persons", null);
    }

    @Test
    void delete_ShouldRemovePerson_AndClearCache() {
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));

        personService.delete(1);

        verify(personRepository).deleteById(1);
        verify(commonCache).removeById("persons", 1);
    }

    @Test
    void findByCountryName_ShouldReturnCachedResults_WhenAvailable() {
        String countryName = "Belarus";
        String cacheKey = "persons_by_country_" + countryName;
        when(commonCache.get(cacheKey, List.class))
                .thenReturn(Collections.singletonList(testPerson));

        List<Person> result = personService.findByCountryName(countryName);

        assertEquals(1, result.size());
        verify(personRepository, never()).findPersonsByCountryName(anyString());
    }

    @Test
    void bulkOperations_ShouldWorkCorrectly() {
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);
        List<Person> created = personService.createAll(Collections.singletonList(testPerson));
        assertEquals(1, created.size());

        Person update = mock(Person.class);
        when(update.getId()).thenReturn(1);
        when(update.getName()).thenReturn("Updated");
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));

        List<Person> updated = personService.updateAll(Collections.singletonList(update));
        assertEquals(1, updated.size());

        personService.deleteAll(Collections.singletonList(1));
        verify(personRepository).deleteAllById(anyList());
    }

    @Test
    void shouldIncrementCounter_ForAllOperations() {
        when(personRepository.save(any())).thenReturn(testPerson);
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));
        when(commonCache.get(anyString(), any())).thenReturn(null);

        personService.findAll();
        personService.findById(1);
        personService.create(testPerson);
        personService.update(1, testPerson);
        personService.delete(1);
        personService.findByCountryName("Belarus");

        verify(requestCounter, times(6)).increment(anyString());
    }
}