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
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

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
    void ShouldReturnPersonsFromCacheWhenCacheExists() {

        Person mockPerson = mock(Person.class);
        List<Person> cachedPersons = List.of(mockPerson);
        when(commonCache.get(anyString(), any())).thenReturn(cachedPersons);
        List<Person> result = personService.findAll();
        assertEquals(1, result.size());
        verify(commonCache).get("all_persons", List.class);
        verify(personRepository, never()).findAll();
    }

    @Test
    void ShouldFetchFromDbAndCacheWhenCacheEmpty() {
        Person mockPerson = mock(Person.class);
        List<Person> dbPersons = List.of(mockPerson);
        when(commonCache.get(anyString(), any())).thenReturn(null);
        when(personRepository.findAll()).thenReturn(dbPersons);
        doNothing().when(commonCache).put(anyString(), any());
        doNothing().when(requestCounter).increment(anyString());
        List<Person> result = personService.findAll();
        assertEquals(1, result.size());
        verify(commonCache).put("all_persons", dbPersons);
        verify(requestCounter).increment("PersonService.findAll");
    }

    @Test
    void ShouldReturnCachedPersonWhenExistsInCache() {
        Person mockPerson = mock(Person.class);
        when(commonCache.getById(anyString(), anyInt(), any())).thenReturn(mockPerson);
        when(mockPerson.getName()).thenReturn("Ivan");
        Optional<Person> result = personService.findById(1);
        assertTrue(result.isPresent());
        assertEquals("Ivan", result.get().getName());
        verify(personRepository, never()).findById(any());
    }

    @Test
    void ShouldSavePersonAndInvalidateCache() {
        Person mockPerson = mock(Person.class);
        Person mockSavedPerson = mock(Person.class);
        when(personRepository.save(mockPerson)).thenReturn(mockSavedPerson);
        when(mockSavedPerson.getId()).thenReturn(1);
        doNothing().when(commonCache).putWithId(anyString(), anyInt(), any());
        doNothing().when(commonCache).put(anyString(), any());
        doNothing().when(requestCounter).increment(anyString());
        Person result = personService.create(mockPerson);
        assertNotNull(result);
        verify(mockSavedPerson).getId();
        verify(commonCache).putWithId("persons", 1, mockSavedPerson);
        verify(commonCache).put("all_persons", null);
        verify(requestCounter).increment("PersonService.create");
    }

    @Test
    void ShouldUpdatePersonAndCache() {
        Person mockExistingPerson = mock(Person.class);
        Person mockUpdatedDetails = mock(Person.class);
        Person mockSavedPerson = mock(Person.class);
        when(personRepository.findById(1)).thenReturn(Optional.of(mockExistingPerson));
        when(personRepository.save(mockExistingPerson)).thenReturn(mockSavedPerson);
        when(mockSavedPerson.getSurname()).thenReturn("Updated");
        doNothing().when(commonCache).removeById(anyString(), anyInt());
        doNothing().when(commonCache).putWithId(anyString(), anyInt(), any());
        Person result = personService.update(1, mockUpdatedDetails);
        assertEquals("Updated", result.getSurname());
        verify(commonCache).removeById("persons", 1);
        verify(commonCache).putWithId("persons", 1, mockSavedPerson);
    }

    @Test
    void ShouldRemovePersonAndCache() {
        Person mockPerson = mock(Person.class);
        when(personRepository.findById(1)).thenReturn(Optional.of(mockPerson));
        doNothing().when(commonCache).removeById(anyString(), anyInt());
        doNothing().when(personRepository).deleteById(anyInt());
        personService.delete(1);
        verify(commonCache).removeById("persons", 1);
        verify(personRepository).deleteById(1);
    }
}