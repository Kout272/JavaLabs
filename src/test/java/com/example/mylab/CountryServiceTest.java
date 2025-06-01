package com.example.mylab;

import com.example.mylab.model.Country;
import com.example.mylab.repository.CountryRepository;
import com.example.mylab.service.CountryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryService countryService;

    @Test
    void create_ShouldSaveCountry_WhenDataIsValid() {
        Country country = new Country("Belarus", "BY");
        when(countryRepository.save(country)).thenReturn(country);

        Country savedCountry = countryService.create(country);

        assertNotNull(savedCountry);
        assertEquals("Belarus", savedCountry.getName());
        verify(countryRepository).save(country);
    }

    @Test
    void create_ShouldThrowException_WhenNameOrCodeIsNull() {
        Country invalidCountry = new Country(null, null);

        assertThrows(IllegalArgumentException.class, () -> countryService.create(invalidCountry));
        verify(countryRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenCodeAlreadyExists() {
        Country country = new Country("Belarus", "BY");
        when(countryRepository.save(country)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(IllegalArgumentException.class, () -> countryService.create(country));
    }

    @Test
    void getCodeByCountry_ShouldReturnCode_WhenCountryExists() {
        String countryName = "Belarus";
        Country country = new Country(countryName, "BY");
        when(countryRepository.findByName(countryName)).thenReturn(Optional.of(country));

        String code = countryService.getCodeByCountry(countryName);

        assertEquals("BY", code);
    }

    @Test
    void getCodeByCountry_ShouldReturnNull_WhenCountryNotExists() {
        when(countryRepository.findByName("Unknown")).thenReturn(Optional.empty());

        String code = countryService.getCodeByCountry("Unknown");

        assertNull(code);
    }

    @Test
    void findAll_ShouldReturnAllCountries() {
        List<Country> countries = List.of(
                new Country("Belarus", "BY"),
                new Country("Russia", "RU")
        );
        when(countryRepository.findAll()).thenReturn(countries);

        List<Country> result = countryService.findAll();

        assertEquals(2, result.size());
        verify(countryRepository).findAll();
    }

    @Test
    void update_ShouldUpdateCountry_WhenIdExists() {
        Integer id = 1;
        Country existingCountry = new Country("Belarus", "BY");
        Country updatedDetails = new Country("Belarus Updated", "BY");

        when(countryRepository.findById(id)).thenReturn(Optional.of(existingCountry));
        when(countryRepository.save(existingCountry)).thenReturn(updatedDetails);

        Country result = countryService.update(id, updatedDetails);

        assertEquals("Belarus Updated", result.getName());
        verify(countryRepository).findById(id);
        verify(countryRepository).save(existingCountry);
    }

    @Test
    void update_ShouldReturnNull_WhenIdNotExists() {
        when(countryRepository.findById(999)).thenReturn(Optional.empty());

        Country result = countryService.update(999, new Country());

        assertNull(result);
    }

    @Test
    void delete_ShouldDeleteCountry_WhenIdExists() {
        Integer id = 1;
        doNothing().when(countryRepository).deleteById(id);

        countryService.delete(id);

        verify(countryRepository).deleteById(id);
    }
}