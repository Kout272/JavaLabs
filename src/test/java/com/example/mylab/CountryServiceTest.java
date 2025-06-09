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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryService countryService;

    @Test
    void ShouldSaveCountryWhenDataIsValid() {
        Country mockCountry = mock(Country.class);
        when(mockCountry.getName()).thenReturn("Belarus");
        when(mockCountry.getCode()).thenReturn("BY");
        when(countryRepository.save(mockCountry)).thenReturn(mockCountry);

        Country savedCountry = countryService.create(mockCountry);

        assertNotNull(savedCountry);
        assertEquals("Belarus", savedCountry.getName());
        verify(countryRepository).save(mockCountry);
    }

    @Test
    void ShouldThrowExceptionWhenNameOrCodeIsNull() {
        Country mockCountry = mock(Country.class);
        when(mockCountry.getName()).thenReturn(null);
        when(mockCountry.getCode()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> countryService.create(mockCountry));
        verify(countryRepository, never()).save(any());
    }

    @Test
    void ShouldThrowExceptionWhenCodeAlreadyExists() {
        Country mockCountry = mock(Country.class);
        when(mockCountry.getName()).thenReturn("Belarus");
        when(mockCountry.getCode()).thenReturn("BY");
        when(countryRepository.save(mockCountry)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(IllegalArgumentException.class, () -> countryService.create(mockCountry));
    }

    @Test
    void ShouldReturnCodeWhenCountryExists() {
        String countryName = "Belarus";
        Country mockCountry = mock(Country.class);
        when(mockCountry.getName()).thenReturn(countryName);
        when(mockCountry.getCode()).thenReturn("BY");
        when(countryRepository.findByName(countryName)).thenReturn(Optional.of(mockCountry));

        String code = countryService.getCodeByCountry(countryName);

        assertEquals("BY", code);
    }

    @Test
    void ShouldReturnNullWhenCountryNotExists() {
        when(countryRepository.findByName("Unknown")).thenReturn(Optional.empty());

        String code = countryService.getCodeByCountry("Unknown");

        assertNull(code);
    }

    @Test
    void ShouldReturnAllCountries() {
        Country mockCountry1 = mock(Country.class);
        Country mockCountry2 = mock(Country.class);
        List<Country> mockCountries = List.of(mockCountry1, mockCountry2);
        when(countryRepository.findAll()).thenReturn(mockCountries);

        List<Country> result = countryService.findAll();

        assertEquals(2, result.size());
        verify(countryRepository).findAll();
    }

    @Test
    void ShouldUpdateCountryWhenIdExists() {
        Integer id = 1;
        Country existingMock = mock(Country.class);
        Country updatedMock = mock(Country.class);
        when(updatedMock.getName()).thenReturn("Belarus Updated");
        when(updatedMock.getCode()).thenReturn("BY");

        when(countryRepository.findById(id)).thenReturn(Optional.of(existingMock));
        when(countryRepository.save(existingMock)).thenReturn(updatedMock);

        Country result = countryService.update(id, updatedMock);

        assertEquals("Belarus Updated", result.getName());
        verify(countryRepository).findById(id);
        verify(countryRepository).save(existingMock);
    }

    @Test
    void ShouldReturnNullWhenIdNotExists() {
        when(countryRepository.findById(999)).thenReturn(Optional.empty());

        Country mockCountry = mock(Country.class);
        Country result = countryService.update(999, mockCountry);

        assertNull(result);
    }

    @Test
    void ShouldDeleteCountryWhenIdExists() {
        Integer id = 1;
        doNothing().when(countryRepository).deleteById(id);

        countryService.delete(id);

        verify(countryRepository).deleteById(id);
    }
}