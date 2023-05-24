package org.mambey.gestiondestock.dto;

import java.util.List;

import org.mambey.gestiondestock.model.Entreprise;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EntrepriseDto {
    
    private Integer id;

    private String nom;

    private String description;

    private AdresseDto adresse;

    private String codeFiscal;

    private String photo;

    private String email;

    private String numTel;

    @JsonIgnore
    private List<UtilisateurDto> utilisateurs;

    public static EntrepriseDto fromEntity(Entreprise entreprise){

        if(entreprise == null){
            return null;
        }

        return EntrepriseDto.builder()
            .id(entreprise.getId())
            .nom(entreprise.getNom())
            .description(entreprise.getDescription())
            .adresse(AdresseDto.fromEntity(entreprise.getAdresse()))
            .codeFiscal(entreprise.getCodeFiscal())
            .photo(entreprise.getPhoto())
            .email(entreprise.getEmail())
            .numTel(entreprise.getNumTel())
            .build();
    }

    public static Entreprise toEntity(EntrepriseDto entrepriseDto){

        if(entrepriseDto == null){
            return null;
        }

        Entreprise entreprise = new Entreprise();
        entreprise.setId(entrepriseDto.getId());
        entreprise.setNom(entrepriseDto.getNom());
        entreprise.setDescription(entrepriseDto.getDescription());
        entreprise.setAdresse(AdresseDto.toEntity(entrepriseDto.getAdresse()));
        entreprise.setCodeFiscal(entrepriseDto.getCodeFiscal());
        entreprise.setPhoto(entrepriseDto.getPhoto());
        entreprise.setEmail(entreprise.getEmail());
        entreprise.setNumTel(entrepriseDto.getNumTel());

        return entreprise;
    }
}
