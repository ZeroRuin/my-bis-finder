package com.personalbis;
public final class RecommendationReadiness {
 private RecommendationReadiness(){}
 public static boolean stylesVisible(boolean targetSelected){return targetSelected;}
 public static boolean resultsVisible(boolean targetSelected,boolean bankChecked,boolean styleSelected){
  return targetSelected&&bankChecked&&styleSelected;
 }
}
