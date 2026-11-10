package com.personalbis;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
/** Shared sidebar colours and component styling. */
final class SidebarStyle {
 static final Color BACKGROUND=new Color(42,36,30),TEXT=new Color(238,220,172),GOLD=new Color(255,184,38),BORDER=new Color(103,90,67),SELECTED=new Color(73,62,46);
 static final Color GREEN=new Color(82,255,92),SLOT=new Color(58,51,40),PANEL=ColorScheme.DARKER_GRAY_COLOR;
 private SidebarStyle(){}
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

 static JPanel card(){JPanel p=new JPanel();p.setBackground(PANEL);p.setBorder(new EmptyBorder(4,4,4,4));p.setAlignmentX(Component.LEFT_ALIGNMENT);return p;}
 static Font headerFont(){return FontManager.getRunescapeBoldFont().deriveFont(FontManager.getRunescapeFont().getSize2D());}
 static JLabel sectionLabel(String text){JLabel label=new JLabel(text);label.setFont(headerFont());label.setForeground(GOLD);label.setBorder(new EmptyBorder(0,0,4,0));return label;}
 /** Panel check box: opaque dark background with vertical padding. */
 static void panelCheck(JCheckBox check){check.setFont(FontManager.getRunescapeFont());check.setForeground(TEXT);check.setBackground(ColorScheme.DARK_GRAY_COLOR);check.setFocusPainted(false);check.setOpaque(true);check.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));}
 /** Attack-style radio button with its icon above the label. */
 static void styleRadio(JRadioButton radio){
  radio.setFont(FontManager.getRunescapeFont());radio.setForeground(TEXT);radio.setBackground(new Color(38,38,38));radio.setOpaque(true);radio.setFocusPainted(false);
  radio.setHorizontalAlignment(SwingConstants.CENTER);radio.setBorderPainted(false);radio.setBorder(BorderFactory.createEmptyBorder(3,1,3,1));
  radio.setVerticalTextPosition(SwingConstants.BOTTOM);radio.setHorizontalTextPosition(SwingConstants.CENTER);radio.setIcon(attackStyleIcon(radio.getText()));
  radio.setPreferredSize(new Dimension(38,58));radio.setMinimumSize(new Dimension(34,54));
 }
 static ImageIcon attackStyleIcon(String label){
  String name=label==null?"":label.toLowerCase(java.util.Locale.ROOT);
  try(InputStream in=SidebarStyle.class.getResourceAsStream("/attack-styles/"+name+".png")){return in==null?null:new ImageIcon(ImageIO.read(in));}
  catch(IOException ex){return null;}
 }
 /** Large bold panel button. */
 static JButton panelButton(String text){
  JButton button=new JButton(text);button.setFont(FontManager.getRunescapeBoldFont());button.setForeground(TEXT);button.setBackground(SLOT);
  button.setBorder(new CompoundBorder(new LineBorder(BORDER),new EmptyBorder(4,6,4,6)));button.setFocusPainted(false);
  button.setAlignmentX(Component.CENTER_ALIGNMENT);button.setMaximumSize(new Dimension(200,28));return button;
 }
}
