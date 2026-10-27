package com.personalbis;

import java.awt.*;
import javax.swing.*;
import net.runelite.client.ui.FontManager;

/** Player-entered planning snapshot only; never reads or tracks a live NPC's HP. */
final class VardorvisHpOptions extends JPanel
{
    final JSpinner startingHp=new JSpinner(new SpinnerNumberModel(700,1,700,1));
    private final JLabel maximum=new JLabel("/ 700");
    private final Runnable changed;
    private MonsterDefinition target;
    private boolean restoring;
    private volatile VardorvisHpSettings settings=VardorvisHpSettings.DEFAULT;
    VardorvisHpOptions(Runnable changed){
        this.changed=changed;
        setLayout(new FlowLayout(FlowLayout.LEFT,4,3));setOpaque(false);setAlignmentX(LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE,33));
        JLabel label=new JLabel("Starting HP");label.setFont(FontManager.getRunescapeFont());label.setForeground(new Color(238,220,172));
        maximum.setFont(FontManager.getRunescapeFont());maximum.setForeground(new Color(238,220,172));
        SidebarInputStyle.spinner(startingHp);
        startingHp.setPreferredSize(new Dimension(65,25));
        startingHp.setToolTipText("Manual starting HP. Accuracy/DPS use this snapshot; TTK scales Defence as remaining HP falls. Regenerate to apply.");
        add(label);add(startingHp);add(maximum);setVisible(false);
        startingHp.addChangeListener(e->{
            if(restoring||target==null)return;
            settings=settings.with(target,((Number)startingHp.getValue()).intValue());
            changed.run();
        });
    }
    VardorvisHpSettings settings(){return settings;}
    void restore(VardorvisHpSettings saved){settings=saved;updateEditor();}
    void showFor(MonsterDefinition selected){
        boolean visible=VardorvisRules.supported(selected);setVisible(visible);
        if(!visible){target=null;return;}
        if(target!=null&&target.getId()==selected.getId()&&target.getVersion().equals(selected.getVersion()))return;
        target=selected;updateEditor();
    }
    private void updateEditor(){
        if(target==null)return;
        restoring=true;
        try{
            int max=VardorvisRules.maximumHp(target);
            startingHp.setModel(new SpinnerNumberModel(settings.hp(target),1,max,1));
            SidebarInputStyle.spinner(startingHp);
            maximum.setText("/ "+max);
        }finally{restoring=false;}
    }
    void commitEditor()throws java.text.ParseException {if(isVisible())startingHp.commitEdit();}
}
