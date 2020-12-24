package nsu.ui;

import java.util.ArrayList;

public class CharData {
    private ArrayList<Object[]> hM;
    private Float imt;
    private String category;
    private ArrayList<Object[]> sevenDays;
    private ArrayList<Object[]> thirtyDays;
    private String dynamicsSeven;
    private String dynamicsThirty;

    public ArrayList<Object[]> gethM() {
        return hM;
    }

    public ArrayList<Object[]> getSevenDays() {
        return sevenDays;
    }

    public ArrayList<Object[]> getThirtyDays() {
        return thirtyDays;
    }

    public Float getImt() {
        return imt;
    }

    public void sethM(ArrayList<Object[]> hM) {
        this.hM = hM;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImt(Float imt) {
        this.imt = imt;
    }

    public void setSevenDays(ArrayList<Object[]> sevenDays) {
        this.sevenDays = sevenDays;
    }

    public void setThirtyDays(ArrayList<Object[]> thirtyDays) {
        this.thirtyDays = thirtyDays;
    }

    public String getDynamicsSeven() {
        return dynamicsSeven;
    }

    public String getDynamicsThirty() {
        return dynamicsThirty;
    }

    public void setDynamicsSeven(String dynamicsSeven) {
        this.dynamicsSeven = dynamicsSeven;
    }

    public void setDynamicsThirty(String dynamicsThirty) {
        this.dynamicsThirty = dynamicsThirty;
    }
}
