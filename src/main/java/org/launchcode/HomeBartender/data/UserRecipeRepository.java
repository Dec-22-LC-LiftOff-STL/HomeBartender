package org.launchcode.HomeBartender.data;

import org.launchcode.HomeBartender.models.Recipe;
import org.launchcode.HomeBartender.models.RecipeDetail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecipeRepository extends CrudRepository<Recipe, Integer> {


}
