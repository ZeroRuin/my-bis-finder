package com.personalbis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.runelite.client.util.ImageUtil;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class BankBisOverlay extends Overlay
{
    private static final int BUTTON_SIZE = 21;
    private static final int CLOSE_GAP = 3;

    private final Client client;
    private final PersonalBisBankFilter bankFilter;

    private Rectangle bounds = new Rectangle();
    private final BufferedImage icon;

    @Inject
    public BankBisOverlay(Client client, PersonalBisBankFilter bankFilter)
    {
        this.client = client;
        this.bankFilter = bankFilter;
        this.icon = ImageUtil.loadImageResource(getClass(), "/personal-bis-icon.png");
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Widget bankTitle = client.getWidget(InterfaceID.Bankmain.TITLE);
        Widget bankContainer = client.getWidget(InterfaceID.Bankmain.ITEMS);

        if (bankTitle == null || bankContainer == null || bankContainer.isHidden())
        {
            bounds = new Rectangle();
            return null;
        }

        Rectangle titleBounds = bankTitle.getBounds();
        // Native close button sits at the far right of the bank title bar.
        // Keep Personal BiS as a matching square immediately to its left.
        // Personal BiS occupies the compact plugin slot immediately left of the
        // native close button; other plugin buttons (e.g. Quest Helper) remain left of us.
        int x = titleBounds.x + titleBounds.width - BUTTON_SIZE - CLOSE_GAP - 23;
        int y = titleBounds.y;
        bounds = new Rectangle(x, y, BUTTON_SIZE, BUTTON_SIZE);

        graphics.setColor(new Color(40, 40, 40, 235));
        graphics.fillRect(x, y, BUTTON_SIZE, BUTTON_SIZE);
        graphics.setColor(new Color(110, 91, 57));
        graphics.drawRect(x, y, BUTTON_SIZE, BUTTON_SIZE);
        if (icon != null)
        {
            graphics.drawImage(icon, x + 2, y + 2, BUTTON_SIZE - 3, BUTTON_SIZE - 3, null);
        }

        return null;
    }

    public boolean contains(Point point)
    {
        return !bounds.isEmpty() && bounds.contains(point);
    }

    public void click()
    {
        bankFilter.toggle();
    }
}
