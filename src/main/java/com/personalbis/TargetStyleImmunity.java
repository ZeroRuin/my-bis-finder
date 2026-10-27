package com.personalbis;

import java.awt.Color;
import java.util.function.Supplier;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

/** Target immunity is independent of owned gear and temporary calculation results. */
final class TargetStyleImmunity
{
    private TargetStyleImmunity() {}
    static boolean immune(MonsterDefinition m,AttackStyle style){return WikiTargetEligibility.styleImmune(m,style);}
    static <T> T calculate(MonsterDefinition m,AttackStyle style,Supplier<T> search,Supplier<T> empty)
    {
        return immune(m,style)?empty.get():search.get();
    }
    static String message(MonsterDefinition m,AttackStyle style)
    {
        if(m==null)return "";
        if(FracturedArchiveRules.blade(m)&&style!=null&&style!=AttackStyle.MAGIC&&style!=AttackStyle.RANGED)
            return "Melee recommendations are excluded for Ocellus Phase 2 / The Blade because of the phase mechanics. Use Ranged or Magic.";
        String target=m.getName()+(m.getVersion().isEmpty()?"":" ("+m.getVersion()+")");
        return target+" is immune to this combat style.";
    }
    static void icon(JRadioButton button,boolean immune)
    {
        Object normal=button.getClientProperty("personalbis.normalStyleIcon");
        if(normal==null&&button.getIcon()!=null){normal=button.getIcon();button.putClientProperty("personalbis.normalStyleIcon",normal);}
        if(!immune){if(normal instanceof ImageIcon)button.setIcon((ImageIcon)normal);return;}
        Object muted=button.getClientProperty("personalbis.immuneStyleIcon");
        if(muted==null&&normal instanceof ImageIcon)
        {
            muted=new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon)normal).getImage()));
            button.putClientProperty("personalbis.immuneStyleIcon",muted);
        }
        if(muted instanceof ImageIcon)button.setIcon((ImageIcon)muted);
    }
    static final Color MUTED=new Color(120,120,120);
}
