package com.example.mylab.controller;

import com.example.mylab.model.Country;
import com.example.mylab.service.CountryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@CrossOrigin(origins = "*")
public class CountryCodeController {

    private final CountryService countryService;

    public CountryCodeController(CountryService countryService) {
        this.countryService = countryService;
    }

    @PostMapping
    public ResponseEntity<?> createCountry(@RequestBody Country country) {
        try {
            Country createdCountry = countryService.create(country);
            return ResponseEntity.ok(createdCountry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating country");
        }
    }

    @GetMapping("/code/{countryName}")
    public ResponseEntity<String> getCountryCode(@PathVariable String countryName) {
        String code = countryService.getCodeByCountry(countryName);
        return code != null
                ? ResponseEntity.ok(code)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/country/{code}")
    public ResponseEntity<String> getCountryByCode(@PathVariable String code) {
        String country = countryService.getCountryByCode(code);
        return country != null
                ? ResponseEntity.ok(country)
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Country> getCountryById(@PathVariable Integer id) {
        return countryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Country> updateCountry(
            @PathVariable Integer id,
            @RequestBody Country countryDetails) {
        Country updatedCountry = countryService.update(id, countryDetails);
        return updatedCountry != null
                ? ResponseEntity.ok(updatedCountry)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Integer id) {
        countryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}