package com.app.afridge.dom;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


/**
 * Ingredient names
 * <p/>
 * Created by drakuwa on 1/27/15.
 */
@Table(name = "Ingredients")
public class Ingredient extends Model {

    @Column(name = "ingredient_id", index = true)
    private int id;

    @Column(name = "name")
    private String name;

    public Ingredient() {
        // empty constructor
        super();
    }

    public Ingredient(int id, String name) {

        super();
        this.id = id;
        this.name = name;
    }

    public int getIngredientId() {

        return id;
    }

    public void setIngredientId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String toString() {

        String ingredient = "";
        ingredient += "id: " + getId() + "\n";
        ingredient += "ingredient_id: " + getIngredientId() + "\n";
        ingredient += "name: " + getName() + "\n";
        return ingredient;
    }
}
