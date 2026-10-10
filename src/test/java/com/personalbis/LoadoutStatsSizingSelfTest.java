package com.personalbis;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class LoadoutStatsSizingSelfTest
{
    private static final String LONG_STATS = "DPS: 7.5403\nEstimated TTK: 19.2s\nMax Hit: 46\n"
        + "Accuracy: 99.26%\nStyle: Dark Demonbane + Mark of Darkness\nPrayer Bonus: +18";

    public static void main(String[] args) throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            try
            {
                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                card.add(new JLabel("Loadout stats"));
                AutoSizingTextArea area = new AutoSizingTextArea();
                area.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                card.add(area);
                card.setSize(205, 500);
                area.setText(LONG_STATS);
                int longHeight = area.getPreferredSize().height;
                require(longHeight > 105, "long wrapped spell exceeds former fixed height");
                require(area.getMaximumSize().height >= longHeight && area.getMinimumSize().height >= longHeight,
                    "BoxLayout cannot clip preferred text height");
                card.setSize(205, card.getPreferredSize().height);
                card.doLayout();
                Rectangle2D last = area.modelToView2D(area.getText().length());
                require(last != null && last.getMaxY() <= area.getHeight(), "last Prayer Bonus line fits within rendered area");
                if (args.length > 0)
                {
                    BufferedImage image = new BufferedImage(card.getWidth(), card.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D graphics = image.createGraphics();
                    card.printAll(graphics);
                    graphics.dispose();
                    ImageIO.write(image, "png", new File(args[0]));
                }
                card.setSize(130, 500);
                int narrowHeight = area.getPreferredSize().height;
                require(narrowHeight > longHeight, "narrower sidebar expands for additional wrapping");
                card.setSize(350, 500);
                require(area.getPreferredSize().height < narrowHeight, "wider sidebar shrinks wrapped content");
                area.setText("DPS: 1.0\nStyle: Accurate\nPrayer Bonus: +1");
                require(area.getPreferredSize().height < longHeight, "shorter style shrinks stats box again");
                System.out.println("ALL LOADOUT STATS SIZING SELF-TESTS PASSED");
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    private static void require(boolean condition, String label)
    {
        if (!condition) throw new AssertionError(label);
        System.out.println("PASS " + label);
    }
}
