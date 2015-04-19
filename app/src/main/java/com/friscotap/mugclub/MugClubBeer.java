package com.friscotap.mugclub;

import java.util.Date;

import com.friscotap.core.Beer;

public class MugClubBeer extends Beer {

    /**
     *  Implement Serializable
     */
    private static final long serialVersionUID = 1L;
    private Date date;
    private boolean confirmed;

    public MugClubBeer() {
        super();
        this.date = new Date();
        this.confirmed = false;
    }

    public MugClubBeer(String beerName) {
        super(beerName);
        this.date = new Date();
        this.confirmed = false;
    }

    public MugClubBeer(Beer copy) {
        super(copy);
        this.date = new Date();
        this.confirmed = false;
    }

    public MugClubBeer(MugClubBeer copy) {
        super(copy);
        this.date = copy.date;
        this.confirmed = copy.confirmed;
    }

    /**
     * Getters
     */
    public Date getDate() {
        return this.date;
    }

    public int getConfirmed() {
        if(confirmed) {
            return 1;
        }
        else {
            return 0;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Setters
     */
    public void setDate(Date d) {
        this.date = d;
    }

    public void setConfirmed(boolean b) {
        this.confirmed = b;
    }

    public void setConfirmed(int i) {
        if(i == 0) {
            this.confirmed = false;
        }
        else {
            this.confirmed = true;
        }
    }
}
