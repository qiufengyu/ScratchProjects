package com.apriori;

public class AprioriResult implements Comparable<AprioriResult>{

    String itemSets;
    double sup;

    public AprioriResult() {
    }

    public AprioriResult(String itemSets, double sup) {
        this.itemSets = itemSets;
        this.sup = sup;
    }

    public String getItemSets() {
        return itemSets;
    }

    public void setItemSets(String itemSets) {
        this.itemSets = itemSets;
    }

    public double getSup() {
        return sup;
    }

    public void setSup(double sup) {
        this.sup = sup;
    }

    @Override
    public int compareTo(AprioriResult o) {
        if (o.getSup() == this.sup)
            return 0;
        return this.sup > o.getSup() ? -1:1;
    }
}
