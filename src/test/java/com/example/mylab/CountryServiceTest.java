package com.example.mylab;

import com.example.mylab.cache.CommonCache;
import com.example.mylab.model.Country;
import com.example.mylab.model.Person;
import com.example.mylab.repository.CountryRepository;
import com.example.mylab.service.PersonService;
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
class CountryServiceTest {

    @Mock
    private CountryRepository repository;

    @Mock
    private PersonService personService;

    @InjectMocks
    private CountryService countryService;

    private Country country;
    private Person person;

    @BeforeEach
    void setUp() {
        country = new Country();
        country.setId(1);
        country.setName("Belarus");
        country.setCode("+375");

        person = new Person();
        person.setId(1);
        person.setName("Timur");
        person.setSurname("Panov");
    }

    @Test
    void findAll_ReturnsCachedCountries_WhenCacheExists() {
        List<Country> cachedCountries = Arrays.asList(country);
        when(CommonCache.get("all_countries", List.class)).thenReturn(cachedCountries);

        List<Country> result = countryService.findAll();

        assertEquals(cachedCountries, result);
        verify(repository, never()).findAll();
    }

    @Test
    void findAll_ReturnsCountriesFromRepository_WhenCacheIsEmpty() {
        List<Country> countries = Arrays.asList(country);
        when(CommonCache.get("all_countries", List.class)).thenReturn(null);
        when(repository.findAll()).thenReturn(countries);

        List<Country> result = countryService.findAll();

        assertEquals(countries, result);
        verify(CommonCache).put("all_countries", countries);
        verify(repository).findAll();
    }

    @Test
    void findById_ReturnsCachedCountry_WhenCacheExists() {
        when(CommonCache.getById("countries", 1, Country.class)).thenReturn(country);

        Optional<Country> result = countryService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(country, result.get());
        verify(repository, never()).findById(anyInt());
    }

    @Test
    void findById_ReturnsCountryFromRepository_WhenCacheIsEmpty() {
        when(CommonCache.getById("countries", 1, Country.class)).thenReturn(null);
        when(repository.findById(1)).thenReturn(Optional.of(country));

        Optional<Country> result = countryService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(country, result.get());
        verify(CommonCache).putWithId("countries", 1, country);
        verify(repository).findById(1);
    }

    @Test
    void getCodeByCountry_ReturnsCachedCode_WhenCacheExists() {
        when(CommonCache.get("country_code_Belarus", String.class)).thenReturn("+375");

        String result = countryService.getCodeByCountry("Belarus");

        assertEquals("+375", result);
        verify(repository, never()).findByName(anyString());
    }

    @Test
    void getCodeByCountry_ReturnsCodeFromRepository_WhenCacheIsEmpty() {
        when(CommonCache.get("country_code_Belarus", String.class)).thenReturn(null);
        when(repository.findByName("Belarus")).thenReturn(Optional.of(country));

        String result = countryService.getCodeByCountry("Belarus");

        assertEquals("+375", result);
        verify(CommonCache).put("country_code_Belarus", "+375");
        verify(CommonCache).put("country_name_+375", "Belarus");
    }

    @Test
    void getCountryByCode_ReturnsCachedName_WhenCacheExists() {
        when(CommonCache.get("country_name_+375", String.class)).thenReturn("Belarus");

        String result = countryService.getCountryByCode("+375");

        assertEquals("Belarus", result);
        verify(repository, never()).findByCode(anyString());
    }

    @Test
    void getCountryByCode_ReturnsNameFromRepository_WhenCacheIsEmpty() {
        when(CommonCache.get("country_name_+375", String.class)).thenReturn(null);
        when(repository.findByCode("+375")).thenReturn(Optional.of(country));

        String result = countryService.getCountryByCode("+375");

        assertEquals("Belarus", result);
        verify(CommonCache).put("country_name_+375", "Belarus");
        verify(CommonCache).put("country_code_Belarus", "+375");
    }

    @Test
    void create_SavesAndCachesCountry_WhenPersonExists() {
        when(personService.findById(1)).thenReturn(Optional.of(person));
        when(repository.save(any(Country.class))).thenReturn(country);

        Country result = countryService.create(country, 1);

        assertEquals(country, result);
        verify(repository).save(country);
        verify(CommonCache).putWithId("countries", country.getId(), country);
        verify(CommonCache).put("all_countries", null);
    }

    @Test
    void create_ReturnsNull_WhenPersonNotFound() {
        when(personService.findById(1)).thenReturn(Optional.empty());

        Country result = countryService.create(country, 1);

        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    void createAll_SavesAndCachesMultipleCountries_WhenPersonExists() {
        when(personService.findById(1)).thenReturn(Optional.of(person));
        when(repository.save(any(Country.class))).thenReturn(country);

        List<Country> result = countryService.createAll(Arrays.asList(country), 1);

        assertEquals(1, result.size());
        assertEquals(country, result.get(0));
        verify(repository, times(1)).save(country);
        verify(CommonCache).putWithId("countries", country.getId(), country);
        verify(CommonCache).put("all_countries", null);
    }

    @Test
    void update_UpdatesExistingCountry() {
        Country updatedDetails = new Country();
        updatedDetails.setName("Russia");
        updatedDetails.setCode("+7");
        when(repository.findById(1)).thenReturn(Optional.of(country));
        when(repository.save(any(Country.class))).thenReturn(country);

        Country result = countryService.update(1, updatedDetails);

        assertEquals(country, result);
        assertEquals("Russia", country.getName());
        assertEquals("+7", country.getCode());
        verify(CommonCache).removeById("countries", 1);
        verify(CommonCache).putWithId("countries", 1, country);
        verify(CommonCache).put("all_countries", null);
    }

    @Test
    void update_ReturnsNull_WhenCountryNotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        Country result = countryService.update(1, country);

        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    void updateAll_UpdatesMultipleCountries() {
        Country update = new Country();
        update.setId(1);
        update.setName("Russia");
        update.setCode("+7");
        when(repository.findById(1)).thenReturn(Optional.of(country));
        when(repository.save(any(Country.class))).thenReturn(country);

        List<Country> result = countryService.updateAll(Arrays.asList(update));

        assertEquals(1, result.size());
        assertEquals("Russia", result.get(0).getName());
        verify(CommonCache).removeById("countries", 1);
        verify(CommonCache).putWithId("countries", 1, country);
        verify(CommonCache).put("all_countries", null);
    }

    @Test
    void delete_RemovesCountry_WhenExists() {
        when(repository.findById(1)).thenReturn(Optional.of(country));

        countryService.delete(1);

        verify(CommonCache).removeById("countries", 1);
        verify(CommonCache).put("all_countries", null);
        verify(repository).deleteById(1);
    }

    @Test
    void deleteAll_RemovesMultipleCountries() {
        List<Integer> ids = Arrays.asList(1);
        when(repository.findAllById(ids)).thenReturn(Arrays.asList(country));

        countryService.deleteAll(ids);

        verify(CommonCache).removeById("countries", 1);
        verify(CommonCache).put("all_countries", null);
        verify(repository).deleteAllById(ids);
    }
}