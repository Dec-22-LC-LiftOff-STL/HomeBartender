package org.launchcode.HomeBartender.controllers;

import org.launchcode.HomeBartender.Service.UserImageStorageService;
import org.launchcode.HomeBartender.Repositories.UserIngredientRepository;
import org.launchcode.HomeBartender.Repositories.UserInstructionRepository;
import org.launchcode.HomeBartender.Repositories.UserRecipeRepository;
import org.launchcode.HomeBartender.data.CreateRecipeFormData;
import org.launchcode.HomeBartender.data.IngredientFormData;
import org.launchcode.HomeBartender.data.InstructionFormData;
import org.launchcode.HomeBartender.models.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/recipe")
public class RecipeController {

    @Autowired
    private UserRecipeRepository userRecipeRepository;


    @Autowired
    private UserIngredientRepository userIngredientRepository;

    @Autowired
    private UserInstructionRepository userInstructionRepository;

    @Autowired
    private AuthenticationController authenticationController;


    @Autowired
    private UserImageStorageService service;


    @GetMapping("/new-recipe")
    public String createNewRecipe(Model model) {
        model.addAttribute("title", "Create A New Recipe");
        model.addAttribute("h1", "Create a new recipe");
//        model.addAttribute("lead", "Add another drink recipe to your home menu. You'll just need a creative name, the yummy ingredients, and some helpful steps!");
        model.addAttribute("recipeForm", new CreateRecipeFormData());

        return "recipes/create/build-recipe";
    }

    @GetMapping("/add-ingredient-fragment")
    public String getAddIngredientFragment(Model model, @RequestParam("index") int index) {
        model.addAttribute("index", index);
        System.out.println(index);
        model.addAttribute("holdRecipeName", "holdRecipe");;
        model.addAttribute("holdRecipe", new CreateRecipeFormData());

        return "recipes/create/add-ingredient :: add-ingredient-form";

    }

    @GetMapping("/add-instruction-fragment")
    public String getAddInstructionFragment(Model model, @RequestParam("instructionIndex") int index) {
        model.addAttribute("instructionIndex", index);
        System.out.println(index);
        model.addAttribute("holdRecipeName2", "holdRecipe2");;
        model.addAttribute("holdRecipe2", new CreateRecipeFormData());

        return "recipes/create/add-instruction :: add-instruction-form";

    }

    @PostMapping("/new-recipe")
    public String processNewRecipe(@ModelAttribute("recipeForm") @Valid CreateRecipeFormData recipeFormData,/*
                                   @RequestParam("userRecipeImage") MultipartFile multipartFile,*/
                                   HttpServletRequest request,
                                   Errors errors, Model model) throws IOException {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Create A New Recipe");
            model.addAttribute("h1", "Create a new recipe");
            model.addAttribute("recipeForm", new CreateRecipeFormData());
//            model.addAttribute("error", "Test error");

            return "recipes/create/build-recipe";
        }

        // create new User Recipe
        UserRecipe newUserRecipe = new UserRecipe();

        // get and set User
        HttpSession session = request.getSession();
        User user = authenticationController.getUserFromSession(session);
        newUserRecipe.setAuthor(user);

        // get and set Name from form
        newUserRecipe.setName(recipeFormData.getName());
        newUserRecipe.setDescription(recipeFormData.getDescription());

        // get Ingredients from form
        ArrayList<IngredientFormData> ingredientForms = recipeFormData.getIngredientForms();

        // for every Ingredient, set Ingredient Name and Recipe on new User Ingredient
        // add User Ingredient to new User Recipe object
        for (int i = 0; i < ingredientForms.size(); i++) {
            String newIngredientName = ingredientForms.get(i).getName();
            UserIngredient newIngredient = new UserIngredient();
            newIngredient.setName(newIngredientName);
            newIngredient.setRecipe(newUserRecipe);

            newUserRecipe.addIngredient(newIngredient);
        }

        // get Instructions from form
        ArrayList<InstructionFormData> instructionForms = recipeFormData.getInstructionForms();

        // for every Instruction, set Instruction Name and Recipe on new User Instruction
        // add User Instruction to new User Recipe object
        for (int j = 0; j < instructionForms.size(); j++) {
            String newInstructionDescription = instructionForms.get(j).getDescription();
            UserInstruction newInstruction = new UserInstruction();
            newInstruction.setDescription(newInstructionDescription);
            newInstruction.setRecipe(newUserRecipe);

            newUserRecipe.addInstruction(newInstruction);
        }


        // check for Image in form
        // if Image exists, get file name
        // set Image on new User Recipe object
        // if Images does NOT exist, set Image to null on new User Recipe object
        MultipartFile imageFromForm = recipeFormData.getImage();

        if (!imageFromForm.isEmpty()) {

            UserImageData imageData = new UserImageData(imageFromForm.getOriginalFilename(), imageFromForm.getContentType(), imageFromForm.getBytes(), newUserRecipe);

            newUserRecipe.setImage(imageData);

            uploadImage(imageData);

            UserRecipe savedRecipe = userRecipeRepository.save(newUserRecipe);

            // get Ingredients and Instructions from Saved Recipe, and save to repositories
            saveIngredientsAndInstructionsToRepository(savedRecipe);

        } else if (imageFromForm.isEmpty()) {
            newUserRecipe.setImage(null);

            // assign a variable for Saved Recipe in User Recipe Repository
            UserRecipe savedRecipe = userRecipeRepository.save(newUserRecipe);

            // get Ingredients and Instructions from Saved Recipe, and save to repositories
            saveIngredientsAndInstructionsToRepository(savedRecipe);

        }

        return "redirect:/my_recipes";

    }

    public void saveIngredientsAndInstructionsToRepository(UserRecipe userRecipe) {

        for (int k = 0; k < userRecipe.getIngredients().size(); k++) {
            userIngredientRepository.save(userRecipe.getIngredients().get(k));
        }

        for (int l = 0; l < userRecipe.getInstructions().size(); l++) {
            userInstructionRepository.save(userRecipe.getInstructions().get(l));
        }

    }

    @GetMapping("/view")
    public String viewUserRecipe(Model model, @RequestParam Integer recipeId) {

        // get User Recipe to view
        Optional<UserRecipe> result = userRecipeRepository.findById(recipeId);

        // check if valid
        if (result.isEmpty()) {

        } else {
            UserRecipe recipe = result.get();
            model.addAttribute("title", recipe.getName());
            model.addAttribute("recipe", recipe);
//            model.addAttribute("name", recipe.getName());
            model.addAttribute("author", recipe.getAuthor().getUserName());
            model.addAttribute("imageName", recipe.getImage().getName());

        }

        return "recipes/view/view-user-recipe";
    }


    public ResponseEntity<?> uploadImage(@RequestParam("image") UserImageData file) throws IOException {
        String uploadImage = service.uploadImage(file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadImage);
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<?> downloadImage(@PathVariable String fileName) {
        byte[] imageData = service.downloadImage(fileName);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/jpg"))
                .body(imageData);

    }

}
