package com.personalbis;

import com.google.inject.Provides;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.slayer.SlayerPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
    name = "My BiS Finder",
    description = "Find your best owned gear for any target using your bank, levels and Slayer task.",
    tags = {"bis", "gear", "equipment", "bank", "combat", "slayer"}
)
@PluginDependency(SlayerPlugin.class)
public class PersonalBisPlugin extends Plugin
{
    @Inject private ClientToolbar clientToolbar;
    @Inject private OverlayManager overlayManager;
    @Inject private MouseManager mouseManager;
    @Inject private PersonalBisPanel panel;
    @Inject private BankBisOverlay bankBisOverlay;
    @Inject private ClientThread clientThread;
    @Inject private EventBus eventBus;
    @Inject private PersonalBisBankFilter bankFilter;

    private NavigationButton navigationButton;
    private int tickCounter;

    private final MouseListener bankButtonMouseListener = new MouseListener()
    {
        @Override public MouseEvent mouseClicked(MouseEvent event) { return event; }
        @Override public MouseEvent mousePressed(MouseEvent event)
        {
            if (bankBisOverlay.contains(event.getPoint()))
            {
                bankFilter.toggle();
                event.consume();
            }
            return event;
        }
        @Override public MouseEvent mouseReleased(MouseEvent event) { return event; }
        @Override public MouseEvent mouseEntered(MouseEvent event) { return event; }
        @Override public MouseEvent mouseExited(MouseEvent event) { return event; }
        @Override public MouseEvent mouseDragged(MouseEvent event) { return event; }
        @Override public MouseEvent mouseMoved(MouseEvent event) { return event; }
    };

    @Provides
    PersonalBisConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PersonalBisConfig.class);
    }

    @Override
    protected void startUp()
    {
        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/personal-bis-icon.png");
        navigationButton = NavigationButton.builder()
            .tooltip("My BiS Finder")
            .icon(icon)
            .priority(50)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navigationButton);
        overlayManager.add(bankBisOverlay);
        eventBus.register(bankFilter);
        mouseManager.registerMouseListener(bankButtonMouseListener);
        clientThread.invokeLater(() ->
        {
            panel.refreshAll();
        });
    }

    @Override
    protected void shutDown()
    {
        mouseManager.unregisterMouseListener(bankButtonMouseListener);
        bankFilter.deactivate();
        eventBus.unregister(bankFilter);
        overlayManager.remove(bankBisOverlay);
        if (navigationButton != null)
        {
            clientToolbar.removeNavigation(navigationButton);
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        panel.updatePlayerStats();
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getContainerId() == InventoryID.BANK)
        {
            // Opening/loading the bank establishes the recommendation snapshot.
            // While the Personal BiS bank view is active, withdrawals/deposits must
            // remain native and must not rerun the optimiser on every quantity change.
            if (!bankFilter.isActive())
            {
                panel.refreshBankItems();
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            panel.refreshAll();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (++tickCounter >= 2)
        {
            tickCounter = 0;
            panel.refreshSlayerTaskIfChanged();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if ("personalbis".equals(event.getGroup()))
        {
            clientThread.invokeLater(panel::updatePlayerStats);
        }
    }
}
