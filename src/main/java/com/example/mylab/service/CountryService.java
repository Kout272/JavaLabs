package com.example.mylab.service;

import com.example.mylab.model.Country;
import com.example.mylab.repository.CountryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountryService {

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public Country create(Country country) {
        if (country.getName() == null || country.getName().isEmpty() ||
                country.getCode() == null || country.getCode().isEmpty()) {
            throw new IllegalArgumentException("Country name and code are required");
        }

        try {
            return countryRepository.save(country);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Country with this code already exists");
        }
    }

    public String getCodeByCountry(String countryName) {
        return countryRepository.findByName(countryName)
                .map(Country::getCode)
                .orElse(null);
    }

    public String getCountryByCode(String code) {
        return countryRepository.findByCode(code)
                .map(Country::getName)
                .orElse(null);
    }

    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    public Optional<Country> findById(Integer id) {
        return countryRepository.findById(id);
    }

    public Country update(Integer id, Country countryDetails) {
        return countryRepository.findById(id)
                .map(country -> {
                    country.setName(countryDetails.getName());
                    country.setCode(countryDetails.getCode());
                    return countryRepository.save(country);
                })
                .orElse(null);
    }

    public void delete(Integer id) {
        countryRepository.deleteById(id);
    }
}