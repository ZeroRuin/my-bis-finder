package com.personalbis;

import java.util.*;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToIntFunction;

/** Bounded snapshot shortlist; only finalists receive remaining-fight TTK. */
final class VardorvisShortlist
{
    private VardorvisShortlist() {}
    static int[] checkpoints(int startingHp) {
        int hp=Math.max(1,startingHp);
        return java.util.stream.IntStream.of(hp,Math.max(1,(int)((long)hp*5/7)),
            Math.max(1,(int)((long)hp*3/7)),Math.max(1,hp/7)).distinct().toArray();
    }
    static <T> List<T> select(List<T> candidates,int startingHp,int perPoint,
        ToDoubleBiFunction<T,Integer> score,ToIntFunction<T> tieBreak) {
        LinkedHashSet<T> selected=new LinkedHashSet<>();
        for(int hp:checkpoints(startingHp)) {
            CalculationCancellation.check();
            Map<T,Double> scores=new IdentityHashMap<>();
            for(T candidate:candidates) {
                CalculationCancellation.check();scores.put(candidate,score.applyAsDouble(candidate,hp));
            }
            List<T> ordered=new ArrayList<>(candidates);
            ordered.sort((a,b)->{
                int d=Double.compare(scores.get(b),scores.get(a));
                return d!=0?d:Integer.compare(tieBreak.applyAsInt(b),tieBreak.applyAsInt(a));
            });
            selected.addAll(ordered.subList(0,Math.min(perPoint,ordered.size())));
        }
        return new ArrayList<>(selected);
    }
}
