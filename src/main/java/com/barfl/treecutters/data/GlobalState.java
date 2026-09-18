package com.barfl.treecutters.data;

import java.util.ArrayList;
import java.util.List;

public final class GlobalState {

    public double stockValue = 600_000;
    public final List<Double> historicalStocks = new ArrayList<>();
    public double weatherMachineLogs = 0;

    public transient int weatherType = 0;
    public transient int lotteryTime = 0;
    public transient String chatGameSolution = "";
    public transient int hintType = 0;

}
