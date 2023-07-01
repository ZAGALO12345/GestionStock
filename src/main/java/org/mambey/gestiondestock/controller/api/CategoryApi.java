package org.mambey.gestiondestock.controller.api;
import static org.mambey.gestiondestock.utils.Constants.APP_ROOT;

import java.util.List;

import org.mambey.gestiondestock.dto.CategoryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping(value = APP_ROOT + "/categories")
public interface CategoryApi {
    
    @PostMapping(value= "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto save(@RequestBody CategoryDto dto);

    @GetMapping(value= "/{idCategory}", produces = MediaType.APPLICATION_JSON_VALUE)
    CategoryDto findById(@PathVariable("idCategory") Integer id);

    @GetMapping(value= "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    List<CategoryDto> findAll();

    @DeleteMapping(value= "/delete/{idCategory}", produces = MediaType.APPLICATION_JSON_VALUE)
    void delete(@PathVariable("idCategory") Integer id);
}
