package com.rezdy.lunch.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Recipe {

    @Id
    private String title;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "recipe_ingredient",
            joinColumns = @JoinColumn(name = "recipe"),
            inverseJoinColumns = @JoinColumn(name = "ingredient"))
    private Set<Ingredient> ingredients;

    public String getTitle() {
        return title;
    }

    public Recipe setTitle(String title) {
        this.title = title;
        return this;
    }

    public Set<Ingredient> getIngredients() {
        return ingredients;
    }

    public Recipe setIngredients(Set<Ingredient> ingredients) {
        this.ingredients = ingredients;
        return this;
    }

    @Override
    public String toString()
    {
        return "Recipe{" + "title='" + title + '\'' + ", ingredients=" + ingredients + '}';
    }
}
