package org.launchcode.HomeBartender.controllers;

import org.launchcode.HomeBartender.Repositories.CocktailRepository;
import org.launchcode.HomeBartender.Repositories.IngredientsRepository;
import org.launchcode.HomeBartender.Repositories.RecipeRepository;
import org.launchcode.HomeBartender.models.Cocktails;
import org.launchcode.HomeBartender.models.Ingredients;


import org.launchcode.HomeBartender.models.Recipes;
import org.launchcode.HomeBartender.data.SearchData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;

@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired IngredientsRepository ingredientsRepository;
    @Autowired CocktailRepository cocktailRepository;
    @Autowired
    RecipeRepository recipeRepository;

    private String searchTerm;
    private String searchType;


    private ArrayList<Cocktails> keywordSearchResults;
    private ArrayList<Ingredients> ingredientSearchResults;
    private ArrayList<Cocktails> cocktailResults;
    private Iterable<Recipes> resultsRecipes;

    @GetMapping
    public String search(Model model){
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchData", new SearchData());

        return "/search/search_homepage";
    }

    @PostMapping("/results")
    public String searchResults(Model model,
                                @RequestParam String searchTerm,
               @RequestParam String searchType, @Valid SearchData searchData, BindingResult bindingResult) {
        Iterable<Cocktails> keywordResults;
        Iterable<Ingredients> allIngredients;
        Iterable<Recipes> resultsRecipes;
        keywordResults = cocktailRepository.findAll();
        allIngredients = ingredientsRepository.findAll();
        resultsRecipes = recipeRepository.findAll();

        if (searchData.getSearchType().equals("Select Search Type")) {
            bindingResult.rejectValue("searchType", "error.searchType", "Must Select Search Type");
            model.addAttribute("searchData", new SearchData());
            return "/search/search_homepage";

        }
        if (searchData.getSearchTerm() == null) {
            bindingResult.rejectValue("searchTerm", "error.searchTerm", "Must Enter Search Term");
            model.addAttribute("searchData", new SearchData());
            return "/search/search_homepage";
        }

        if (searchType.toLowerCase().equals("keyword")) {
            keywordSearchResults = new ArrayList<>();
            for (Cocktails cocktails: keywordResults) {
                if (cocktails.getStrDrink().toLowerCase().contains(searchTerm)) {
                    keywordSearchResults.add(cocktails);
                }
            }
            model.addAttribute("results", keywordSearchResults);
            model.addAttribute("searchData", new SearchData());
            model.addAttribute("resultsRecipes", resultsRecipes);
            return "/search/search_homepage";

        } else if (searchType.toLowerCase().equals("ingredient")) {
            keywordSearchResults = new ArrayList<>();
            cocktailResults = new ArrayList<>();
            ingredientSearchResults = new ArrayList<>();
            for (Ingredients ingredient: allIngredients) {

                if (ingredient.getIngredient().toLowerCase().contains(searchTerm.toLowerCase())) {
                    System.out.println("2. Right here!");
                    ingredientSearchResults.add(ingredient);
                }
            }

            for (Ingredients ingred : ingredientSearchResults) {
                    Cocktails cocktails = ingred.getRecipes().getCocktails();
                    keywordSearchResults.add(cocktails);
                }
                for (Cocktails cocktail : keywordSearchResults) {
                    if (!cocktailResults.contains(cocktail)) {
                        cocktailResults.add(cocktail);
                    }
                }
            model.addAttribute("results", cocktailResults);
            model.addAttribute("searchData", new SearchData());
            model.addAttribute("resultsRecipes", resultsRecipes);
            return "/search/search_homepage";

        }
        return "/search/search_homepage";
    }

}


