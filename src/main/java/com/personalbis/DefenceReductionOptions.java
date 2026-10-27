package com.personalbis;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.FontManager;

/** Ownership and profile preferences control visibility; hidden effects never remain active. */
final class DefenceReductionOptions extends JPanel {
 private static final String MODEL="Pre-applied successful effects, held fixed for the estimate. No special damage, setup time, energy or recovery in normal TTK. Accursed and Vulnerability are alternatives. Vulnerability uses standard 10%; enhanced Tome effect excluded.";
 private final ConfigManager config;private final Runnable changed;
 private Set<DefenceReductionPlan.Type> owned=EnumSet.noneOf(DefenceReductionPlan.Type.class);
 private Set<DefenceReductionPlan.Type> hidden=EnumSet.noneOf(DefenceReductionPlan.Type.class);
 private DefenceReductionPlan saved,selected=DefenceReductionPlan.OFF;
 private boolean ownershipComplete,restoring;
 private final JButton resetButton=new JButton("Reset reductions");
 private final Map<DefenceReductionPlan.Type,JSpinner> spinners=new EnumMap<>(DefenceReductionPlan.Type.class);
 DefenceReductionOptions(ConfigManager config,Runnable changed){
  this.config=config;this.changed=changed;
  saved=DefenceReductionPlan.decode(config.getConfiguration("personalbis",ManualDefenceReduction.CONFIG_KEY));
  setOpaque(false);setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setAlignmentX(LEFT_ALIGNMENT);setVisible(false);
  hidden=readHidden();clearHidden();
  SidebarInputStyle.button(resetButton);resetButton.setAlignmentX(LEFT_ALIGNMENT);resetButton.setToolTipText("Clear all selected defence reductions; other Advanced settings stay unchanged.");resetButton.addActionListener(e->reset());
 }
 private static String tooltip(String text){return "<html><body style='width:220px'>"+text+"</body></html>";}
 private Set<DefenceReductionPlan.Type> readHidden(){
  Set<DefenceReductionPlan.Type> result=EnumSet.noneOf(DefenceReductionPlan.Type.class);
  for(DefenceReductionPlan.Type type:DefenceReductionPlan.Type.values())
   if(Boolean.parseBoolean(config.getConfiguration("personalbis","hideReduction"+type.name())))result.add(type);
  return result;
 }
 private Set<DefenceReductionPlan.Type> visibleTypes(){
  Set<DefenceReductionPlan.Type> result=EnumSet.noneOf(DefenceReductionPlan.Type.class);result.addAll(owned);result.removeAll(hidden);return result;
 }
 private void clearHidden(){
  DefenceReductionPlan next=saved;for(DefenceReductionPlan.Type type:hidden)next=next.with(type,0);
  if(!next.encode().equals(saved.encode())){saved=next;save();}
 }
 DefenceReductionPlan selected(){return selected;}
 boolean hasOptions(){return !visibleTypes().isEmpty();}
 int activeCount(){int count=0;for(DefenceReductionPlan.Type type:DefenceReductionPlan.Type.values())if(selected.amount(type)>0)count++;return count;}
 String selectionTooltip(){StringBuilder text=new StringBuilder();for(DefenceReductionPlan.Type type:DefenceReductionPlan.Type.values())if(selected.amount(type)>0)text.append(type.label).append(": ").append(selected.amount(type)).append("; ");return tooltip(text.append(MODEL).toString());}
 void refresh(Set<DefenceReductionPlan.Type> available){refresh(available,true);}
 void refresh(Set<DefenceReductionPlan.Type> available,boolean complete){
  Set<DefenceReductionPlan.Type> preferences=readHidden();
  if(owned.equals(available)&&hidden.equals(preferences)&&ownershipComplete==complete&&selected.encode().equals(saved.revalidate(visibleTypes()).encode()))return;
  String before=selected.encode(),previousSaved=saved.encode();
  owned=EnumSet.noneOf(DefenceReductionPlan.Type.class);owned.addAll(available);ownershipComplete=complete;hidden=preferences;
  clearHidden();selected=saved.revalidate(visibleTypes());
  if(complete&&!saved.encode().equals(selected.encode())){saved=selected;save();}
  rebuild();if(!previousSaved.equals(saved.encode())||!before.equals(selected.encode()))changed.run();
 }
 void reload(){
  saved=DefenceReductionPlan.decode(config.getConfiguration("personalbis",ManualDefenceReduction.CONFIG_KEY));
  hidden=readHidden();clearHidden();selected=saved.revalidate(visibleTypes());
  if(ownershipComplete&&!saved.encode().equals(selected.encode())){saved=selected;save();}rebuild();
 }
 void commitEditors()throws java.text.ParseException{for(JSpinner spinner:new ArrayList<>(spinners.values()))spinner.commitEdit();}
 private void save(){config.setConfiguration("personalbis",ManualDefenceReduction.CONFIG_KEY,saved.encode());}
 private void update(DefenceReductionPlan.Type type,int amount){if(restoring||!visibleTypes().contains(type))return;saved=saved.with(type,amount);selected=saved.revalidate(visibleTypes());save();if(type.maximum==1)rebuild();else resetButton.setEnabled(activeCount()>0);changed.run();}
 private void reset(){saved=DefenceReductionPlan.OFF;selected=saved;save();rebuild();changed.run();}
 private void rebuild(){
  restoring=true;removeAll();spinners.clear();
  JLabel heading=new JLabel("Defensive Reductions");heading.setForeground(SidebarInputStyle.GOLD);heading.setFont(FontManager.getRunescapeFont());heading.setAlignmentX(LEFT_ALIGNMENT);heading.setToolTipText(tooltip(MODEL));add(heading);
  for(DefenceReductionPlan.Type type:DefenceReductionPlan.Type.values())if(visibleTypes().contains(type)){
   String tooltip=tooltip(type.label+(type==DefenceReductionPlan.Type.TONALZTICS?" — individual successful hitsplats. ":" — successful hits or total special damage. ")+MODEL);
   if(type.maximum==1){JCheckBox check=new JCheckBox(type.label,selected.amount(type)>0);SidebarInputStyle.check(check);check.setToolTipText(tooltip);check.setAlignmentX(LEFT_ALIGNMENT);check.addActionListener(e->update(type,check.isSelected()?1:0));add(check);}
   else {
    JPanel row=new JPanel(new BorderLayout(4,0));row.setOpaque(false);row.setAlignmentX(LEFT_ALIGNMENT);row.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));row.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
    JSpinner spinner=new JSpinner(new SpinnerNumberModel(selected.amount(type),0,type.maximum,1));SidebarInputStyle.spinner(spinner);spinner.setToolTipText(tooltip);((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setToolTipText(tooltip);spinner.setPreferredSize(new Dimension(60,26));spinners.put(type,spinner);
    String compact=type==DefenceReductionPlan.Type.BGS?"BGS damage":type==DefenceReductionPlan.Type.DWH?"Warhammer hits":type.label;
    JLabel label=new JLabel(compact);label.setToolTipText(tooltip);label.setFont(FontManager.getRunescapeFont());label.setForeground(SidebarInputStyle.TEXT);
    row.add(spinner,BorderLayout.WEST);row.add(label,BorderLayout.CENTER);add(row);
    spinner.addChangeListener(e->update(type,((Number)spinner.getValue()).intValue()));
   }
  }
  resetButton.setEnabled(activeCount()>0);add(resetButton);add(Box.createVerticalStrut(5));
  setVisible(hasOptions());restoring=false;revalidate();repaint();
 }
}
