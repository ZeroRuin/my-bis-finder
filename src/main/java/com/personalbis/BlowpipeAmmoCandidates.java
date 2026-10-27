package com.personalbis;

import java.util.*;
import com.personalbis.wiki.WikiEquipment;
import com.personalbis.BlowpipeAmmoPlan.Pipe;
import com.personalbis.BlowpipeAmmoPlan.Dart;

/** Adapts explicit ammo assumptions into calculator-only candidates. */
public final class BlowpipeAmmoCandidates
{
    private BlowpipeAmmoCandidates() {}
    /** Marker keeps manual internal ammo separate from genuinely owned loose darts. */
    public static final class AssumedDart extends EquipmentCandidate
    {
        public final Pipe pipe;
        private AssumedDart(Pipe pipe,Dart dart,WikiEquipment item)
        {
            super(new BankItem(dart.itemId,1,dart.label),EquipmentSlot.AMMO,item.bonuses.ranged_str,
                0,0,0,item.bonuses.ranged_str,0,0f,0,0,false,"Assumed internal dart",
                RequirementResult.usable("Player-selected internal ammo; contents/count not detected"));
            this.pipe=pipe;
        }
    }

    public static boolean matches(EquipmentCandidate weapon,EquipmentCandidate ammo)
    {
        return weapon!=null&&ammo instanceof AssumedDart
            &&((AssumedDart)ammo).pipe==BlowpipeAmmoPlan.pipe(weapon.getItem().getItemId());
    }

    public static Map<EquipmentSlot,List<EquipmentCandidate>> withAssumptions(
        Map<EquipmentSlot,List<EquipmentCandidate>> ranked,Map<Pipe,Dart> selections,Set<Pipe> scanned)
    {
        Map<EquipmentSlot,List<EquipmentCandidate>> result=new EnumMap<>(EquipmentSlot.class);
        result.putAll(ranked);
        List<EquipmentCandidate> ammo=new ArrayList<>(ranked.getOrDefault(EquipmentSlot.AMMO,Collections.emptyList()));
        for(Pipe pipe:scanned){Dart dart=selections.get(pipe);if(!pipe.accepts(dart))continue;
            WikiEquipment item=WikiEquipmentEngine.canonicalItem(dart.itemId);
            if(item!=null&&item.bonuses!=null)ammo.add(new AssumedDart(pipe,dart,item));
        }
        result.put(EquipmentSlot.AMMO,ammo);
        return result;
    }
}
