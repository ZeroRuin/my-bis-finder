package com.personalbis;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import net.runelite.client.ui.FontManager;
/** Shared sidebar colours, including popup rows, editable text and arrow buttons. */
final class SidebarInputStyle {
 static final Color BACKGROUND=new Color(42,36,30),TEXT=new Color(238,220,172),GOLD=new Color(255,184,38),BORDER=new Color(103,90,67),SELECTED=new Color(73,62,46);
 private SidebarInputStyle(){}
 static void text(JTextField field){field.setFont(FontManager.getRunescapeFont());field.setBackground(BACKGROUND);field.setForeground(TEXT);field.setCaretColor(GOLD);field.setSelectionColor(SELECTED);field.setSelectedTextColor(TEXT);field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),BorderFactory.createEmptyBorder(2,4,2,4)));}
 static JButton arrow(String value){JButton b=new JButton(value);b.setFont(FontManager.getRunescapeFont());b.setForeground(GOLD);b.setBackground(BACKGROUND);b.setBorder(BorderFactory.createEmptyBorder(0,3,0,3));b.setFocusable(false);return b;}
 static void combo(JComboBox<?> box){
  box.setFont(FontManager.getRunescapeFont());box.setForeground(TEXT);box.setBackground(BACKGROUND);box.setBorder(BorderFactory.createLineBorder(BORDER));
  box.setUI(new BasicComboBoxUI(){protected JButton createArrowButton(){return arrow("▼");}});
  box.setRenderer(new DefaultListCellRenderer(){public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus){super.getListCellRendererComponent(list,value,index,selected,focus);setBackground(selected?SELECTED:BACKGROUND);setForeground(TEXT);setFont(FontManager.getRunescapeFont());return this;}});
  if(box.getEditor().getEditorComponent() instanceof JTextField)text((JTextField)box.getEditor().getEditorComponent());
 }
 static void spinner(JSpinner spinner){
  spinner.setFont(FontManager.getRunescapeFont());spinner.setBackground(BACKGROUND);spinner.setBorder(BorderFactory.createLineBorder(BORDER));
  spinner.setUI(new BasicSpinnerUI(){protected Component createNextButton(){JButton b=arrow("▲");installNextButtonListeners(b);return b;}protected Component createPreviousButton(){JButton b=arrow("▼");installPreviousButtonListeners(b);return b;}});
  if(spinner.getEditor() instanceof JSpinner.DefaultEditor){JFormattedTextField field=((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();text(field);field.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));field.setDisabledTextColor(TEXT);}
 }
 static void button(JButton button){button.setFont(FontManager.getRunescapeFont());button.setForeground(GOLD);button.setBackground(BACKGROUND);button.setFocusPainted(false);button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),BorderFactory.createEmptyBorder(3,7,3,7)));}
 static void check(JCheckBox check){check.setOpaque(false);check.setFont(FontManager.getRunescapeFont());check.setForeground(TEXT);check.setFocusPainted(false);}
}
