package org.mambey.gestiondestock.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.mambey.gestiondestock.model.Client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientDto {
    
    private Integer id;

    @NotBlank(message = "Veuillez renseigner le nom du client")
    private String nom;

    @NotBlank(message = "Veuillez renseigner le prenom du client")
    private String prenom;

    @NotNull(message = "Veuillez renseigner l'adresse du client")
    private AdresseDto adresse;

    private String photo;

    @NotBlank(message = "Veuillez renseigner le mail du client")
    private String mail;

    @NotBlank(message = "Veuillez renseigner le numero de téléphone")
    private String numTel;

    @JsonIgnore
    private List<CommandeClientDto> commandeClients;

    public static ClientDto fromEntity(Client client){
        if(client == null){
            return null;
        }

        return ClientDto.builder()
            .id(client.getId())
            .nom(client.getNom())
            .prenom(client.getPrenom())
            .adresse(AdresseDto.fromEntity(client.getAdresse()))
            .photo(client.getPhoto())
            .mail(client.getMail())
            .numTel(client.getNumTel())
            .build();
    }

    public static Client toEntity(ClientDto clientDto){
        if(clientDto == null){
            return null;
        }

        Client client = new Client();
        client.setId(clientDto.getId());
        client.setNom((clientDto.getNom()));
        client.setPrenom(clientDto.getPrenom());
        client.setAdresse(AdresseDto.toEntity(clientDto.getAdresse()));
        client.setPhoto(clientDto.getPhoto());
        clientDto.setMail(client.getMail());
        client.setNumTel(clientDto.getNumTel());

        return client;
    }
}
