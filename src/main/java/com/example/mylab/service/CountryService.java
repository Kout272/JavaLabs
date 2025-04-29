package com.example.mylab.service;

import com.example.mylab.cache.CommonCache;
import com.example.mylab.counter.RequestCounter;
import com.example.mylab.model.Country;
import com.example.mylab.model.Person;
import com.example.mylab.repository.CountryRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CountryService {
    private static final String CACHE_NAME = "countries";
    private static final String ALL_COUNTRIES_CACHE_KEY = "all_countries";

    private final CountryRepository repository;
    private final PersonService personService;
    private final RequestCounter requestCounter;

    @Autowired
    public CountryService(CountryRepository repository,
                          PersonService personService,
                          RequestCounter requestCounter) {
        this.repository = repository;
        this.personService = personService;
        this.requestCounter = requestCounter;
    }

    @PostConstruct
    public void init() {
        loadCountryCodes();
    }

    private void loadCountryCodes() {
        requestCounter.increment("CountryService.loadCountryCodes");
        String url = "https://www.ixbt.com/mobile/country_code.html";
        try {
            Document document = Jsoup.connect(url).get();
            Elements rows = document.select("table tbody tr");
            for (Element row : rows) {
                Elements columns = row.select("td");
                if (columns.size() >= 3) {
                    String countryName = columns.get(1).text();
                    String countryCode = columns.get(2).text();
                    CommonCache.put("country_code_" + countryName, countryCode);
                    CommonCache.put("country_name_" + countryCode, countryName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCodeByCountry(String countryName) {
        requestCounter.increment("CountryService.getCodeByCountry");
        String cachedCode = CommonCache.get("country_code_" + countryName, String.class);
        if (cachedCode != null) {
            return cachedCode;
        }

        Optional<Country> countryFromDb = repository.findByName(countryName);
        if (countryFromDb.isPresent()) {
            String code = countryFromDb.get().getCode();
            CommonCache.put("country_code_" + countryName, code);
            CommonCache.put("country_name_" + code, countryName);
            return code;
        }
        return null;
    }

    public String getCountryByCode(String code) {
        requestCounter.increment("CountryService.getCountryByCode");
        String cachedName = CommonCache.get("country_name_" + code, String.class);
        if (cachedName != null) {
            return cachedName;
        }

        Optional<Country> countryFromDb = repository.findByCode(code);
        if (countryFromDb.isPresent()) {
            String name = countryFromDb.get().getName();
            CommonCache.put("country_name_" + code, name);
            CommonCache.put("country_code_" + name, code);
            return name;
        }
        return null;
    }

    public List<Country> findAll() {
        requestCounter.increment("CountryService.findAll");
        List<Country> cached = CommonCache.get(ALL_COUNTRIES_CACHE_KEY, List.class);
        if (cached != null) {
            return cached;
        }

        List<Country> countries = repository.findAll();
        cacheCountries(countries);
        CommonCache.put(ALL_COUNTRIES_CACHE_KEY, countries);
        return countries;
    }

    public Optional<Country> findById(Integer id) {
        requestCounter.increment("CountryService.findById");
        Country cached = CommonCache.getById(CACHE_NAME, id, Country.class);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<Country> country = repository.findById(id);
        country.ifPresent(this::cacheCountry);
        return country;
    }

    public Country create(Country country, Integer personId) {
        requestCounter.increment("CountryService.create");
        return personService.findById(personId)
                .map(person -> {
                    country.setPerson(person);
                    Country saved = repository.save(country);
                    cacheCountry(saved);
                    invalidateAllCountriesCache();
                    return saved;
                })
                .orElse(null);
    }

    public List<Country> createAll(List<Country> countries, Integer personId) {
        requestCounter.increment("CountryService.createAll");
        return personService.findById(personId)
                .map(person -> {
                    List<Country> savedCountries = countries.stream()
                            .peek(country -> country.setPerson(person))
                            .map(repository::save)
                            .peek(this::cacheCountry)
                            .collect(Collectors.toList());
                    invalidateAllCountriesCache();
                    return savedCountries;
                })
                .orElse(List.of());
    }

    public Country update(Integer id, Country countryDetails) {
        requestCounter.increment("CountryService.update");
        return repository.findById(id)
                .map(existing -> {
                    clearCountryCache(existing);
                    existing.setName(countryDetails.getName());
                    existing.setCode(countryDetails.getCode());
                    Country updated = repository.save(existing);
                    cacheCountry(updated);
                    invalidateAllCountriesCache();
                    return updated;
                })
                .orElse(null);
    }

    public List<Country> updateAll(List<Country> countryUpdates) {
        requestCounter.increment("CountryService.updateAll");
        return countryUpdates.stream()
                .map(update -> repository.findById(update.getId())
                        .map(existing -> {
                            clearCountryCache(existing);
                            existing.setName(update.getName());
                            existing.setCode(update.getCode());
                            Country updated = repository.save(existing);
                            cacheCountry(updated);
                            return updated;
                        })
                        .orElse(null))
                .filter(country -> country != null)
                .peek(country -> invalidateAllCountriesCache())
                .collect(Collectors.toList());
    }

    public void delete(Integer id) {
        requestCounter.increment("CountryService.delete");
        repository.findById(id).ifPresent(country -> {
            clearCountryCache(country);
            invalidateAllCountriesCache();
            repository.deleteById(id);
        });
    }

    public void deleteAll(List<Integer> ids) {
        requestCounter.increment("CountryService.deleteAll");
        List<Country> countries = repository.findAllById(ids);
        countries.forEach(this::clearCountryCache);
        invalidateAllCountriesCache();
        repository.deleteAllById(ids);
    }

    private void cacheCountry(Country country) {
        CommonCache.putWithId(CACHE_NAME, country.getId(), country);
        CommonCache.put("country_code_" + country.getName(), country.getCode());
        CommonCache.put("country_name_" + country.getCode(), country.getName());
    }

    private void cacheCountries(List<Country> countries) {
        countries.forEach(this::cacheCountry);
    }

    private void clearCountryCache(Country country) {
        CommonCache.removeById(CACHE_NAME, country.getId());
        CommonCache.put("country_code_" + country.getName(), null);
        CommonCache.put("country_name_" + country.getCode(), null);
    }

    private void invalidateAllCountriesCache() {
        CommonCache.put(ALL_COUNTRIES_CACHE_KEY, null);
    }
}