package com.example.mylab;

import com.example.mylab.cache.CommonCache;
import com.example.mylab.counter.RequestCounter;
import com.example.mylab.model.Country;
import com.example.mylab.model.Person;
import com.example.mylab.repository.CountryRepository;
import com.example.mylab.service.CountryService;
import com.example.mylab.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private PersonService personService;

    @Mock
    private RequestCounter requestCounter;

    @Mock
    private CommonCache commonCache;

    @InjectMocks
    private CountryService countryService;

    private Country testCountry;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = mock(Person.class);
        when(testPerson.getId()).thenReturn(1);
        when(testPerson.getName()).thenReturn("Test");

        testCountry = mock(Country.class);
        when(testCountry.getId()).thenReturn(1);
        when(testCountry.getName()).thenReturn("TestCountry");
        when(testCountry.getCode()).thenReturn("+1");
        when(testCountry.getPerson()).thenReturn(testPerson);
    }

    @Test
    void findAll_ShouldReturnCachedCountries_WhenCacheExists() {
        when(commonCache.get("all_countries", List.class))
                .thenReturn(List.of(testCountry));

        List<Country> result = countryService.findAll();

        assertEquals(1, result.size());
        verify(commonCache, never()).put(anyString(), any());
        verify(countryRepository, never()).findAll();
    }

    @Test
    void findById_ShouldReturnCachedCountry_WhenExistsInCache() {
        when(commonCache.getById("countries", 1, Country.class))
                .thenReturn(testCountry);

        Optional<Country> result = countryService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(testCountry, result.get());
        verify(countryRepository, never()).findById(anyInt());
    }

    @Test
    void create_ShouldSaveAndCacheNewCountry_WhenPersonExists() {
        when(personService.findById(1)).thenReturn(Optional.of(testPerson));
        when(countryRepository.save(any(Country.class))).thenReturn(testCountry);

        Country result = countryService.create(testCountry, 1);

        assertNotNull(result);
        verify(commonCache).putWithId("countries", 1, testCountry);
        verify(commonCache).put("all_countries", null);
    }

    @Test
    void update_ShouldUpdateExistingCountry_WhenFound() {
        Country updated = mock(Country.class);
        when(updated.getName()).thenReturn("Updated");
        when(updated.getCode()).thenReturn("+2");

        when(countryRepository.findById(1)).thenReturn(Optional.of(testCountry));
        when(countryRepository.save(any(Country.class))).thenReturn(testCountry);

        Country result = countryService.update(1, updated);

        assertNotNull(result);
        assertEquals("TestCountry", testCountry.getName());
        verify(commonCache).removeById("countries", 1);
    }

    @Test
    void delete_ShouldRemoveCountry_WhenExists() {
        when(countryRepository.findById(1)).thenReturn(Optional.of(testCountry));

        countryService.delete(1);

        verify(countryRepository).deleteById(1);
        verify(commonCache).removeById("countries", 1);
    }

    @Test
    void createAll_ShouldSaveMultipleCountries_WhenPersonExists() {
        when(personService.findById(1)).thenReturn(Optional.of(testPerson));
        when(countryRepository.save(any(Country.class))).thenReturn(testCountry);

        List<Country> result = countryService.createAll(List.of(testCountry), 1);

        assertEquals(1, result.size());
        verify(commonCache, times(1)).putWithId(eq("countries"), eq(1), any());
    }

    @Test
    void updateAll_ShouldProcessMultipleUpdates() {
        Country update = mock(Country.class);
        when(update.getId()).thenReturn(1);
        when(update.getName()).thenReturn("Updated");

        when(countryRepository.findById(1)).thenReturn(Optional.of(testCountry));
        when(countryRepository.save(any(Country.class))).thenReturn(testCountry);

        List<Country> result = countryService.updateAll(List.of(update));

        assertEquals(1, result.size());
        verify(commonCache).removeById("countries", 1);
    }

    @Test
    void shouldIncrementCounter_ForAllOperations() {
        when(personService.findById(1)).thenReturn(Optional.of(testPerson));
        when(countryRepository.save(any())).thenReturn(testCountry);
        when(countryRepository.findById(1)).thenReturn(Optional.of(testCountry));

        countryService.findAll();
        countryService.findById(1);
        countryService.create(testCountry, 1);
        countryService.update(1, testCountry);
        countryService.delete(1);

        verify(requestCounter, times(5)).increment(anyString());
    }
}