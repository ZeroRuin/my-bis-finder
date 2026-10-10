package com.personalbis;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JTextArea;
import javax.swing.text.View;

/** Fits every wrapped line while leaving scrolling to the surrounding sidebar. */
final class AutoSizingTextArea extends JTextArea
{
    @Override
    public Dimension getPreferredSize()
    {
        Container parent = getParent();
        Insets parentInsets = parent == null ? new Insets(0, 0, 0, 0) : parent.getInsets();
        int width = parent == null ? getWidth() : parent.getWidth() - parentInsets.left - parentInsets.right;
        if (width <= 0) width = 205;
        Insets insets = getInsets();
        View view = getUI().getRootView(this);
        view.setSize(Math.max(1, width - insets.left - insets.right), Float.MAX_VALUE);
        int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS)) + insets.top + insets.bottom;
        return new Dimension(width, height);
    }

    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(0, getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize()
    {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
