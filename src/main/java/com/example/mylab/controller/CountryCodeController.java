package com.example.mylab.controller;

import com.example.mylab.model.Country;
import com.example.mylab.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
@Tag(name = "Country API", description = "Управление странами и кодами стран")
public class CountryCodeController {

    @Autowired
    private CountryService countryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("countries", List.of("Russia", "USA", "UK"));
        return "index";
    }

    @Operation(summary = "Получить код страны по названию",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Код страны найден"),
                    @ApiResponse(responseCode = "404", description = "Страна не найдена")
            })
    @GetMapping("/code/{countryName}")
    public ResponseEntity<String> getCountryCode(
            @Parameter(description = "Название страны", example = "Russia")
            @PathVariable String countryName) {
        String code = countryService.getCodeByCountry(countryName);
        return code != null
                ? ResponseEntity.ok(code)
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Получить страну по коду",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Страна найдена"),
                    @ApiResponse(responseCode = "404", description = "Код не найден")
            })
    @GetMapping("/country/{code}")
    public ResponseEntity<String> getCountryByCode(
            @Parameter(description = "Код страны", example = "+7")
            @PathVariable String code) {
        String country = countryService.getCountryByCode(code);
        return country != null
                ? ResponseEntity.ok(country)
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Получить все страны")
    @GetMapping
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryService.findAll());
    }

    @Operation(summary = "Получить страну по ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Страна найдена"),
                    @ApiResponse(responseCode = "404", description = "Страна не найдена")
            })
    @GetMapping("/{id}")
    public ResponseEntity<Country> getCountryById(
            @Parameter(description = "ID страны", example = "1")
            @PathVariable Integer id) {
        return countryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Создать несколько стран",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Страны созданы"),
                    @ApiResponse(responseCode = "400", description = "Неверные параметры запроса")
            })
    @PostMapping("/createAll")
    public ResponseEntity<List<Country>> createCountries(
            @Parameter(description = "Данные стран")
            @RequestBody List<Country> countries,
            @Parameter(description = "ID связанного человека")
            @RequestParam Integer personId) {
        List<Country> createdCountries = countryService.createAll(countries, personId);
        return ResponseEntity.ok(createdCountries);
    }

    @Operation(summary = "Обновить несколько стран",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные обновлены"),
                    @ApiResponse(responseCode = "404", description = "Некоторые страны не найдены")
            })
    @PutMapping("/updateAll")
    public ResponseEntity<List<Country>> updateCountries(
            @Parameter(description = "Новые данные стран")
            @RequestBody List<Country> countryDetails) {
        List<Country> updatedCountries = countryService.updateAll(countryDetails);
        return ResponseEntity.ok(updatedCountries);
    }

    @Operation(summary = "Создать новую страну",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Страна создана"),
                    @ApiResponse(responseCode = "400", description = "Неверные параметры запроса")
            })
    @PostMapping("/create")
    public ResponseEntity<Country> createCountry(
            @Parameter(description = "Данные страны")
            @RequestBody Country country,
            @Parameter(description = "ID связанного человека")
            @RequestParam Integer personId) {
        Country createdCountry = countryService.create(country, personId);
        return createdCountry != null
                ? ResponseEntity.ok(createdCountry)
                : ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Обновить данные страны",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные обновлены"),
                    @ApiResponse(responseCode = "404", description = "Страна не найдена")
            })
    @PutMapping("/{id}")
    public ResponseEntity<Country> updateCountry(
            @Parameter(description = "ID страны для обновления", example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Новые данные страны")
            @RequestBody Country countryDetails) {
        Country updatedCountry = countryService.update(id, countryDetails);
        return updatedCountry != null
                ? ResponseEntity.ok(updatedCountry)
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Удалить страну",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Страна удалена"),
                    @ApiResponse(responseCode = "404", description = "Страна не найдена")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(
            @Parameter(description = "ID страны для удаления", example = "1")
            @PathVariable Integer id) {
        countryService.delete(id);
        return ResponseEntity.noContent().build();
    }

}