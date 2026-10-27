package com.personalbis;

public enum FillPreference
{
    PRAYER("Prayer fill"), DEFENCE("Defence fill");
    public static final String CONFIG_KEY="fillPreference";
    private final String label;
    FillPreference(String label){this.label=label;}
    public static FillPreference decode(String raw)
    {
        try{return valueOf(raw);}catch(IllegalArgumentException|NullPointerException ignored){return PRAYER;}
    }
    @Override public String toString(){return label;}
}
