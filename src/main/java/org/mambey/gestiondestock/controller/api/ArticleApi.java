package org.mambey.gestiondestock.controller.api;
import static org.mambey.gestiondestock.utils.Constants.APP_ROOT;

import java.util.List;

import org.mambey.gestiondestock.dto.ArticleDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping(value = APP_ROOT)
public interface ArticleApi {
    
    @PostMapping(value= "/articles/create")
    @ResponseStatus(HttpStatus.CREATED)
    ArticleDto save(@RequestBody ArticleDto dto);

    @GetMapping(value= "/articles/{idArticle}")
    ArticleDto findById(@PathVariable("idArticle") Integer id);

    @GetMapping(value= "/articles2/{codeArticle}")
    ArticleDto findByCodeArticle(@PathVariable("codeArticle") String codeArticle);

    @GetMapping(value= "/articles/all")
    List<ArticleDto> findAll();

    @DeleteMapping(value= "/articles/delete/{idArticle}")
    void delete(@PathVariable("idArticle") Integer id);
}
