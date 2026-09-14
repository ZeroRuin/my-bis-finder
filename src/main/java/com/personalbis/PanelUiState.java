package com.personalbis;
public enum PanelUiState {
 LOGGED_OUT, NO_TARGET, CHOOSE_STYLE, RESULTS;
 public static PanelUiState of(boolean loggedIn,boolean target,boolean bank,boolean style){
  if(!loggedIn)return LOGGED_OUT;
  if(!target)return NO_TARGET;
  if(!style)return CHOOSE_STYLE;
  return bank?RESULTS:CHOOSE_STYLE;
 }
}
