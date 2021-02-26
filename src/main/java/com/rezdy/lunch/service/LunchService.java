package com.rezdy.lunch.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rezdy.lunch.entity.Recipe;
import com.rezdy.lunch.helper.LunchServiceHelper;
import com.rezdy.lunch.entity.Ingredient;

@Service
public class LunchService {

    @Autowired
    private EntityManager entityManager;

    private List<Recipe> recipesSorted;

    public List<Recipe> getNonExpiredRecipesOnDate(LocalDate date) {
        List<Recipe> recipes = loadRecipes(date);

        sortRecipes(recipes,date);

        return recipesSorted;
    }

    private List<Recipe> sortRecipes(List<Recipe> recipes, final LocalDate date) {
        List<Recipe> recipesSorted = new ArrayList<>();
        if (recipes != null) {
            recipesSorted.addAll(recipes);
            recipesSorted.sort((o1, o2) -> compareBestBeforeDates(date, o1, o2));
        }
        return recipesSorted;
    }

    private int compareBestBeforeDates(LocalDate date, Recipe o1, Recipe o2) {
        Optional<LocalDate> expireBestBefore = getOldestPastBestBeforeDateFromRecipeIngredients(date, o1);
        Optional<LocalDate> expireBestbefore2 = getOldestPastBestBeforeDateFromRecipeIngredients(date, o2);
        // recipes with an ingredient past its "best before" at the bottom at the list, sorted by
        // "best before" desc, recipe title asc
        if (expireBestBefore.isPresent() && expireBestbefore2.isEmpty()) {
            return 1;
        } else if (expireBestBefore.isEmpty() && expireBestbefore2.isPresent()) {
            return -1;
        } else if (expireBestBefore.isPresent() && expireBestbefore2.isPresent()) {
            int dateComparison = expireBestBefore.get().compareTo(expireBestbefore2.get());
            if (dateComparison != 0) return dateComparison;
        }
        return o1.getTitle().compareTo(o2.getTitle());
    }

    private Optional<LocalDate> getOldestPastBestBeforeDateFromRecipeIngredients(LocalDate date, Recipe o1) {
        return o1.getIngredients().stream()
                .map(Ingredient::getBestBefore)
                .filter(bestBefore -> date.isAfter(bestBefore))
                .min(LocalDate::compareTo);
    }
    public List<Recipe> loadRecipes(LocalDate date)
    {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        List<Recipe> recipeList = null;
        if (cb != null)
        {
            CriteriaQuery<Recipe> criteriaQuery = cb.createQuery(Recipe.class);
            Root<Recipe> recipeRoot = criteriaQuery.from(Recipe.class);
            CriteriaQuery<Recipe> query = criteriaQuery.select(recipeRoot);
            recipeList = entityManager.createQuery(query).getResultList();
        }

        System.out.println("recipeList :: " + recipeList);

        recipeList.forEach(recipe -> System.out.println("Recipe Title ::: " + recipe));

        List<Recipe> validRecipes = recipeList.stream().filter(recipe -> !LunchServiceHelper.isExpired(recipe, date))
                .collect(Collectors.toList());
        return validRecipes;

    }
}
