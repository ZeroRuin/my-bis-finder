package com.personalbis;

import java.awt.*;
import javax.swing.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/** Conditional controls; only immutable snapshots leave Swing's event thread. */
final class RaidOptionsPanel extends JPanel
{
    final JSpinner coxHp=spinner(99,1,99), coxCombat=spinner(126,3,126), coxParty=spinner(1,1,100);
    final JSpinner raidLevel=spinner(0,0,700), pathLevel=spinner(0,0,6), toaParty=spinner(1,1,8);
    final JCheckBox challenge=new JCheckBox("Challenge mode"), excludeSalts=new JCheckBox("Exclude Smelling salts");
    final JPanel cox=new JPanel(), toa=new JPanel();
    private volatile RaidSettings snapshot=RaidSettings.DEFAULT;

    private final Font normalFont,boldFont;

    RaidOptionsPanel(Runnable onChange)
    {
        this(onChange,FontManager.getRunescapeFont(),FontManager.getRunescapeBoldFont());
    }

    RaidOptionsPanel(Runnable onChange,Font normalFont,Font boldFont)
    {
        this.normalFont=normalFont;this.boldFont=boldFont;
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setOpaque(false);setAlignmentX(LEFT_ALIGNMENT);
        card(cox,"Chambers of Xeric");card(toa,"Tombs of Amascut");
        row(cox,"Party HP level",coxHp);row(cox,"Party combat level",coxCombat);row(cox,"Party size",coxParty);
        row(toa,"Raid level",raidLevel);row(toa,"Path level",pathLevel);row(toa,"Party size",toaParty);
        check(cox,challenge);check(toa,excludeSalts);
        coxHp.setToolTipText("Highest party Hitpoints level (maximum 99).");
        coxCombat.setToolTipText("Highest party combat level (maximum 126).");
        pathLevel.setToolTipText("Applies to path bosses and supported adds; Wardens ignore path level.");
        excludeSalts.setToolTipText("Assume Smelling salts unless excluded. Excluding uses supported owned potion boosts, or base levels.");
        add(cox);add(toa);setVisible(false);
        for(JSpinner s:new JSpinner[]{coxHp,coxCombat,coxParty,raidLevel,pathLevel,toaParty}) s.addChangeListener(e->changed(onChange));
        challenge.addActionListener(e->changed(onChange));excludeSalts.addActionListener(e->changed(onChange));
    }
    RaidSettings settings(){return snapshot;}
    void commitEditors() throws java.text.ParseException
    {
        if(cox.isVisible())for(JSpinner s:new JSpinner[]{coxHp,coxCombat,coxParty})s.commitEdit();
        if(toa.isVisible())for(JSpinner s:new JSpinner[]{raidLevel,pathLevel,toaParty})s.commitEdit();
    }
    void showFor(MonsterDefinition m)
    {
        boolean c=RaidScaling.isCox(m),t=RaidScaling.isToa(m);
        cox.setVisible(c);toa.setVisible(t);setVisible(c||t);
        setMaximumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
    }
    private void changed(Runnable callback)
    {
        snapshot=new RaidSettings(number(coxHp),number(coxCombat),number(coxParty),challenge.isSelected(),
            number(raidLevel),number(pathLevel),number(toaParty),excludeSalts.isSelected());
        challenge.setForeground(challenge.isSelected()?new Color(82,255,92):new Color(238,220,172));
        excludeSalts.setForeground(excludeSalts.isSelected()?new Color(82,255,92):new Color(238,220,172));callback.run();
    }
    private static int number(JSpinner s){return ((Number)s.getValue()).intValue();}
    private static JSpinner spinner(int value,int min,int max)
    {
        JSpinner s=new JSpinner(new SpinnerNumberModel(value,min,max,1));s.setEditor(new JSpinner.NumberEditor(s,"0"));
        JFormattedTextField text=((JSpinner.DefaultEditor)s.getEditor()).getTextField();text.setColumns(3);
        text.setBackground(ColorScheme.DARKER_GRAY_COLOR);text.setForeground(new Color(238,220,172));
        Color gold=new Color(255,184,38);
        s.setBorder(BorderFactory.createLineBorder(gold));
        ((JSpinner.DefaultEditor)s.getEditor()).setBorder(BorderFactory.createEmptyBorder());
        text.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
        for(Component c:s.getComponents())if(c instanceof AbstractButton)
        {
            AbstractButton arrow=(AbstractButton)c;
            arrow.setBorder(BorderFactory.createLineBorder(new Color(103,90,67)));
            arrow.setBackground(new Color(58,51,40));arrow.setForeground(gold);
            arrow.setBorderPainted(true);arrow.setOpaque(true);
        }
        ((javax.swing.text.DefaultFormatter)text.getFormatter()).setCommitsOnValidEdit(true);
        s.setPreferredSize(new Dimension(62,26));return s;
    }
    private void card(JPanel p,String title)
    {
        p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setAlignmentX(LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createEmptyBorder(5,4,8,4));
        JLabel h=new JLabel(title);h.setForeground(new Color(255,184,38));h.setFont(boldFont);p.add(h);
    }
    private void row(JPanel p,String title,JSpinner s)
    {
        JPanel row=new JPanel(new BorderLayout(4,0));row.setOpaque(false);row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel label=new JLabel(title);label.setFont(normalFont);label.setForeground(new Color(238,220,172));
        ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setFont(normalFont);
        row.add(label,BorderLayout.CENTER);row.add(s,BorderLayout.EAST);row.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));p.add(row);
    }
    private void check(JPanel p,JCheckBox c)
    {
        c.setOpaque(false);c.setFont(normalFont);c.setForeground(new Color(238,220,172));
        c.setAlignmentX(LEFT_ALIGNMENT);c.setFocusPainted(false);p.add(c);
    }
}
