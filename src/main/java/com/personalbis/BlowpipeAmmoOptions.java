package com.personalbis;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import net.runelite.client.config.ConfigManager;

/** Swing-only view of explicit ammo assumptions. Visibility uses personal bank scans. */
final class BlowpipeAmmoOptions extends JPanel
{
    private final ConfigManager config;
    private final Runnable changed;
    private Map<BlowpipeAmmoPlan.Pipe,BlowpipeAmmoPlan.Dart> selections;
    private Set<BlowpipeAmmoPlan.Pipe> scanned=EnumSet.noneOf(BlowpipeAmmoPlan.Pipe.class);
    private boolean restoring;

    BlowpipeAmmoOptions(ConfigManager config,Runnable changed)
    {
        this.config=config;this.changed=changed;
        setOpaque(false);setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        selections=BlowpipeAmmoPlan.decode(config.getConfiguration("personalbis",BlowpipeAmmoPlan.CONFIG_KEY));
        setVisible(false);
    }

    void refresh(List<BankItem> bank)
    {
        Set<BlowpipeAmmoPlan.Pipe> next=BlowpipeAmmoPlan.scannedPipes(bank);
        if(next.equals(scanned))return;
        scanned=next;rebuild();
    }

    void reload()
    {
        selections=BlowpipeAmmoPlan.decode(config.getConfiguration("personalbis",BlowpipeAmmoPlan.CONFIG_KEY));
        rebuild();
    }

    Map<BlowpipeAmmoPlan.Pipe,BlowpipeAmmoPlan.Dart> snapshot()
    {
        Map<BlowpipeAmmoPlan.Pipe,BlowpipeAmmoPlan.Dart> result=new EnumMap<>(BlowpipeAmmoPlan.Pipe.class);
        for(BlowpipeAmmoPlan.Pipe pipe:scanned)if(selections.containsKey(pipe))result.put(pipe,selections.get(pipe));
        return Collections.unmodifiableMap(result);
    }

    private void rebuild()
    {
        restoring=true;removeAll();
        for(BlowpipeAmmoPlan.Pipe pipe:scanned)
        {
            JLabel label=new JLabel(pipe.label+" Ammo");label.setForeground(new Color(255,215,0));label.setFont(net.runelite.client.ui.FontManager.getRunescapeFont());
            label.setAlignmentX(Component.LEFT_ALIGNMENT);add(label);
            JComboBox<BlowpipeAmmoPlan.Dart> box=new JComboBox<>(BlowpipeAmmoPlan.options(pipe).toArray(new BlowpipeAmmoPlan.Dart[0]));
            SidebarInputStyle.combo(box);
            box.setToolTipText("<html><body style='width:200px'>Manual ammo assumption; loaded contents and quantity are unknown. Select darts, then regenerate.</body></html>");
            box.setSelectedItem(selections.getOrDefault(pipe,BlowpipeAmmoPlan.Dart.UNKNOWN));
            box.setAlignmentX(Component.LEFT_ALIGNMENT);box.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
            box.addActionListener(e->{if(restoring)return;
                BlowpipeAmmoPlan.Dart dart=(BlowpipeAmmoPlan.Dart)box.getSelectedItem();
                if(pipe.accepts(dart))selections.put(pipe,dart);else selections.remove(pipe);
                config.setConfiguration("personalbis",BlowpipeAmmoPlan.CONFIG_KEY,BlowpipeAmmoPlan.encode(selections));
                changed.run();
            });
            add(box);add(Box.createVerticalStrut(5));
        }
        setVisible(!scanned.isEmpty());restoring=false;revalidate();repaint();
    }
}
