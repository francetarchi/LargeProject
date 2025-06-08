package com.wineadvisor.wineadvisor.service;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.wineadvisor.wineadvisor.DTO.styles.StyleDTO;
import com.wineadvisor.wineadvisor.DTO.styles.FoodDTO;
import com.wineadvisor.wineadvisor.exception.BadRequestException;
import com.wineadvisor.wineadvisor.exception.ResourceAlreadyExistsException;
import com.wineadvisor.wineadvisor.exception.ResourceNotFoundException;
import com.wineadvisor.wineadvisor.model.styles.Style;
import com.wineadvisor.wineadvisor.model.wines.Wine;
import com.wineadvisor.wineadvisor.model.utils.Food;
import com.wineadvisor.wineadvisor.model.utils.Grape;
import com.wineadvisor.wineadvisor.model.utils.Taste;
import com.wineadvisor.wineadvisor.repository.StyleRepository;
import com.wineadvisor.wineadvisor.repository.WineRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class StyleService {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final StyleRepository styleRepository;
    private final WineRepository wineRepository;

    /////////// COSTANTI ////////////
    private static final int PAGE_SIZE = 20;

    

    ////////////////////////////////
    /////// PRIVATE METHODS ////////
    ////////////////////////////////
    
    ////////////////////////////////
    ///// Checking operations //////
    
    // Controlla che la pagina ritornata dalla repo sia valida e che sia consistente rispetto alle opzioni di paginazione richieste dal client.
    private void checkReturnedPage(Page<Style> page, String notFoundMessage) throws ResourceNotFoundException, BadRequestException {
        if (page.getTotalElements() == 0) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        if (page.getPageable().getPageNumber() >= page.getTotalPages()) {
            throw new BadRequestException("Page requested too high.");
        }
    }

    /// END of checking operations //
    /////////////////////////////////
    

    
    /////////////////////////////////
    //////// PUBLIC METHODS /////////
    /////////////////////////////////

    /////////////////////////////////
    /////// CRUD operations /////////

    /// CREATE operations ///
    // Aggiunge uno style alla collection "styles" del database
    public Style createStyle(StyleDTO style) throws ResourceAlreadyExistsException {
        if (!styleRepository.findByName(style.getName()).isEmpty()){
            throw new ResourceAlreadyExistsException("Style " + style.getName() + " already exists.");
        }

        Style newStyle = new Style();

        newStyle.setName(style.getName());
        newStyle.setDescription(style.getDescription());
        newStyle.setInterestingFacts(style.getInterestingFacts());
        newStyle.setFood(new ArrayList<Food>());
        for (FoodDTO food : style.getFood()) {
            Food food_to_add = new Food();
            food_to_add.setName(food.getName());
            food_to_add.setImage(food.getImage());
            newStyle.getFood().add(food_to_add);
        }
        newStyle.setGrapes(new ArrayList<Grape>());
        for (String grape_name : style.getGrapes()) {
            Grape grape_to_add = new Grape();
            grape_to_add.setName(grape_name);
            grape_to_add.setWinesCount(0);
            newStyle.getGrapes().add(grape_to_add);
        }
        newStyle.setTaste(new Taste());
        newStyle.getTaste().setAcidity(style.getTaste().getAcidity());
        newStyle.getTaste().setFizziness(style.getTaste().getFizziness());
        newStyle.getTaste().setIntensity(style.getTaste().getIntensity());
        newStyle.getTaste().setSweetness(style.getTaste().getSweetness());
        newStyle.getTaste().setTannin(style.getTaste().getTannin());

        return styleRepository.save(newStyle);
    }


    /// READ operations ///
    // Restituisce lo style di nome style_name
    public Style getStyleByName(String style_name) throws ResourceNotFoundException {
        Style style = styleRepository.findByName(style_name)
            .orElseThrow(() -> new ResourceNotFoundException("Style " + style_name + " not found."));

        return style;
    }

    // Restituisce tutti gli styles della collection (paginazione)
    public Page<Style> getAllStyles(Integer page) {
        Page<Style> styles = styleRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        checkReturnedPage(styles, "No users found.");
        return styles;
    }
    

    /// UPDATE operations ///
    // Aggiorna uno style già esistente nella collection "styles" del db
    @Transactional
    public Style updateStyle(StyleDTO style) throws ResourceNotFoundException {
        return styleRepository.findByName(style.getName())
            .map(style_to_update -> {

                style_to_update.setDescription(style.getDescription());
                style_to_update.setInterestingFacts(style.getInterestingFacts());
                style_to_update.setFood(new ArrayList<Food>());
                for (FoodDTO food : style.getFood()) {
                    Food food_to_add = new Food();
                    food_to_add.setName(food.getName());
                    food_to_add.setImage(food.getImage());
                    style_to_update.getFood().add(food_to_add);
                }
                style_to_update.setGrapes(new ArrayList<Grape>());
                for (String grape_name : style.getGrapes()) {
                    Grape grape_to_add = new Grape();
                    grape_to_add.setName(grape_name);
                    grape_to_add.setWinesCount(0);
                    style_to_update.getGrapes().add(grape_to_add);
                }
                style_to_update.setTaste(new Taste());
                style_to_update.getTaste().setAcidity(style.getTaste().getAcidity());
                style_to_update.getTaste().setFizziness(style.getTaste().getFizziness());
                style_to_update.getTaste().setIntensity(style.getTaste().getIntensity());
                style_to_update.getTaste().setSweetness(style.getTaste().getSweetness());
                style_to_update.getTaste().setTannin(style.getTaste().getTannin());

                // Devo aggiornare lo stile anche nello StyleEmbedded dei wines che hanno quello stile
                ArrayList<Wine> wines = wineRepository.findByStyle_Name(style.getName());
                for (Wine wine : wines) {
                    wine.getStyle().setDescription(style_to_update.getDescription());
                    wine.getStyle().setInterestingFacts(style_to_update.getInterestingFacts());
                    wine.getStyle().setFood(style_to_update.getFood());
                    wine.getStyle().setGrapes(style_to_update.getGrapes());
                    wineRepository.save(wine);
                }
                
                return styleRepository.save(style_to_update);
            })
            .orElseThrow(
                () -> new ResourceNotFoundException("Style " + style.getName() + " not found.")
            );
    }


    /// DELETE operations ///
    // Elimina tutti gli styles dalla collection "styles" del db
    @Transactional
    public void deleteAll() {
        // Metto style = null in tutti i vini
        ArrayList<Wine> wines = (ArrayList<Wine>) wineRepository.findAll();
        for (Wine wine : wines) {
            wine.setStyle(null);
            wineRepository.save(wine);
        }

        styleRepository.deleteAll();
    }
    
    // Elimina uno style dalla collection "styles" del db
    @Transactional
    public void deleteStyleByName(String style_name) throws ResourceNotFoundException {
        styleRepository.findByName(style_name)
            .orElseThrow(() -> new ResourceNotFoundException("Style " + style_name + " not found."));

        // Metto style = null in tutti i vini con quello style
        ArrayList<Wine> wines = wineRepository.findByStyle_Name(style_name);
        for (Wine wine : wines) {
            wine.setStyle(null);
            wineRepository.save(wine);
        }

        styleRepository.deleteByName(style_name);
    }

    //// END of crud operations ////
    ////////////////////////////////
}
