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
    void CreateShouldSaveCountryWhenDataIsValid() {
        Country country = new Country("Belarus", "BY");
        when(countryRepository.save(country)).thenReturn(country);

        Country savedCountry = countryService.create(country);

        assertNotNull(savedCountry);
        assertEquals("BY", savedCountry.getCode());
        verify(countryRepository).save(country);
    }

    @Test
    void CreateShouldThrowExceptionWhenNameOrCodeIsNull() {
        Country invalidCountry = new Country(null, null);

        assertThrows(IllegalArgumentException.class, () -> countryService.create(invalidCountry));
        verify(countryRepository, never()).save(any());
    }

    @Test
    void CreateShouldThrowExceptionWhenCodeAlreadyExists() {
        Country country = new Country("Belarus", "BY");
        when(countryRepository.save(country))
                .thenThrow(new DataIntegrityViolationException("Duplicate code"));

        assertThrows(IllegalArgumentException.class, () -> countryService.create(country));
    }

    @Test
    void GetCodeByCountryShouldReturnCodeWhenCountryExists() {
        String countryName = "Belarus";
        when(countryRepository.findByName(countryName))
                .thenReturn(Optional.of(new Country(countryName, "BY")));

        String code = countryService.getCodeByCountry(countryName);

        assertEquals("BY", code);
    }

    @Test
    void GetCodeByCountryShouldReturnNullWhenCountryNotExists() {
        when(countryRepository.findByName("Unknown")).thenReturn(Optional.empty());

        String code = countryService.getCodeByCountry("Unknown");

        assertNull(code);
    }

    @Test
    void FindAllShouldReturnAllCountries() {
        List<Country> mockCountries = List.of(
                new Country("Belarus", "BY"),
                new Country("Poland", "PL")
        );
        when(countryRepository.findAll()).thenReturn(mockCountries);

        List<Country> result = countryService.findAll();

        assertEquals(2, result.size());
        assertEquals("PL", result.get(1).getCode());
        verify(countryRepository).findAll();
    }

    @Test
    void UpdateShouldUpdateCountryWhenIdExists() {
        Integer id = 1;
        Country existing = new Country("Belarus", "BY");
        Country updated = new Country("Belarus Updated", "BY");

        when(countryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(countryRepository.save(existing)).thenReturn(updated);

        Country result = countryService.update(id, updated);

        assertEquals("Belarus Updated", result.getName());
        verify(countryRepository).findById(id);
        verify(countryRepository).save(existing);
    }

    @Test
    void UpdateShouldReturnNullWhenIdNotExists() {
        when(countryRepository.findById(999)).thenReturn(Optional.empty());

        Country result = countryService.update(999, new Country());

        assertNull(result);
        verify(countryRepository, never()).save(any());
    }

    @Test
    void UpdateShouldThrowExceptionWhenCodeConflict() {
        Integer id = 1;
        Country existing = new Country("Belarus", "BY");
        Country updated = new Country("Belarus", "RU"); // RU уже существует

        when(countryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(countryRepository.save(existing))
                .thenThrow(new DataIntegrityViolationException("Duplicate code"));

        assertThrows(IllegalArgumentException.class,
                () -> countryService.update(id, updated));
    }

    @Test
    void DeleteShouldDeleteCountryWhenIdExists() {
        Integer id = 1;
        doNothing().when(countryRepository).deleteById(id);

        countryService.delete(id);

        verify(countryRepository).deleteById(id);
    }
}