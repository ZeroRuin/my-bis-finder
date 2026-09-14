package com.personalbis;
import java.util.Collections;
import java.util.List;
public final class BankStyleSelection {
 private BankStyleSelection(){}
 public static List<AttackStyle> visibleStyles(AttackStyle selected){
  return selected==null?Collections.emptyList():Collections.singletonList(selected);
 }
}
