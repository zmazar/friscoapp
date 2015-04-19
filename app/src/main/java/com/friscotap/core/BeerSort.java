package com.friscotap.core;

import java.util.Comparator;

public class BeerSort implements Comparator<Beer>{

    @Override
    public int compare(Beer lhs, Beer rhs) {
        String leftName = lhs.getName();
        String rightName = rhs.getName();

        return leftName.compareTo(rightName);
    }
}
