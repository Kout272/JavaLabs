package com.example.mylab.service;

import com.example.mylab.cache.CommonCache;
import com.example.mylab.counter.RequestCounter;
import com.example.mylab.model.Country;
import com.example.mylab.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CountryService {
    private static final String CACHE_NAME = "countries";
    private static final String ALL_COUNTRIES_KEY = "all_countries";

    private final CountryRepository countryRepository;
    private final PersonService personService;
    private final RequestCounter requestCounter;
    private final CommonCache commonCache;

    @Autowired
    public CountryService(CountryRepository countryRepository,
                          PersonService personService,
                          RequestCounter requestCounter,
                          CommonCache commonCache) {
        this.countryRepository = countryRepository;
        this.personService = personService;
        this.requestCounter = requestCounter;
        this.commonCache = commonCache;
    }

    public List<Country> findAll() {
        requestCounter.increment("CountryService.findAll");
        List<Country> cached = commonCache.get(ALL_COUNTRIES_KEY, List.class);
        if (cached != null) {
            return cached;
        }

        List<Country> countries = countryRepository.findAll();
        countries.forEach(this::cacheCountry);
        commonCache.put(ALL_COUNTRIES_KEY, countries);
        return countries;
    }

    public Optional<Country> findById(Integer id) {
        requestCounter.increment("CountryService.findById");
        Country cached = commonCache.getById(CACHE_NAME, id, Country.class);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<Country> country = countryRepository.findById(id);
        country.ifPresent(this::cacheCountry);
        return country;
    }

    public Country create(Country country, Integer personId) {
        requestCounter.increment("CountryService.create");
        return personService.findById(personId)
                .map(person -> {
                    country.setPerson(person);
                    Country saved = countryRepository.save(country);
                    cacheCountry(saved);
                    invalidateAllCountriesCache();
                    return saved;
                })
                .orElse(null);
    }

    public String getCountryByCode(String code) {
        requestCounter.increment("CountryService.getCountryByCode");
        List<Country> allCountries = findAll();
        return allCountries.stream()
                .filter(c -> c.getCode().equalsIgnoreCase(code))
                .findFirst().toString();
    }

    public String getCodeByCountry(String countryName) {
        requestCounter.increment("CountryService.getCodeByCountry");
        List<Country> allCountries = findAll();
        return allCountries.stream()
                .filter(c -> c.getName().equalsIgnoreCase(countryName))
                .findFirst()
                .map(Country::getCode).toString();
    }

    public List<Country> createAll(List<Country> countries, Integer personId) {
        requestCounter.increment("CountryService.createAll");
        return personService.findById(personId)
                .map(person -> {
                    return countries.stream()
                            .peek(c -> c.setPerson(person))
                            .map(countryRepository::save)
                            .peek(this::cacheCountry)
                            .collect(Collectors.toList());
                })
                .orElse(List.of());
    }

    public Country update(Integer id, Country countryDetails) {
        requestCounter.increment("CountryService.update");
        return countryRepository.findById(id)
                .map(existing -> {
                    clearCountryCache(existing);
                    existing.setName(countryDetails.getName());
                    existing.setCode(countryDetails.getCode());
                    Country updated = countryRepository.save(existing);
                    cacheCountry(updated);
                    invalidateAllCountriesCache();
                    return updated;
                })
                .orElse(null);
    }

    public List<Country> updateAll(List<Country> updates) {
        requestCounter.increment("CountryService.updateAll");
        return updates.stream()
                .map(update -> countryRepository.findById(update.getId())
                        .map(existing -> {
                            clearCountryCache(existing);
                            existing.setName(update.getName());
                            existing.setCode(update.getCode());
                            return countryRepository.save(existing);
                        })
                        .orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    public void delete(Integer id) {
        requestCounter.increment("CountryService.delete");
        countryRepository.findById(id).ifPresent(country -> {
            clearCountryCache(country);
            invalidateAllCountriesCache();
            countryRepository.deleteById(id);
        });
    }

    private void cacheCountry(Country country) {
        commonCache.putWithId(CACHE_NAME, country.getId(), country);
    }

    private void clearCountryCache(Country country) {
        commonCache.removeById(CACHE_NAME, country.getId());
    }

    private void invalidateAllCountriesCache() {
        commonCache.put(ALL_COUNTRIES_KEY, null);
    }
}