package com.personalbis;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/** A target in the sidebar's target list. */
final class MonsterRenderer extends DefaultListCellRenderer
{
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setToolTipText(value == null ? null : value.toString());
        label.setFont(FontManager.getRunescapeFont());
        label.setBorder(new EmptyBorder(4, 5, 4, 5));
        label.setBackground(isSelected ? SidebarStyle.BORDER : ColorScheme.DARK_GRAY_COLOR);
        label.setForeground(isSelected ? Color.WHITE : SidebarStyle.TEXT);
        return label;
    }
}
