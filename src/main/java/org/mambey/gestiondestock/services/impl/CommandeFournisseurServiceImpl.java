package org.mambey.gestiondestock.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mambey.gestiondestock.dto.CommandeFournisseurDto;
import org.mambey.gestiondestock.dto.FournisseurDto;
import org.mambey.gestiondestock.dto.LigneCommandeFournisseurDto;
import org.mambey.gestiondestock.exception.EntityNotFoundException;
import org.mambey.gestiondestock.exception.ErrorCodes;
import org.mambey.gestiondestock.exception.InvalidOperationException;
import org.mambey.gestiondestock.exception.InvaliddEntityException;
import org.mambey.gestiondestock.model.Article;
import org.mambey.gestiondestock.model.CommandeFournisseur;
import org.mambey.gestiondestock.model.EtatCommande;
import org.mambey.gestiondestock.model.Fournisseur;
import org.mambey.gestiondestock.model.LigneCommandeFournisseur;
import org.mambey.gestiondestock.repository.ArticleRepository;
import org.mambey.gestiondestock.repository.CommandeFournisseurRepository;
import org.mambey.gestiondestock.repository.FournisseurRepository;
import org.mambey.gestiondestock.repository.LigneCommandeFournisseurRepository;
import org.mambey.gestiondestock.services.CommandeFournisseurService;
import org.mambey.gestiondestock.services.ObjectsValidator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandeFournisseurServiceImpl implements CommandeFournisseurService{

    private final CommandeFournisseurRepository commandeFournisseurRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ArticleRepository articleRepository;
    private LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;

    private final ObjectsValidator<CommandeFournisseurDto> commandeFournisseurValidator;


    @Override
    public CommandeFournisseurDto save(CommandeFournisseurDto dto) {
        
        var violations = commandeFournisseurValidator.validate(dto);

        if(!violations.isEmpty()){
            log.error("La commande fournisseur n'est pas valide {}", dto);
            throw new InvaliddEntityException("Données invalides", ErrorCodes.COMMANDE_FOURNISSEUR_NOT_VALID, violations);
        }

        //On vérifie que le fournisseur existe bien dans la BDD
        Optional<Fournisseur> fournisseur = fournisseurRepository.findById(dto.getFournisseur().getId());
        if(fournisseur.isEmpty()){
            log.warn("Fournisseur with ID {} was not find in the DB", dto.getFournisseur().getId());
            throw new EntityNotFoundException("Aucun fournisseur aavec l'ID " + dto.getFournisseur().getId() + " n'a été trouvé dans la BDD");
        }

        // On vérifie que les différents articles de la commande existent bien dans la BDD
        List<String> articleErrors = new ArrayList<>();
        if(dto.getLigneCommandeFournisseurs() != null){
            dto.getLigneCommandeFournisseurs().forEach(ligneCmdFrn -> {
                if(ligneCmdFrn.getArticle() != null){
                    Optional<Article> article = articleRepository.findById(ligneCmdFrn.getArticle().getId());
                    if(article.isEmpty()){
                        articleErrors.add("L'article avec l'ID "+ ligneCmdFrn.getArticle().getId() + " n'existe pas dans la BDD");
                    }
                }else{
                    articleErrors.add("Impossible d'enregistrer une commande avec un article NULL");
                }
            });
        }

        if(!articleErrors.isEmpty()){
            log.warn("Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND);
            throw new InvaliddEntityException("Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
        }

        CommandeFournisseur savedCmdFrn = commandeFournisseurRepository.save(CommandeFournisseurDto.toEntity(dto));

        if(dto.getLigneCommandeFournisseurs() != null){
            dto.getLigneCommandeFournisseurs().forEach(ligneCmdFrnDto -> {
                LigneCommandeFournisseur ligneCmdFrn = LigneCommandeFournisseurDto.toEntity(ligneCmdFrnDto);
                ligneCmdFrn.setCommandeFournisseur(savedCmdFrn);
                ligneCommandeFournisseurRepository.save(ligneCmdFrn);
            });
        }

        return CommandeFournisseurDto.fromEntity(savedCmdFrn);
        
    }

    @Override
    public CommandeFournisseurDto updateEtatCommande(Integer idCommande, EtatCommande etatCommande) {
        checkIdCommande(idCommande);

        if(!StringUtils.hasLength(String.valueOf(etatCommande))){
            log.error("L'état de la commande client est nul");
            throw new InvalidOperationException(
                "Impossible de modifier l'état de la commande avec un état NULL",
                ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE
            );
        }

        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);

        commandeFournisseur.setEtatCommade(etatCommande);

        return CommandeFournisseurDto.fromEntity(
            commandeFournisseurRepository.save(CommandeFournisseurDto.toEntity(commandeFournisseur))
        );
    }

    @Override
    public CommandeFournisseurDto updateQuantiteCommandee(Integer idCommande, Integer idLigneCommande,
            BigDecimal quantite) {

        checkIdCommande(idCommande);

        checkIdLigneCommande(idLigneCommande);

        if(quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0){
            log.error("La quantite de la ligne de commande est NULL");
            throw new InvalidOperationException(
                "Impossible de modifier la quantité de la ligne de commande avec une valeur NULL",
                ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE
            );
        }

        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);

        Optional<LigneCommandeFournisseur> ligneCommandeFournisseurOptional = ligneCommandeFournisseurRepository.findById(idLigneCommande);

        if(ligneCommandeFournisseurOptional.isEmpty()){
            throw new EntityNotFoundException(
                "Aucune ligne de commande n'a été trouvée avec l'ID "+idLigneCommande,
                ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE
            );
        }

        LigneCommandeFournisseur ligneCommandeFournisseur = ligneCommandeFournisseurOptional.get();
        ligneCommandeFournisseur.setQuantite(quantite);
        ligneCommandeFournisseurRepository.save(ligneCommandeFournisseur);

        return commandeFournisseur;
    }

    @Override
    public CommandeFournisseurDto updateFournisseur(Integer idCommande, Integer idFournisseur) {
        checkIdCommande(idCommande);

        if(idFournisseur == null){
            log.error("L'ID fournisseur is NULL");
            throw new InvalidOperationException(
                "Impossible de modifier la commande avec ID client NULL",
                ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE
            );
        }

        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);

        Optional<Fournisseur> fournisseurOptional = fournisseurRepository.findById(idFournisseur);

        if(fournisseurOptional.isEmpty()){
            throw new EntityNotFoundException(
                "Aucun fournisseur avec l'ID "+idFournisseur+" n'a été trouvé dans la BDD",
                ErrorCodes.FOURNISSEUR_NOT_FOUND
            );
        }

        commandeFournisseur.setFournisseur(FournisseurDto.fromEntity(fournisseurOptional.get()));

        return CommandeFournisseurDto.fromEntity(
            commandeFournisseurRepository.save(CommandeFournisseurDto.toEntity(commandeFournisseur))
        );
    }

    @Override
    public CommandeFournisseurDto updateArticle(Integer idCommande, Integer idLigneCommande, Integer idArticle) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);
        checkIdArticle(idArticle);

        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);
        Optional<LigneCommandeFournisseur> ligneCommandeFournisseurOptional = checkLigneCommande(idLigneCommande);
        Optional<Article> articleOptional = articleRepository.findById(idArticle);

        if(articleOptional.isEmpty()){
            throw new EntityNotFoundException(
                "Aucun article avec l'ID "+idArticle+" n'a été trouvé dans la BDD",
                ErrorCodes.ARTICLE_NOT_FOUND
            );
        }

        LigneCommandeFournisseur ligneCommandeFournisseurToSave = ligneCommandeFournisseurOptional.get();
        ligneCommandeFournisseurToSave.setArticle(articleOptional.get());

        ligneCommandeFournisseurRepository.save(ligneCommandeFournisseurToSave);

        return commandeFournisseur;
    }

    @Override
    public CommandeFournisseurDto deleteArticle(Integer idCommande, Integer idLigneCommande) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);
        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);
        //Juste pour vérifier que la ligne de commande fournisseur existe
        checkLigneCommande(idLigneCommande);
        ligneCommandeFournisseurRepository.deleteById(idLigneCommande);


        return commandeFournisseur;
    }

    @Override
    public CommandeFournisseurDto findById(Integer id) {
        if(id == null){
            log.error("Commande fournisseur ID is null");
            return null;
        }

        Optional<CommandeFournisseur> commandeFournisseur = commandeFournisseurRepository.findById(id);

        return commandeFournisseur.map(CommandeFournisseurDto::fromEntity)
                      .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande fournisseur avec l'ID " + id + " n'a été trouvée dans la BDD", 
                        ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND));
    }

    @Override
    public CommandeFournisseurDto findByCode(String code) {
        
        if(!StringUtils.hasLength(code)){
            log.error("Commande fournisseur CODE is null");
            return null;
        }

        Optional<CommandeFournisseur> cmdFrn = commandeFournisseurRepository.findCommandFournisseurByCode(code);

        return cmdFrn.map(CommandeFournisseurDto::fromEntity)
                      .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande fournisseur avec le Code " + code + " n'a été trouvé dans la BDD", 
                        ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND));
    }

    @Override
    public List<CommandeFournisseurDto> findAll() {
        return commandeFournisseurRepository.findAll().stream()
                    .map(CommandeFournisseurDto::fromEntity)
                    .collect(Collectors.toList());
    }

    @Override
    public List<LigneCommandeFournisseurDto> findAllLignesCommandesFournisseurByCommandeFournisseurId(
            Integer idCommande) {
        
        return ligneCommandeFournisseurRepository.findAllByCommandeFournisseurId(idCommande).stream()
                .map(LigneCommandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {

        if(id == null){
            log.error("Command fournisseur ID is null");
        }

        commandeFournisseurRepository.deleteById(id);
    }

    private void checkIdCommande(Integer idCommande){

        if(idCommande == null){
            log.error("Command client ID is null");
            throw new InvalidOperationException(
                "Impossible de modifier l'état de la commande avec un ID NULL",
                ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE
            );
        }
    }

    private void checkIdLigneCommande(Integer idLigneCommande){

        if(idLigneCommande == null){
            log.error("L'ID de la ligne de commande est NULL");
            throw new InvalidOperationException(
                "Impossible de modifier la quantité de la ligne de commande avec ID NULL",
                ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE
            );
        }
    }

    private void checkIdArticle(Integer idArticle){

        if(idArticle == null){
            log.error("L'ID du nouvel article est NULL");
            throw new InvalidOperationException(
                "Impossible de modifier l'état de la commande avec un ID article null",
                ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE
            );
        }
    }

    private CommandeFournisseurDto checkEtatCommande(Integer idCommande){

        CommandeFournisseurDto commandeFournisseur = findById(idCommande);
        if(commandeFournisseur.isCommandeLivree()){
            throw new InvalidOperationException(
                "Impossible de modifier de modifier la commande lorsqu'elle est livrée",
                ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE
            );
        }

        return commandeFournisseur;
    }

    private Optional<LigneCommandeFournisseur> checkLigneCommande(Integer idLigneCommande){

        Optional<LigneCommandeFournisseur> ligneCommandeClientOptional =  ligneCommandeFournisseurRepository.findById(idLigneCommande);

        if(ligneCommandeClientOptional.isEmpty()){
            throw new EntityNotFoundException(
                "Aucune ligne de commande n'a été trouvée avec l'ID "+idLigneCommande,
                ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE
            );
        }

        return  ligneCommandeClientOptional;
    }

}
