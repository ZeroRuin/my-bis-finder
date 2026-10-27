package com.personalbis;
import java.util.*;
public final class VardorvisShortlistSelfTest {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args){
  check(Arrays.equals(VardorvisShortlist.checkpoints(700),new int[]{700,500,300,100}),"agreed checkpoints");
  check(Arrays.equals(VardorvisShortlist.checkpoints(1400),new int[]{1400,1000,600,200}),"Awakened proportions");
  check(Arrays.equals(VardorvisShortlist.checkpoints(350),new int[]{350,250,150,50}),"manual starting HP proportions");
  check(Arrays.equals(VardorvisShortlist.checkpoints(1),new int[]{1}),"small HP deduplicated");
  List<Integer> candidates=Arrays.asList(700,500,300,100,999);
  List<Integer> selected=VardorvisShortlist.select(candidates,700,1,(g,hp)->-(double)Math.abs(g-hp),g->0);
  check(selected.equals(Arrays.asList(700,500,300,100)),"preserve winners from every checkpoint, exclude weak candidate");
  check(VardorvisShortlist.select(Arrays.asList(1,2,3),700,2,(g,hp)->1.0,g->g).equals(Arrays.asList(3,2)),"tie preference and duplicate finalist removal");
  check(VardorvisShortlist.select(Collections.<Integer>emptyList(),700,2,(g,hp)->1.0,g->g).isEmpty(),"empty candidates");
  boolean[] ran={false};
  CalculationCancellation.runWhile(()->false,()->{
   VardorvisShortlist.select(candidates,700,2,(g,hp)->1.0,g->g);ran[0]=true;
  });
  check(!ran[0],"cancelled work stopped");
  CalculationCancellation.check();
  System.out.println("PASS four HP checkpoints, scaled manual HP, checkpoint coverage, deduplication, ties and cancellation");
 }
}
