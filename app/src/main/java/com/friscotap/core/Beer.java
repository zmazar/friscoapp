package com.friscotap.core;

import java.io.Serializable;

public class Beer
        implements Serializable {
    private static final long serialVersionUID = 1L;

    // Private members
    private int ounces;
    private boolean active;
    private boolean newBeer;
    private int     rating;
    private long    id;
    private long 	frisco_id;
    private String 	name;
    private String 	abv;

    /*
     * Constructors
     */
    public Beer() {
        ounces = 16;
        active = false;
        newBeer = false;
        rating = 0;
        id = 0;
        frisco_id = 0;
        name = "";
        abv = "";
    }

    public Beer(String beerName) {
        ounces = 16;
        active = false;
        newBeer = false;
        rating = 0;
        id = 0;
        frisco_id = 0;
        name = beerName;
        abv = "";
    }

    public Beer(Beer copy) {
        ounces = copy.ounces;
        active = copy.active;
        newBeer = copy.newBeer;
        rating = copy.rating;
        id = copy.id;
        frisco_id = copy.frisco_id;
        name = copy.name;
        abv = copy.abv;
    }

    /*
     * Getters for private members
     */
    public long getId() {
        return id;
    }

    public long getFriscoId() {
        return frisco_id;
    }

    public int getRating() {
        return rating;
    }

    public String getName() {
        return name;
    }

    public String getAbv() {
        return abv;
    }

    public int getOunces() {
        return ounces;
    }

    public int getActive() {
        if(active) {
            return 1;
        }
        else {
            return 0;
        }
    }

    public boolean isActive() {
        return active;
    }

    public int getNewBeer() {
        if(newBeer) {
            return 1;
        }
        else {
            return 0;
        }
    }

    public boolean isNewBeer() {
        return newBeer;
    }

    /*
     * Setters for private members
     */
    public void setId(long l) {
        id = l;
    }

    public void setFriscoId(long l) {
        frisco_id = l;
    }

    public void setRating(int i) {
        rating = i;
    }

    public void setName(String s) {
        name = s;
    }

    public void setAbv(String s){
        abv = s;
    }

    public void setOunces(int i) {
        ounces = i;
    }

    public void setActive(boolean b) {
        active = b;
    }

    public void setActive(int i) {
        if(i == 0) {
            active = false;
        }
        else {
            active = true;
        }
    }

    public void setNewBeer(boolean b) {
        newBeer = b;
    }

    public void setNewBeer(int i) {
        if(i == 0) {
            newBeer = false;
        }
        else {
            newBeer = true;
        }
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) {
            return false;
        }
        if(other == this) {
            return false;
        }
        if(!(other instanceof Beer)) {
            return false;
        }

        Beer rhs = (Beer) other;

        if(rhs.getName().equals(this.name)) {
            return true;
        }

        return false;
    }
}
