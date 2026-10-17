package com.personalbis;

import java.awt.*;
import javax.swing.*;
import net.runelite.client.ui.FontManager;

/** Fourth equipment row: two item tiles and a double-width combat-style button. */
final class LoadoutActionRow extends JPanel
{
    final JLabel spec=tile("Spec"),spell=tile("Spell");
    private Image button;
    final JLabel style=new JLabel("—",SwingConstants.CENTER){
        @Override protected void paintComponent(Graphics g){
            if(button!=null)g.drawImage(button,0,0,getWidth(),getHeight(),this);
            super.paintComponent(g);
        }
    };
    LoadoutActionRow(){
        super(null);setOpaque(false);setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(205,50));setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        add(spec);add(spell);add(style);
        style.setFont(FontManager.getRunescapeFont());style.setForeground(new Color(255,190,90));
        style.setBorder(BorderFactory.createLineBorder(new Color(94,78,52)));
    }
    @Override public void doLayout(){
        int unit=Math.max(0,(getWidth()-8)/4),height=getHeight();
        spec.setBounds(0,0,unit,height);spell.setBounds(unit+4,0,unit,height);
        style.setBounds(2*unit+8,0,Math.max(0,getWidth()-2*unit-8),height);
    }
    private static JLabel tile(String text){
        JLabel l=new JLabel(text,SwingConstants.CENTER);l.setFont(FontManager.getRunescapeFont());
        l.setForeground(new Color(220,210,190));l.setOpaque(true);l.setBackground(new Color(38,38,38));
        l.setPreferredSize(new Dimension(48,50));l.setMinimumSize(new Dimension(40,40));return l;
    }
    void setButton(Image image){button=image;style.repaint();}
    void clear(){spec.setIcon(null);spec.setText("Spec");spell.setIcon(null);spell.setText("Spell");style.setText("—");}
}
