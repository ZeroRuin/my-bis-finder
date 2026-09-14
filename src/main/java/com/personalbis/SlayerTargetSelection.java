package com.personalbis;
public final class SlayerTargetSelection {
 private SlayerTargetSelection(){}
 public static boolean targetReady(boolean slayerSelected,int variantCount){
  return slayerSelected && variantCount>0;
 }
}
