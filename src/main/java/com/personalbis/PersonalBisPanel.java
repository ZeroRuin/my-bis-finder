package com.personalbis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.slayer.SlayerPluginService;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class PersonalBisPanel extends PluginPanel
{
    private static final Color OSRS_PANEL = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color OSRS_PANEL_DARK = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color OSRS_SLOT = new Color(58, 51, 40);
    private static final Color OSRS_BORDER = new Color(103, 90, 67);
    private static final Color OSRS_GOLD = new Color(255, 184, 38);
    private static final Color OSRS_CREAM = new Color(238, 220, 172);
    private static final Color OSRS_GREEN = new Color(82, 255, 92);

    private final Client client;
    private final ClientThread clientThread;
    private final ItemManager itemManager;
    private final SpriteManager spriteManager;
    private final BankScanner bankScanner;
    private final EquipmentIntelligence intelligence;
    private final MeleeLoadoutOptimizer meleeOptimizer;
    private final RangedLoadoutOptimizer rangedOptimizer;
    private final MagicLoadoutOptimizer magicOptimizer;
    private final SlayerPluginService slayerService;
    private final MonsterDatabase monsterDatabase;
    private final AccountSnapshot account;
    private final PersonalBisBankFilter bankFilter;
    private final ItemExclusions exclusions;
    private final JLabel exclusionWarning = new JLabel();
    private volatile ItemExclusionList generationExclusions = new ItemExclusionList("");
    private List<BankItem> generationBank = Collections.emptyList();
    private List<BankItem> generationOwned = Collections.emptyList();

    private final JPanel content = new JPanel();
    private final JComboBox<MonsterDefinition> monsterBox = new JComboBox<>();
    private final JTextField monsterSearch = new JTextField();
    private final JCheckBox slayerTask = new JCheckBox("Load Slayer Task");
    private final JComboBox<String> prayerBox = new JComboBox<>();
    private final JComboBox<String> boostBox = new JComboBox<>();
    private final JLabel status = new JLabel("Log in to load account and monsters");
    private final JLabel resultTitle = new JLabel("Best owned setup");
    private final JLabel monsterInfo = new JLabel("Target: —");
    private final JTextArea meleeValidation = new AutoSizingTextArea();
    private final JLabel slayerItemWarning = new JLabel();
    private final JLabel[] gear = new JLabel[EquipmentSlot.values().length];
    private final JLabel prayerIcon = new JLabel("", JLabel.CENTER);
    private final JRadioButton slash = new JRadioButton("Slash");
    private final JRadioButton stab = new JRadioButton("Stab");
    private final JRadioButton crush = new JRadioButton("Crush");
    private final JRadioButton ranged = new JRadioButton("Ranged");
    private final JRadioButton magic = new JRadioButton("Magic");
    private final JLabel[] bestStyleBadges = new JLabel[5];
    private JPanel targetCard;
    private JPanel stylesPanel;
    private JLabel attackStyleHeading;
    private JPanel gearSection;
    private JPanel loggedOutPanel;
    private JPanel noTargetPanel;
    private JPanel chooseStylePanel;
    private JLabel monsterCountLabel;
    private JPanel validationSection;
    private final JButton generateLoadoutButton = osrsButton("Generate Loadout");
    private final JLabel generateMessage = new JLabel(" ", SwingConstants.CENTER);
    private final JButton advancedToggle = osrsButton("Advanced ▸");
    private final JPanel advancedOptions = new JPanel();
    private final JPanel groupStorageOptions = new JPanel();
    private boolean advancedExpanded;
    private volatile boolean groupStorageAvailable = false;
    private final net.runelite.client.config.ConfigManager preferenceConfig;
    private final JCheckBox includeGroupStorage = new JCheckBox("Include group storage", false);
    private final JTextArea groupStorageNote = new JTextArea();
    private volatile boolean includeGroupStorageEnabled;
    private final GroupStorageSnapshot groupSnapshot = new GroupStorageSnapshot();
    private final JCheckBox includeRubyBolts = new JCheckBox("Include Ruby bolts (e)",true);
    private final JTextArea rubyBoltNote = new JTextArea("    - Increases calculation time.");
    private final JLabel loadoutHeading = new JLabel("Your best loadout");
    private AttackStyle calculatedBestStyle;
    private boolean targetExplicitlySelected;
    private boolean bankChecked;
    private boolean styleExplicitlySelected;
    private boolean populatingTargets;
    private boolean filteringTargets;
    private boolean loadoutsGenerated;
    private boolean loadoutReady;

    private List<BankItem> cachedBank = Collections.emptyList();
    private volatile boolean ownedHasRubyBolts;
    private String lastSlayerTask;
    private String lastSlayerLocation = "";
    private final JTextArea slayerAssignmentNote = new AutoSizingTextArea();
    private Set<String> unverifiedTaskTargets = Collections.emptySet();
    private volatile boolean slayerMode;
    private volatile boolean slayerTargetMode;
    private volatile boolean includeRubyBoltsEnabled=true;
    private AttackStyle selectedStyle = AttackStyle.MELEE_SLASH;
    private final ExecutorService optimizerExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t=new Thread(r,"personal-bis-optimizer"); t.setDaemon(true); return t;
    });
    private final AtomicLong calculationGeneration=new AtomicLong();

    // alpha50.4.20: cache completed optimiser reports for the current combat
    // inputs. Switching the visible attack style must not recompute the same
    // five expensive searches when target/account/bank/Slayer state is unchanged.
    private volatile String optimizerCacheKey;
    private final Map<AttackStyle, MeleeOptimizationReport> cachedMeleeReports = new ConcurrentHashMap<>();
    private final Map<AttackStyle, RangedOptimizationReport> cachedRangedReports = new ConcurrentHashMap<>();
    private final Map<AttackStyle, MagicOptimizationReport> cachedMagicReports = new ConcurrentHashMap<>();

    private void invalidateOptimizerCache()
    {
        optimizerCacheKey = null;
        cachedMeleeReports.clear();
        cachedRangedReports.clear();
        cachedMagicReports.clear();
    }

    private String optimizerCacheKey(MonsterDefinition monster, List<BankItem> bank, boolean slayer)
    {
        StringBuilder k = new StringBuilder(256);
        k.append(monster == null ? -1 : monster.getId())
            .append('|').append(monster == null ? 1 : monster.getSize())
            .append('|').append(monster == null ? "" : monster.getVersion())
            .append('|').append(slayer).append('|').append(includeRubyBoltsEnabled);
        Skill[] combatSkills = {Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC, Skill.PRAYER, Skill.HITPOINTS};
        for (Skill skill : combatSkills)
            k.append('|').append(account.real(skill)).append('/').append(account.boosted(skill));
        k.append('|').append(account.isPietyUnlocked()).append(account.isChivalryUnlocked())
            .append(account.isRigourUnlocked()).append(account.isAuguryUnlocked())
            .append(account.isDeadeyeUnlocked()).append(account.isMysticVigourUnlocked())
            .append(account.isAncientMagicksUnlocked()).append(account.isKingdomDividedUnlocked())
            .append(account.isKandarinHardCompleted());
        // BankScanner order is stable for a snapshot; include quantities because
        // runes/ammo availability can change an otherwise identical loadout.
        for (BankItem item : bank)
            k.append('|').append(item.getItemId()).append(':').append(item.getQuantity());
        return k.toString();
    }

    @Inject
    public PersonalBisPanel(Client client, ClientThread clientThread, ItemManager itemManager, SpriteManager spriteManager,
        BankScanner bankScanner, EquipmentIntelligence intelligence, MeleeLoadoutOptimizer meleeOptimizer, RangedLoadoutOptimizer rangedOptimizer, MagicLoadoutOptimizer magicOptimizer, SlayerPluginService slayerService,
        MonsterDatabase monsterDatabase, AccountSnapshot account, PersonalBisBankFilter bankFilter, net.runelite.client.config.ConfigManager preferenceConfig, ItemExclusions exclusions)
    {
        super(false);
        this.preferenceConfig = preferenceConfig;
        this.exclusions = exclusions;
        String savedGroup = preferenceConfig.getConfiguration("personalbis", "includeGroupStorage");
        String savedRuby = preferenceConfig.getConfiguration("personalbis", "includeRubyBolts");
        includeGroupStorageEnabled = savedGroup != null && Boolean.parseBoolean(savedGroup);
        includeRubyBoltsEnabled = savedRuby == null || Boolean.parseBoolean(savedRuby);
        includeGroupStorage.setSelected(includeGroupStorageEnabled);
        includeRubyBolts.setSelected(includeRubyBoltsEnabled);
        this.client = client;
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        this.spriteManager = spriteManager;
        this.bankScanner = bankScanner;
        this.intelligence = intelligence;
        this.meleeOptimizer = meleeOptimizer;
        this.rangedOptimizer = rangedOptimizer;
        this.magicOptimizer = magicOptimizer;
        this.slayerService = slayerService;
        this.monsterDatabase = monsterDatabase;
        this.account = account;
        this.bankFilter = bankFilter;

        setLayout(new BorderLayout());
        setBackground(OSRS_PANEL);
        setBorder(null);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(OSRS_PANEL);
        content.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        content.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH, Integer.MAX_VALUE));
        buildUi();
        // BoxLayout uses each child's alignment to calculate the shared horizontal axis.
        // Mixing centred test buttons with left-aligned controls shifts the entire column.
        for (Component child : content.getComponents())
        {
            if (child instanceof javax.swing.JComponent)
                ((javax.swing.JComponent) child).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        // PluginPanel's default constructor creates a JScrollPane with a Look&Feel border.
        // Use the unwrapped panel and provide our own borderless scroll pane instead.
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel northPanel = new SidebarViewportPanel();
        northPanel.setBackground(OSRS_PANEL);
        northPanel.add(content, BorderLayout.NORTH);
        JScrollPane panelScroll = new JScrollPane(northPanel);
        panelScroll.setBackground(OSRS_PANEL);
        panelScroll.getViewport().setBackground(OSRS_PANEL);
        panelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelScroll.setBorder(null);
        panelScroll.setViewportBorder(null);
        add(panelScroll, BorderLayout.CENTER);
    }

    private static final class SidebarViewportPanel extends JPanel implements javax.swing.Scrollable
    {
        SidebarViewportPanel() { super(new BorderLayout()); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(java.awt.Rectangle bounds, int orientation, int direction) { return 16; }
        public int getScrollableBlockIncrement(java.awt.Rectangle bounds, int orientation, int direction) { return Math.max(16, bounds.height - 16); }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private void buildUi()
    {
        JLabel title = new JLabel("My BiS Finder", SwingConstants.LEFT);
        title.setFont(headerFont());
        title.setForeground(OSRS_GOLD);
        title.setOpaque(true);
        title.setBackground(OSRS_PANEL);
        title.setBorder(new EmptyBorder(4, 4, 4, 4));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));

        targetCard = new JPanel(new GridBagLayout());
        targetCard.setBackground(OSRS_PANEL);
        targetCard.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        targetCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        targetCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));

        monsterCountLabel = new JLabel("Monsters loading...");
        monsterCountLabel.setFont(FontManager.getRunescapeFont());
        monsterCountLabel.setForeground(OSRS_CREAM);
        monsterCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(monsterCountLabel);
        content.add(Box.createVerticalStrut(8));

        GridBagConstraints targetGbc = new GridBagConstraints();
        targetGbc.gridx = 0;
        targetGbc.weightx = 1.0;
        targetGbc.fill = GridBagConstraints.HORIZONTAL;
        targetGbc.anchor = GridBagConstraints.WEST;
        targetGbc.insets = new Insets(0, 0, 0, 0);

        JLabel targetHeading = sectionLabel("Target");
        targetHeading.setHorizontalAlignment(SwingConstants.LEFT);
        JPanel targetHeadingRow=new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));
        targetHeadingRow.setOpaque(false);
        targetHeadingRow.add(targetHeading);
        styleCheck(slayerTask);
        slayerTask.setText("Load Slayer Task");
        slayerTask.setOpaque(false);
        slayerTask.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
        targetHeadingRow.add(slayerTask);
        targetGbc.gridy = 0;
        targetCard.add(targetHeadingRow, targetGbc);

        styleCombo(monsterBox);
        monsterBox.setEditable(true);
        monsterBox.setMaximumRowCount(20);
        monsterBox.setRenderer(new MonsterRenderer());
        Component editorComponent = monsterBox.getEditor().getEditorComponent();
        if (editorComponent instanceof JTextField)
        {
            JTextField editor = (JTextField) editorComponent;
            styleTextField(editor);
            editor.setToolTipText("Type to filter NPCs, or enable Slayer task targeting.");
            editor.addKeyListener(new KeyAdapter()
            {
                @Override public void keyReleased(KeyEvent e)
                {
                    int key=e.getKeyCode();
                    boolean navigationKey=key==KeyEvent.VK_UP||key==KeyEvent.VK_DOWN
                        ||key==KeyEvent.VK_PAGE_UP||key==KeyEvent.VK_PAGE_DOWN
                        ||key==KeyEvent.VK_ENTER||key==KeyEvent.VK_ESCAPE||key==KeyEvent.VK_TAB;
                    if (!slayerTargetMode && !navigationKey)
                    {
                        SwingUtilities.invokeLater(() ->
                        {
                            filteringTargets = true;
                            applyMonsterFilter();
                            filteringTargets = false;
                            // Autocomplete should behave like a search result list:
                            // typing opens it immediately, and each keystroke replaces
                            // its contents with the progressively narrower match set.
                            if (monsterBox.getItemCount()>0 && monsterBox.isShowing()
                                && editor.isFocusOwner())
                            {
                                monsterBox.showPopup();
                            }
                            else if (monsterBox.isPopupVisible()) monsterBox.hidePopup();
                        });
                    }
                }
            });
            // Filtering is driven by keyReleased above.  Rebuilding the combo model
            // from a DocumentListener races the editor and restores the previous
            // selection, making typed text disappear one character at a time.
        }
        targetGbc.gridy = 1;
        targetGbc.insets = new Insets(0, 0, 4, 0);
        targetCard.add(monsterBox, targetGbc);
        slayerAssignmentNote.setEditable(false);
        slayerAssignmentNote.setFocusable(false);
        slayerAssignmentNote.setOpaque(false);
        slayerAssignmentNote.setLineWrap(true);
        slayerAssignmentNote.setWrapStyleWord(true);
        slayerAssignmentNote.setFont(FontManager.getRunescapeFont());
        slayerAssignmentNote.setForeground(OSRS_CREAM);
        slayerAssignmentNote.setVisible(false);
        targetGbc.gridy = 2;
        targetCard.add(slayerAssignmentNote, targetGbc);
        monsterBox.addActionListener(e ->
        {
            if (!populatingTargets)
            {
                MonsterDefinition chosen = monsterBox.getSelectedItem() instanceof MonsterDefinition
                    ? (MonsterDefinition) monsterBox.getSelectedItem() : resolveTypedTarget();
                if (chosen != null)
                {
                    populatingTargets = true;
                    monsterBox.setSelectedItem(chosen);
                    populatingTargets = false;
                    targetExplicitlySelected = true;
                    styleExplicitlySelected = false;
                    calculatedBestStyle = null;
                    clearStyleSelection();
                    invalidateOptimizerCache();
                    markLoadoutStale("");
                    updateProgressiveVisibility();
                    updateSlayerAssignmentNote();
                }
            }
        });
        slayerTask.addActionListener(e ->
        {
            slayerTargetMode = slayerTask.isSelected();
            slayerMode = slayerTargetMode;
            invalidateOptimizerCache();
            updateSlayerUiState();
            if (!slayerTargetMode)
            {
                setTargetEditorText("");
                applyMonsterFilter();
            }
            clientThread.invokeLater(() ->
            {
                updateSlayerTask();
                SwingUtilities.invokeLater(() -> markLoadoutStale(""));
            });
        });
        content.add(targetCard);
        content.add(Box.createVerticalStrut(4));
        targetCard.setVisible(false);
        monsterCountLabel.setVisible(false);

        // Prayer and boost controls are intentionally hidden; recommendations are automatic.

        resultTitle.setFont(headerFont());
        resultTitle.setForeground(OSRS_GREEN);
        resultTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
monsterInfo.setFont(FontManager.getRunescapeFont());
        monsterInfo.setForeground(OSRS_CREAM);
        monsterInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
content.add(Box.createVerticalStrut(5));

        advancedToggle.setFont(headerFont());
        advancedToggle.setForeground(OSRS_GOLD);
        advancedToggle.setHorizontalAlignment(SwingConstants.LEFT);
        advancedToggle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        advancedToggle.setBorderPainted(false);
        advancedToggle.setContentAreaFilled(false);
        advancedToggle.setFocusPainted(false);
        advancedToggle.setOpaque(false);
        advancedToggle.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        advancedToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        advancedToggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        advancedOptions.setLayout(new BoxLayout(advancedOptions, BoxLayout.Y_AXIS));
        advancedOptions.setOpaque(false);
        advancedOptions.setAlignmentX(Component.LEFT_ALIGNMENT);
        advancedOptions.setBorder(BorderFactory.createEmptyBorder(6, 4, 8, 4));
        advancedOptions.setVisible(false);
        advancedToggle.addActionListener(e -> {
            advancedExpanded = !advancedExpanded;
            advancedToggle.setText(advancedExpanded ? "Advanced ▾" : "Advanced ▸");
            advancedOptions.setVisible(advancedExpanded);
            content.revalidate(); content.repaint();
        });
        JTextArea advancedNote = new JTextArea("Options may increase calculation time.");
        advancedNote.setEditable(false); advancedNote.setFocusable(false);
        advancedNote.setOpaque(false); advancedNote.setLineWrap(true); advancedNote.setWrapStyleWord(true);
        advancedNote.setFont(FontManager.getRunescapeFont()); advancedNote.setForeground(OSRS_CREAM);
        advancedNote.setAlignmentX(Component.LEFT_ALIGNMENT); advancedNote.setRows(1);
        advancedNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        advancedOptions.add(advancedNote);
        advancedOptions.add(Box.createVerticalStrut(5));
        content.add(advancedToggle); content.add(advancedOptions);
        content.add(Box.createVerticalStrut(8));

        groupStorageOptions.setLayout(new BoxLayout(groupStorageOptions, BoxLayout.Y_AXIS));
        groupStorageOptions.setOpaque(false);
        groupStorageOptions.setAlignmentX(Component.LEFT_ALIGNMENT);
        advancedOptions.add(groupStorageOptions);
        styleCheck(includeGroupStorage);
        includeGroupStorage.setForeground(includeGroupStorageEnabled ? OSRS_GREEN : OSRS_CREAM);
        includeGroupStorage.setFont(FontManager.getRunescapeBoldFont());
        includeGroupStorage.setOpaque(false);
        includeGroupStorage.setBorderPainted(false);
        includeGroupStorage.setAlignmentX(Component.LEFT_ALIGNMENT);
        includeGroupStorage.addActionListener(e -> {
            includeGroupStorageEnabled = includeGroupStorage.isSelected();
            includeGroupStorage.setForeground(includeGroupStorageEnabled ? OSRS_GREEN : OSRS_CREAM);
            preferenceConfig.setConfiguration("personalbis", "includeGroupStorage", includeGroupStorageEnabled);
            invalidateOptimizerCache();
            if (loadoutsGenerated) markLoadoutStale("Group storage setting changed — regenerate.");
            clientThread.invokeLater(() -> bankFilter.setGroupItems(groupStorageAvailable && includeGroupStorageEnabled ? groupSnapshot.getItems() : Collections.emptyList()));
        });
        groupStorageNote.setEditable(false);
        groupStorageNote.setFocusable(false);
        groupStorageNote.setOpaque(false);
        groupStorageNote.setLineWrap(true);
        groupStorageNote.setWrapStyleWord(true);
        groupStorageNote.setFont(FontManager.getRunescapeFont());
        groupStorageNote.setForeground(OSRS_CREAM);
        groupStorageNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        groupStorageNote.setRows(4);
        groupStorageNote.setColumns(18);
        groupStorageNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        includeGroupStorage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        groupStorageOptions.add(Box.createVerticalStrut(2));
        groupStorageOptions.add(includeGroupStorage);
        groupStorageOptions.add(groupStorageNote);
        groupStorageOptions.add(Box.createVerticalStrut(5));
        groupStorageOptions.add(Box.createVerticalStrut(4));
        groupStorageOptions.add(Box.createVerticalStrut(5));
        updateGroupStorageNote();

        styleCheck(includeRubyBolts);
        includeRubyBolts.setFont(FontManager.getRunescapeBoldFont());
        includeRubyBolts.setOpaque(false);
        includeRubyBolts.setBorderPainted(false);
        includeRubyBolts.setMargin(new Insets(0,0,0,0));
        includeRubyBolts.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
        includeRubyBolts.setAlignmentX(Component.LEFT_ALIGNMENT);
        includeRubyBolts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        includeRubyBolts.addActionListener(e -> {
            includeRubyBoltsEnabled=includeRubyBolts.isSelected();
            preferenceConfig.setConfiguration("personalbis", "includeRubyBolts", includeRubyBoltsEnabled);
            updateRubyBoltUi();
            invalidateOptimizerCache();
            if(loadoutsGenerated)markLoadoutStale("Ruby bolt setting changed — regenerate the loadout.");
        });
        updateRubyBoltUi();
        rubyBoltNote.setEditable(false);
        rubyBoltNote.setFocusable(false);
        rubyBoltNote.setOpaque(false);
        rubyBoltNote.setLineWrap(true);
        rubyBoltNote.setWrapStyleWord(true);
        rubyBoltNote.setRows(2);
        rubyBoltNote.setFont(FontManager.getRunescapeFont());
        rubyBoltNote.setForeground(OSRS_CREAM); rubyBoltNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        rubyBoltNote.setBorder(BorderFactory.createEmptyBorder(0,0,2,0));
        rubyBoltNote.setMaximumSize(new Dimension(Integer.MAX_VALUE,rubyBoltNote.getPreferredSize().height));
        advancedOptions.add(includeRubyBolts); advancedOptions.add(Box.createVerticalStrut(5));

        generateLoadoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateLoadoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        generateLoadoutButton.addActionListener(e -> generateLoadouts());
        content.add(generateLoadoutButton);
        generateMessage.setFont(FontManager.getRunescapeFont());
        generateMessage.setForeground(OSRS_CREAM);
        generateMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateMessage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        content.add(generateMessage);
        exclusionWarning.setFont(FontManager.getRunescapeFont());
        exclusionWarning.setForeground(OSRS_GOLD);
        exclusionWarning.setAlignmentX(Component.LEFT_ALIGNMENT);
        exclusionWarning.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        exclusionWarning.setVisible(false);
        content.add(exclusionWarning);
        content.add(Box.createVerticalStrut(8));

        attackStyleHeading = sectionLabel("Attack style");
        attackStyleHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(attackStyleHeading);
                JPanel styles = new JPanel(new GridLayout(1, 5, 3, 0));
        styles.setPreferredSize(new Dimension(205, 78));
        styles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        styles.setAlignmentX(Component.LEFT_ALIGNMENT);
        styles.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(slash);
        group.add(stab);
        group.add(crush);
        group.add(ranged);
        group.add(magic);
        styleRadio(slash);
        styleRadio(stab);
        styleRadio(crush);
        styleRadio(ranged);
        styleRadio(magic);
        updateStyleHighlight();
        slash.addActionListener(e -> selectStyle(AttackStyle.MELEE_SLASH));
        stab.addActionListener(e -> selectStyle(AttackStyle.MELEE_STAB));
        crush.addActionListener(e -> selectStyle(AttackStyle.MELEE_CRUSH));
        ranged.addActionListener(e -> selectStyle(AttackStyle.RANGED));
        magic.addActionListener(e -> selectStyle(AttackStyle.MAGIC));
        styles.add(styleTile(slash,0));
        styles.add(styleTile(stab,1));
        styles.add(styleTile(crush,2));
        styles.add(styleTile(ranged,3));
        styles.add(styleTile(magic,4));
        stylesPanel = styles;
        setStyleControlsEnabled(false);
        content.add(styles);
        content.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setForeground(OSRS_BORDER);
        gearSection = new JPanel();
        gearSection.setLayout(new BoxLayout(gearSection, BoxLayout.Y_AXIS));
        gearSection.setOpaque(false);
        gearSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        gearSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        gearSection.add(sep);
        gearSection.add(Box.createVerticalStrut(4));

        loadoutHeading.setHorizontalAlignment(SwingConstants.LEFT);
        loadoutHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadoutHeading.setFont(headerFont());
        loadoutHeading.setForeground(OSRS_GOLD);
        loadoutHeading.setBorder(new EmptyBorder(0,0,4,0));
        gearSection.add(loadoutHeading);
        JPanel gearGrid = new JPanel(new GridLayout(3, 4, 4, 4));
        gearGrid.setOpaque(false);
        gearGrid.setPreferredSize(new Dimension(205, 156));
        gearGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 156));
        gearGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Labels remain indexed by EquipmentSlot ordinal for recommendation updates,
        // but are inserted into the visible grid in the user's preferred order.
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            JLabel label = new JLabel("—", JLabel.CENTER);
            label.setFont(FontManager.getRunescapeFont());
            label.setForeground(OSRS_CREAM);
            label.setOpaque(true);
            label.setBackground(new Color(38, 38, 38));
            label.setPreferredSize(new Dimension(48, 50));
            label.setMinimumSize(new Dimension(40, 40));
            label.setBorder(BorderFactory.createEmptyBorder());
            gear[slot.ordinal()] = label;
            label.addMouseListener(new java.awt.event.MouseAdapter()
            {
                private void showMenu(java.awt.event.MouseEvent event)
                {
                    Object id = label.getClientProperty("personalbis.itemId");
                    if (!event.isPopupTrigger() || !loadoutsGenerated || !(id instanceof Integer)) return;
                    javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
                    javax.swing.JMenuItem exclude = new javax.swing.JMenuItem(ItemExclusions.MENU_OPTION);
                    exclude.addActionListener(e -> clientThread.invokeLater(() -> exclusions.exclude((Integer) id)));
                    popup.add(exclude);
                    popup.show(label, event.getX(), event.getY());
                }
                @Override public void mousePressed(java.awt.event.MouseEvent e) { showMenu(e); }
                @Override public void mouseReleased(java.awt.event.MouseEvent e) { showMenu(e); }
            });
        }
        prayerIcon.setOpaque(true);
        prayerIcon.setBackground(new Color(38, 38, 38));
        prayerIcon.setPreferredSize(new Dimension(48, 50));
        prayerIcon.setMinimumSize(new Dimension(40, 40));
        prayerIcon.setBorder(BorderFactory.createEmptyBorder());
        prayerIcon.setToolTipText("Recommended prayer");

        // Row 1: Head | Neck | Ammo | Prayer
        gearGrid.add(gear[EquipmentSlot.HEAD.ordinal()]);
        gearGrid.add(gear[EquipmentSlot.NECK.ordinal()]);
        gearGrid.add(gear[EquipmentSlot.AMMO.ordinal()]);
        gearGrid.add(prayerIcon);
        // Row 2: Body | Cape | Weapon | Shield
        gearGrid.add(gear[EquipmentSlot.BODY.ordinal()]);
        gearGrid.add(gear[EquipmentSlot.CAPE.ordinal()]);
        gearGrid.add(gear[EquipmentSlot.WEAPON.ordinal()]);
        gearGrid.add(gear[EquipmentSlot.SHIELD.ordinal()]);
        // Row 3: Legs | Feet | Hands | Ring
        gearGrid.add(gear[EquipmentSlot.LEGS.ordinal()]);
        gearGrid.add(gear[EquipmentSlot.FEET.ordinal()]);
        gearGrid.add(gear[EquipmentSlot.HANDS.ordinal()]);
        gearGrid.add(gear[EquipmentSlot.RING.ordinal()]);

        gearSection.add(gearGrid);
        gearSection.add(Box.createVerticalStrut(5));

        content.add(gearSection);

        JPanel validationCard = osrsCard();
        validationCard.setLayout(new BoxLayout(validationCard, BoxLayout.Y_AXIS));
        validationCard.setBackground(OSRS_PANEL);
        validationCard.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        validationCard.add(sectionLabel("Loadout stats"));
        slayerItemWarning.setForeground(Color.RED);
        slayerItemWarning.setFont(FontManager.getRunescapeFont());
        slayerItemWarning.setAlignmentX(Component.LEFT_ALIGNMENT);
        slayerItemWarning.setVisible(false);
        validationCard.add(slayerItemWarning);
        meleeValidation.setEditable(false);
        meleeValidation.setFocusable(false);
        meleeValidation.setLineWrap(true);
        meleeValidation.setWrapStyleWord(true);
        meleeValidation.setFont(FontManager.getRunescapeFont());
        meleeValidation.setForeground(OSRS_CREAM);
        meleeValidation.setBackground(OSRS_PANEL);
        meleeValidation.setBorder(new EmptyBorder(3, 3, 3, 3));
        meleeValidation.setText("Select an attack style.");
        meleeValidation.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        meleeValidation.setAlignmentX(Component.LEFT_ALIGNMENT);
        validationCard.add(meleeValidation);

        validationSection = validationCard;
        content.add(validationCard);
        updateProgressiveVisibility();

        status.setFont(FontManager.getRunescapeFont());
        status.setForeground(OSRS_CREAM);
        status.setBorder(new EmptyBorder(8, 2, 4, 2));
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Developer status text hidden from the normal sidebar.
    }

    private JPanel osrsCard()
    {
        JPanel p = new JPanel();
        p.setBackground(OSRS_PANEL);
        p.setBorder(new EmptyBorder(4, 4, 4, 4));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private java.awt.Font headerFont()
    {
        return FontManager.getRunescapeBoldFont().deriveFont(FontManager.getRunescapeFont().getSize2D());
    }

    private JLabel sectionLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(headerFont());
        label.setForeground(OSRS_GOLD);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        return label;
    }

    private void styleTextField(JTextField field)
    {
        field.setFont(FontManager.getRunescapeFont());
        field.setForeground(Color.WHITE);
        field.setCaretColor(OSRS_GOLD);
        field.setBackground(ColorScheme.DARK_GRAY_COLOR);
        field.setBorder(new CompoundBorder(new LineBorder(OSRS_BORDER), new EmptyBorder(3, 4, 3, 4)));
    }

    private void styleCombo(JComboBox<?> combo)
    {
        combo.setFont(FontManager.getRunescapeFont());
        combo.setForeground(Color.WHITE);
        combo.setBackground(ColorScheme.DARK_GRAY_COLOR);
        combo.setBorder(new LineBorder(OSRS_BORDER));
        combo.setFocusable(true);
        combo.setUI(new BasicComboBoxUI()
        {
            @Override
            protected JButton createArrowButton()
            {
                JButton b = new JButton("▼");
                b.setFont(FontManager.getRunescapeFont());
                b.setForeground(OSRS_GOLD);
                b.setBackground(ColorScheme.DARK_GRAY_COLOR);
                b.setBorder(new EmptyBorder(0, 4, 0, 4));
                b.setFocusable(false);
                return b;
            }
        });
    }

    private void styleCheck(JCheckBox check)
    {
        check.setFont(FontManager.getRunescapeFont());
        check.setForeground(OSRS_CREAM);
        check.setBackground(ColorScheme.DARK_GRAY_COLOR);
        check.setFocusPainted(false);
        check.setOpaque(true);
        check.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
    }

    private void updateRubyBoltUi()
    {
        includeRubyBolts.setForeground(includeRubyBolts.isSelected() ? OSRS_GREEN : OSRS_CREAM);
    }

    private void updateSlayerUiState()
    {
        boolean selected = slayerTargetMode;
        slayerTask.setSelected(selected);
        slayerTask.setText("Load Slayer Task");
        slayerTask.setForeground(selected ? OSRS_GREEN : OSRS_CREAM);
        slayerTask.setOpaque(false);
        slayerTask.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        monsterBox.setEnabled(true);
        // Restricted tasks must not accept a typed target outside their model.
        monsterBox.setEditable(!selected);
        slayerAssignmentNote.setVisible(selected);
        if (monsterBox.getEditor().getEditorComponent() != null)
            monsterBox.getEditor().getEditorComponent().setEnabled(true);
        monsterBox.setEnabled(true);
        monsterBox.setToolTipText(selected
            ? "Task alternatives; entries marked [location?] need location verification."
            : "Choose a monster target.");
    }

    private ImageIcon attackStyleIcon(String label)
    {
        String name=label==null?"":label.toLowerCase();
        try (InputStream in=PersonalBisPanel.class.getResourceAsStream("/attack-styles/"+name+".png"))
        {
            return in==null?null:new ImageIcon(ImageIO.read(in));
        }
        catch (IOException ex)
        {
            return null;
        }
    }

    private void styleRadio(JRadioButton radio)
    {
        radio.setFont(FontManager.getRunescapeFont());
        radio.setForeground(OSRS_CREAM);
        radio.setBackground(new Color(38, 38, 38));
        radio.setOpaque(true);
        radio.setFocusPainted(false);
        radio.setHorizontalAlignment(SwingConstants.CENTER);
        radio.setBorderPainted(false);
        radio.setBorder(BorderFactory.createEmptyBorder(3,1,3,1));
        radio.setVerticalTextPosition(SwingConstants.BOTTOM);
        radio.setHorizontalTextPosition(SwingConstants.CENTER);
        radio.setIcon(attackStyleIcon(radio.getText()));
        radio.setPreferredSize(new Dimension(38, 58));
        radio.setMinimumSize(new Dimension(34, 54));
    }

    private JPanel styleTile(JRadioButton radio,int index)
    {
        JPanel tile=new JPanel(new BorderLayout(0,0));
        tile.setOpaque(true);
        tile.setBackground(new Color(38,38,38));
        JLabel badge=new JLabel(" ",SwingConstants.CENTER);
        badge.setFont(FontManager.getRunescapeBoldFont());
        badge.setForeground(OSRS_GOLD);
        badge.setPreferredSize(new Dimension(38,16));
        bestStyleBadges[index]=badge;
        tile.add(badge,BorderLayout.NORTH);
        tile.add(radio,BorderLayout.CENTER);
        tile.setBorder(new LineBorder(new Color(0,0,0,0),1));
        return tile;
    }

    private JButton osrsButton(String text)
    {
        JButton button = new JButton(text);
        button.setFont(FontManager.getRunescapeBoldFont());
        button.setForeground(OSRS_CREAM);
        button.setBackground(OSRS_SLOT);
        button.setBorder(new CompoundBorder(new LineBorder(OSRS_BORDER), new EmptyBorder(4, 6, 4, 6)));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 28));
        return button;
    }

    @SuppressWarnings("deprecation")
    private int prayerSpriteId(String prayer)
    {
        if (prayer == null) return -1;
        String p = prayer.toLowerCase();
        if (p.contains("piety")) return SpriteID.PRAYER_PIETY;
        if (p.contains("chivalry")) return SpriteID.PRAYER_CHIVALRY;
        if (p.contains("rigour")) return SpriteID.PRAYER_RIGOUR;
        if (p.contains("augury")) return SpriteID.PRAYER_AUGURY;
        if (p.contains("eagle eye")) return SpriteID.PRAYER_EAGLE_EYE;
        if (p.contains("mystic might")) return SpriteID.PRAYER_MYSTIC_MIGHT;
        if (p.contains("ultimate strength")) return SpriteID.PRAYER_ULTIMATE_STRENGTH;
        if (p.contains("incredible reflexes")) return SpriteID.PRAYER_INCREDIBLE_REFLEXES;
        return -1;
    }

    private String displayAttackStyle(AttackStyle style)
    {
        if(style==null)return "";
        switch(style){
            case MELEE_STAB:return "Stab";
            case MELEE_SLASH:return "Slash";
            case MELEE_CRUSH:return "Crush";
            case RANGED:return "Ranged";
            case MAGIC:return "Magic";
            default:return style.toString();
        }
    }

    private String displayTtk(double seconds)
    {
        if(!Double.isFinite(seconds))return "—";
        if(seconds<60.0)return String.format(java.util.Locale.ROOT,"%.1fs",seconds);
        return String.format(java.util.Locale.ROOT,"%dm %.1fs",(int)(seconds/60.0),seconds%60.0);
    }

    private void clearStyleSelection()
    {
        slash.setSelected(false); stab.setSelected(false); crush.setSelected(false);
        ranged.setSelected(false); magic.setSelected(false);
        updateStyleHighlight();
    }

    private JPanel messagePanel(String heading, String body)
    {
        JPanel panel=osrsCard();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 8,180));
        JLabel h=new JLabel(heading,SwingConstants.CENTER);
        h.setFont(headerFont());
        h.setForeground(OSRS_GOLD); h.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextArea b = new JTextArea(body);
        b.setEditable(false); b.setFocusable(false); b.setOpaque(false);
        b.setLineWrap(true); b.setWrapStyleWord(true);
        b.setFont(FontManager.getRunescapeFont()); b.setForeground(OSRS_CREAM);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setRows(4); b.setColumns(16);
        b.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 24, 85));
        panel.add(Box.createVerticalStrut(20)); panel.add(h); panel.add(Box.createVerticalStrut(8)); panel.add(b); panel.add(Box.createVerticalStrut(20));
        return panel;
    }

    private JPanel onboardingPanel()
    {
        JPanel panel=osrsCard();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 8,180));
        JLabel heading=new JLabel("How to use",SwingConstants.CENTER);
        heading.setFont(headerFont());
        heading.setForeground(OSRS_GOLD); heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(20)); panel.add(heading); panel.add(Box.createVerticalStrut(8));
        panel.add(onboardingLine("1. Select a target"));
        panel.add(onboardingLine("2. Generate the loadout"));
        JPanel bankLine=new JPanel(new FlowLayout(FlowLayout.CENTER,3,0)); bankLine.setOpaque(false);
        bankLine.add(onboardingLine("3. Open your bank and click"));
        try(InputStream in=PersonalBisPanel.class.getResourceAsStream("/personal-bis-icon.png"))
        {
            if(in!=null)bankLine.add(new JLabel(new ImageIcon(ImageIO.read(in).getScaledInstance(18,18,Image.SCALE_SMOOTH))));
        }
        catch(IOException ignored){}
        bankLine.setAlignmentX(Component.CENTER_ALIGNMENT);
        bankLine.setMaximumSize(new Dimension(Integer.MAX_VALUE,bankLine.getPreferredSize().height));
        panel.add(bankLine);
        panel.add(onboardingLine("for quick withdrawal.")); panel.add(Box.createVerticalStrut(20));
        return panel;
    }

    private JLabel onboardingLine(String text)
    {
        JLabel line=new JLabel(text,SwingConstants.CENTER);
        line.setFont(FontManager.getRunescapeFont()); line.setForeground(OSRS_CREAM);
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        return line;
    }

    private void ensureStatePanels()
    {
        if(loggedOutPanel==null){
            loggedOutPanel=messagePanel("🔒  Log in to begin","Log in to search monsters, scan your bank and find your best gear.");
            noTargetPanel=onboardingPanel();
            chooseStylePanel=messagePanel("Choose an attack style","Select Stab, Slash, Crush, Ranged or Magic to view your best gear.");
            loggedOutPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            noTargetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            chooseStylePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(loggedOutPanel); content.add(noTargetPanel); content.add(chooseStylePanel);
        }
    }

    private void updateProgressiveVisibility()
    {
        ensureStatePanels();
        boolean loggedIn=client.getGameState()==GameState.LOGGED_IN;
        boolean targetReady=loggedIn && targetExplicitlySelected && monsterBox.getSelectedItem() instanceof MonsterDefinition;
        boolean stylesReady=RecommendationReadiness.stylesVisible(targetReady);
        boolean resultsReady=RecommendationReadiness.resultsVisible(targetReady,bankChecked,styleExplicitlySelected) && loadoutReady;
        if(targetCard!=null)targetCard.setVisible(loggedIn);
        if(monsterCountLabel!=null){
            monsterCountLabel.setVisible(loggedIn);
            if(loggedIn)monsterCountLabel.setText(monsterDatabase.getAll().size()+" monsters loaded");
        }
        if(loggedOutPanel!=null)loggedOutPanel.setVisible(!loggedIn);
        if(noTargetPanel!=null)noTargetPanel.setVisible(loggedIn&&!targetReady);
        if(chooseStylePanel!=null)chooseStylePanel.setVisible(stylesReady&&loadoutsGenerated&&!styleExplicitlySelected);
        if (attackStyleHeading != null) attackStyleHeading.setVisible(stylesReady);
        if (stylesPanel != null) stylesPanel.setVisible(stylesReady);
        boolean advancedAvailable = targetReady && (groupStorageAvailable || ownedHasRubyBolts);
        advancedToggle.setVisible(advancedAvailable);
        advancedOptions.setVisible(advancedAvailable && advancedExpanded);
        groupStorageOptions.setVisible(targetReady && groupStorageAvailable);
        includeGroupStorage.setVisible(targetReady && groupStorageAvailable);
        groupStorageNote.setVisible(targetReady && groupStorageAvailable);
        boolean showRuby=targetReady&&ownedHasRubyBolts;
        includeRubyBolts.setVisible(showRuby); rubyBoltNote.setVisible(false);
        generateLoadoutButton.setText(loadoutsGenerated ? "Regenerate Loadout" : "Generate Loadout");
        generateLoadoutButton.setVisible(targetReady);
        generateMessage.setVisible(targetReady);
        if (gearSection != null) gearSection.setVisible(resultsReady);
        if (validationSection != null) validationSection.setVisible(resultsReady);
        content.revalidate();
        content.repaint();
    }

    private boolean validTarget(MonsterDefinition m)
    {
        if (m == null || m.getId() <= 0 || m.getName() == null || m.getName().trim().isEmpty()) return false;
        // Avoid presenting the transient/incomplete NPC definition observed during startup.
        // Real level-1 Defence monsters remain valid if their bundled record contains combat data.
        return m.getCombatLevel() > 0 && (m.getDefenceLevel() > 1 || m.getHitpoints() > 1
            || m.getMagicLevel() > 1 || m.getRangedLevel() > 1
            || m.getStabDefence()!=0 || m.getSlashDefence()!=0 || m.getCrushDefence()!=0
            || m.getMagicDefence()!=0 || m.getLightDefence()!=0 || m.getStandardDefence()!=0 || m.getHeavyDefence()!=0);
    }

    private void selectStyle(AttackStyle style)
    {
        if (!loadoutsGenerated) return;
        selectedStyle = style;
        styleExplicitlySelected = true;
        loadoutHeading.setText("Your best loadout ("+displayAttackStyle(style)+")");
        updateStyleHighlight();

        loadoutReady = false;
        generateMessage.setForeground(OSRS_CREAM);
        generateMessage.setText("Loading " + displayAttackStyle(style) + " loadout...");
        updateProgressiveVisibility();
        refreshRecommendationsAsync(false);
    }

    private void generateLoadouts()
    {
        if (groupStorageAvailable && includeGroupStorageEnabled && !groupSnapshot.hasSnapshot())
        {
            generateMessage.setText("Open group storage first, then try again.");
            generateMessage.setForeground(Color.RED);
            return;
        }
        if (!bankChecked)
        {
            generateMessage.setText("Please open your bank and try again.");
            generateMessage.setForeground(Color.RED);
            return;
        }
        if (!targetExplicitlySelected || !(monsterBox.getSelectedItem() instanceof MonsterDefinition))
        {
            generateMessage.setText("Choose a target first.");
            generateMessage.setForeground(Color.RED);
            return;
        }
        invalidateOptimizerCache();
        styleExplicitlySelected = false;
        clearStyleSelection();
        loadoutsGenerated = false;
        loadoutReady = false;
        generateLoadoutButton.setEnabled(false);
        setStyleControlsEnabled(false);
        generateMessage.setForeground(OSRS_CREAM);
        generateMessage.setText("Generating all attack styles...");
        updateProgressiveVisibility();
        refreshRecommendationsAsync(true);
    }

    public void exclusionsChanged()
    {
        SwingUtilities.invokeLater(this::updateExclusionWarning);
    }

    private void updateExclusionWarning()
    {
        boolean changed = loadoutsGenerated && !generationExclusions.sameAs(exclusions.snapshot());
        exclusionWarning.setText("<html>Exclusion list updated.<br>Generate again to apply changes.</html>");
        exclusionWarning.setVisible(changed);
        content.revalidate();
        content.repaint();
    }

    private void markLoadoutStale(String message)
    {
        calculationGeneration.incrementAndGet();
        loadoutsGenerated = false;
        loadoutReady = false;
        generateLoadoutButton.setEnabled(true);
        setStyleControlsEnabled(false);
        generateMessage.setForeground(OSRS_CREAM);
        generateMessage.setText(message == null || message.isEmpty() ? " " : message);
        SwingUtilities.invokeLater(this::updateProgressiveVisibility);
    }

    private void setStyleControlsEnabled(boolean enabled)
    {
        slash.setEnabled(enabled);
        stab.setEnabled(enabled);
        crush.setEnabled(enabled);
        ranged.setEnabled(enabled);
        magic.setEnabled(enabled);
    }

    private void updateStyleHighlight()
    {
        JRadioButton[] buttons = {slash, stab, crush, ranged, magic};
        AttackStyle[] styles = {AttackStyle.MELEE_SLASH,AttackStyle.MELEE_STAB,AttackStyle.MELEE_CRUSH,AttackStyle.RANGED,AttackStyle.MAGIC};
        for (int i=0;i<buttons.length;i++)
        {
            JRadioButton button=buttons[i];
            boolean selected=button.isSelected();
            boolean best=calculatedBestStyle==styles[i];
            button.setForeground(best ? OSRS_GREEN : (selected ? OSRS_GOLD : OSRS_CREAM));
            button.setFont((selected||best) ? FontManager.getRunescapeBoldFont() : FontManager.getRunescapeFont());
            button.setBackground(selected ? new Color(67, 58, 38) : (best ? new Color(39, 58, 39) : new Color(38, 38, 38)));
            if(bestStyleBadges[i]!=null)bestStyleBadges[i].setText(best?"BEST":" ");
            Container tile=button.getParent();
            if(tile instanceof JPanel)
            {
                ((JPanel)tile).setBorder(new LineBorder(best?OSRS_GOLD:new Color(0,0,0,0),best?2:1));
                ((JPanel)tile).setBackground(best?new Color(39,58,39):new Color(38,38,38));
            }
            button.setToolTipText(best ? "Best style for this target with your owned gear" : null);
        }
    }

    private void applyMonsterFilter()
    {
        if (slayerTargetMode) return;
        String query = targetEditorText();
        List<MonsterDefinition> matches = monsterDatabase.search(query, Integer.MAX_VALUE);
        matches.removeIf(this::isImpling);
        int slayerLevel = account.real(Skill.SLAYER);
        matches.removeIf(m -> !SlayerTargetRequirements.allowed(m, slayerLevel));
        Object selected = monsterBox.getSelectedItem();
        DefaultComboBoxModel<MonsterDefinition> model = new DefaultComboBoxModel<>();
        for (MonsterDefinition m : matches)
        {
            model.addElement(m);
        }
        populatingTargets = true;
        monsterBox.setModel(model);
        if (filteringTargets || !targetExplicitlySelected) monsterBox.setSelectedIndex(-1);
        if (!filteringTargets && targetExplicitlySelected && selected instanceof MonsterDefinition)
        {
            MonsterDefinition old = (MonsterDefinition) selected;
            for (int i = 0; i < model.getSize(); i++)
            {
                if (MonsterDatabase.sameCatalogueEntry(model.getElementAt(i), old))
                {
                    monsterBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        populatingTargets = false;
        if (filteringTargets) setTargetEditorText(query);
        else if (selected instanceof MonsterDefinition && targetExplicitlySelected)
            setTargetEditorText(((MonsterDefinition) selected).getName());
        else if (!targetExplicitlySelected)
            setTargetEditorText(query);
        updateProgressiveVisibility();
    }

    private String targetEditorText()
    {
        Object item = monsterBox.getEditor().getItem();
        return item == null ? "" : item.toString();
    }

    private void setTargetEditorText(String text)
    {
        if (monsterBox.isEditable()) monsterBox.getEditor().setItem(text == null ? "" : text);
    }

    private MonsterDefinition resolveTypedTarget()
    {
        if (slayerTargetMode) return null;
        String query = targetEditorText().trim();
        if (query.isEmpty()) return null;
        for (MonsterDefinition m : monsterDatabase.search(query, Integer.MAX_VALUE))
            if (!isImpling(m) && SlayerTargetRequirements.allowed(m, account.real(Skill.SLAYER))
                && (m.getName().equalsIgnoreCase(query) || m.toString().equalsIgnoreCase(query))) return m;
        return null;
    }

    private boolean isImpling(MonsterDefinition monster)
    {
        return monster!=null && monster.getName()!=null
            && monster.getName().toLowerCase(java.util.Locale.ROOT).contains("impling");
    }

    public void refreshAll()
    {
        invalidateOptimizerCache();
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            SwingUtilities.invokeLater(this::updateProgressiveVisibility);
            return;
        }
        account.refresh();
        if (monsterDatabase.getAll().isEmpty())
        {
            monsterDatabase.rebuild();
        }
        SwingUtilities.invokeLater(this::updateProgressiveVisibility);
        updateSlayerTask();
        SwingUtilities.invokeLater(() ->
        {
            if (monsterBox.getItemCount() == 0)
            {
                applyMonsterFilter();
            }
        });
    }

    public void updatePlayerStats()
    {
        groupStorageAvailable = client.getAccountType() != null && client.getAccountType().isGroupIronman();
        account.refresh();
        // Stat/XP events may fire repeatedly during ordinary combat. Refresh the
        // account snapshot for the next manual run without invalidating the five
        // generated style results or launching another optimisation pass.
    }

    public void captureGroupStorage(net.runelite.api.events.ItemContainerChanged event)
    {
        if (groupSnapshot.capture(event, client::getItemDefinition, java.time.Instant::now))
        {
            bankFilter.setGroupItems(groupStorageAvailable && includeGroupStorageEnabled ? groupSnapshot.getItems() : Collections.emptyList());
            SwingUtilities.invokeLater(() -> { updateGroupStorageNote(); invalidateOptimizerCache(); });
        }
    }

    public void clearGroupStorage()
    {
        groupSnapshot.clear();
        bankFilter.setGroupItems(Collections.emptyList());
        SwingUtilities.invokeLater(() -> { updateGroupStorageNote(); invalidateOptimizerCache();
            if (loadoutsGenerated) markLoadoutStale("Group snapshot cleared — regenerate."); });
    }

    private void updateGroupStorageNote()
    {
        groupStorageNote.setText("Changes made by teammates won't be reflected until storage is reopened.\n" +
            (groupSnapshot.hasSnapshot() ? "Last snapshot: " + java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss").withZone(java.time.ZoneId.systemDefault()).format(groupSnapshot.getCapturedAt()) + " (" + groupSnapshot.getItems().size() + " items)" : "Group storage: not scanned"));
    }

    public void refreshBankItems()
    {
        // ItemContainerChanged runs on RuneLite's client thread. Keep this path to
        // a lightweight immutable ownership snapshot. Existing generated loadouts
        // remain available until the player deliberately regenerates them.
        cachedBank = bankScanner.scan();
        bankFilter.setPersonalItems(cachedBank);
        ownedHasRubyBolts = hasRubyBolts(bankScanner.scanOwnedGear(cachedBank));
        bankChecked = true;

        SwingUtilities.invokeLater(() ->
        {
            updateProgressiveVisibility();
            if (bankFilter.isActive())
            {
                bankFilter.refreshLayout();
            }
        });
    }

    private boolean hasRubyBolts(List<BankItem> items)
    {
        if (items == null) return false;
        for (BankItem item : items)
        {
            if (item != null && item.getQuantity() > 0 && rubyBoltItem(item)) return true;
        }
        return false;
    }

    /** Refresh carried/worn ownership on the client thread, without rescanning the bank. */
    public void refreshRubyBoltOwnership()
    {
        boolean updated = hasRubyBolts(bankScanner.scanOwnedGear(cachedBank));
        if (updated == ownedHasRubyBolts) return;
        ownedHasRubyBolts = updated;
        SwingUtilities.invokeLater(this::updateProgressiveVisibility);
    }

    private boolean rubyBoltItem(BankItem item)
    {
        return item!=null&&item.getName()!=null
            &&item.getName().toLowerCase(java.util.Locale.ROOT).contains("ruby bolts (e)");
    }

    public void updateSlayerTask()
    {
        String task = currentSlayerTask();
        String location = currentSlayerLocation();
        boolean changed = !java.util.Objects.equals(task, lastSlayerTask)
            || !java.util.Objects.equals(location, lastSlayerLocation);
        lastSlayerTask = task;
        lastSlayerLocation = location;
        if (!slayerTargetMode)
        {
            return;
        }

        List<MonsterDefinition> variants = monsterDatabase.findTaskVariants(task, location);
        variants.removeIf(m -> !SlayerTargetRequirements.allowed(m, account.real(Skill.SLAYER)));
        Set<String> unverified = new HashSet<>();
        for (MonsterDefinition variant : monsterDatabase.findTaskVariants(task))
            if (SlayerTargetRequirements.allowed(variant, account.real(Skill.SLAYER))
                && monsterDatabase.taskLocationEligibility(task, variant, location) == SlayerAssignmentLocation.Eligibility.UNKNOWN)
                unverified.add(MonsterDatabase.combatKey(variant));
        SwingUtilities.invokeLater(() ->
        {
            if (!slayerTargetMode || !java.util.Objects.equals(task, lastSlayerTask)
                || !java.util.Objects.equals(location, lastSlayerLocation)) return;
            MonsterDefinition previous = monsterBox.getSelectedItem() instanceof MonsterDefinition
                ? (MonsterDefinition) monsterBox.getSelectedItem() : null;
            DefaultComboBoxModel<MonsterDefinition> model = new DefaultComboBoxModel<>();
            for (MonsterDefinition variant : variants)
            {
                model.addElement(variant);
            }
            populatingTargets = true;
            unverifiedTaskTargets = Collections.unmodifiableSet(unverified);
            monsterBox.setModel(model);

            // Preserve the user's chosen variant while the task is unchanged.
            if (!changed && previous != null)
            {
                for (int i = 0; i < model.getSize(); i++)
                {
                    if (MonsterDatabase.sameCatalogueEntry(model.getElementAt(i), previous))
                    {
                        monsterBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            if (model.getSize() > 0)
            {
                MonsterDefinition selected = (MonsterDefinition) monsterBox.getSelectedItem();
                setTargetEditorText(task);
                monsterInfo.setText("Slayer: " + task + " • choose variant (" + model.getSize() + ")");
                if (selected == null)
                {
                    monsterBox.setSelectedIndex(0);
                    selected = (MonsterDefinition) monsterBox.getSelectedItem();
                }
                if (selected != null)
                {
                    targetExplicitlySelected = true;
                    if (changed || !MonsterDatabase.sameCatalogueEntry(previous, selected))
                    {
                        styleExplicitlySelected = false;
                        clearStyleSelection();
                        markLoadoutStale("");
                    }
                    updateProgressiveVisibility();
                }
            }
            else
            {
                monsterInfo.setText("Slayer: " + task + " • no monster variants found");
                targetExplicitlySelected = false;
                styleExplicitlySelected = false;
                clearStyleSelection();
                markLoadoutStale("");
            }
            populatingTargets = false;
            if (changed) markLoadoutStale("Slayer assignment changed — regenerate the loadout.");
            updateSlayerUiState();
            updateSlayerAssignmentNote();
            updateProgressiveVisibility();
        });
    }

    private String currentSlayerLocation()
    {
        // Krystilia assignments count only inside the Wilderness, even when
        // SlayerPluginService has no explicit Konar-style location string.
        return SlayerAssignmentLocation.assignment(slayerService.getTaskLocation(),
            client.getVarbitValue(net.runelite.api.gameval.VarbitID.SLAYER_MASTER),
            client.getVarpValue(net.runelite.api.gameval.VarPlayerID.SLAYER_AREA) > 0, currentSlayerTask());
    }

    private String currentSlayerTask()
    {
        return slayerService.getRemainingAmount() > 0 ? slayerService.getTask() : null;
    }

    private void updateSlayerAssignmentNote()
    {
        slayerAssignmentNote.setVisible(slayerTargetMode);
        if (!slayerTargetMode) return;
        String text = SlayerAssignmentLocation.note(lastSlayerTask, lastSlayerLocation,
            unverifiedTaskTargets.size(), SlayerTaskCatalogue.knownTask(lastSlayerTask));
        if (monsterBox.getItemCount() == 0 && lastSlayerTask != null)
            text += lastSlayerLocation == null || lastSlayerLocation.isEmpty()
                ? " No matching targets available in the bundled catalogue."
                : " No confirmed targets available for this assigned location.";
        slayerAssignmentNote.setText(text);
        slayerAssignmentNote.setForeground(unverifiedTaskTargets.isEmpty() ? OSRS_CREAM : OSRS_GOLD);
        content.revalidate();
    }

    public void refreshSlayerTaskIfChanged()
    {
        String task = currentSlayerTask();
        if (!java.util.Objects.equals(task, lastSlayerTask)
            || !java.util.Objects.equals(currentSlayerLocation(), lastSlayerLocation))
        {
            updateSlayerTask();
        }
    }

    private void refreshRecommendationsAsync(boolean calculateAllStyles)
    {
        final long generation=calculationGeneration.incrementAndGet();
        clientThread.invokeLater(() -> captureAndCalculate(generation, calculateAllStyles));
    }

    private void captureAndCalculate(long generation, boolean calculateAllStyles)
    {
        if (client.getGameState() != GameState.LOGGED_IN) return;
        if (!targetExplicitlySelected || !bankChecked || (!calculateAllStyles && !styleExplicitlySelected))
        {
            SwingUtilities.invokeLater(this::updateProgressiveVisibility);
            return;
        }
        MonsterDefinition selectedMonster=(MonsterDefinition) monsterBox.getSelectedItem();
        MonsterDefinition monster = monsterDatabase.canonical(selectedMonster);
        if (!validTarget(monster))
        {
            SwingUtilities.invokeLater(() ->
            {
                gearSection.setVisible(false);
                validationSection.setVisible(false);
                content.revalidate();
                content.repaint();
            });
            return;
        }
        // Recommended remains bank-only (and therefore placeholder-free), while
        // loadout ownership also includes items currently worn or carried.
        if (calculateAllStyles)
        {
            generationExclusions = exclusions.snapshot();
            List<BankItem> stores = new ArrayList<>(cachedBank);
            if (groupStorageAvailable && includeGroupStorageEnabled) stores.addAll(groupSnapshot.getItems());
            generationBank = exclusions.filter(stores, generationExclusions);
            generationOwned = exclusions.filter(bankScanner.scanOwnedGear(stores), generationExclusions);
        }
        // Style switches reuse the exact ownership/exclusion policy of the last
        // generation, even while the user batches changes in the settings list.
        final List<BankItem> bankSnapshot = new ArrayList<>(generationBank);
        final List<BankItem> ownedGearSnapshot = new ArrayList<>(generationOwned);
        final boolean includeRuby=includeRubyBoltsEnabled&&hasRubyBolts(ownedGearSnapshot);
        final Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> rankedSnapshot=new EnumMap<>(AttackStyle.class);
        for(AttackStyle style:AttackStyle.values()){
            List<BankItem> styleBank=ownedGearSnapshot;
            if(style==AttackStyle.RANGED&&!includeRuby){styleBank=new ArrayList<>(ownedGearSnapshot);styleBank.removeIf(this::rubyBoltItem);}
            rankedSnapshot.put(style,intelligence.rank(styleBank,style,slayerMode));
        }
        final AttackStyle requestedStyle=selectedStyle;
        final boolean requestedSlayerMode=slayerMode;

        final String cacheKey=calculateAllStyles
            ? optimizerCacheKey(monster,ownedGearSnapshot,requestedSlayerMode)
            : optimizerCacheKey;
        optimizerExecutor.execute(() -> calculateRecommendations(generation,ownedGearSnapshot,bankSnapshot,rankedSnapshot,monster,requestedStyle,requestedSlayerMode,calculateAllStyles,cacheKey));
    }

    private void calculateRecommendations(long generation,List<BankItem> ownedGearSnapshot,List<BankItem> bankSnapshot,
        Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> rankedSnapshot,
        MonsterDefinition monster,AttackStyle requestedStyle,boolean requestedSlayerMode,boolean calculateAllStyles,String cacheKey)
    {
        if(generation!=calculationGeneration.get())return;
        Map<AttackStyle, List<Integer>> styleRecommendations = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, OptimizedLoadout> meleeLoads = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, OptimizedRangedLoadout> rangedLoads = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, OptimizedMagicLoadout> magicLoads = new EnumMap<>(AttackStyle.class);
        Map<EquipmentSlot, List<EquipmentCandidate>> selectedRanked = null;
        final boolean optimizerCacheHit = cacheKey.equals(optimizerCacheKey);
        if (!optimizerCacheHit)
        {
            cachedMeleeReports.clear();
            cachedRangedReports.clear();
            cachedMagicReports.clear();
        }

        // alpha50.4.49.1: Best Style must use the same post-mechanics DPS that
        // the individual style panels display.  The old quick equipment score could
        // rank an immune/restricted style above a genuinely higher-DPS style (Kurask
        // exposed this as Ranged 4.5681 being labelled best over Stab 6.0416).
        // Optimise every style here; cached reports still make subsequent switches cheap.
        for (AttackStyle style : AttackStyle.values())
        {
            if (!calculateAllStyles && style != requestedStyle) continue;
            final Map<EquipmentSlot,List<EquipmentCandidate>> ranked = rankedSnapshot.get(style);
            if (style == requestedStyle) selectedRanked = ranked;

            if (style == AttackStyle.MELEE_STAB || style == AttackStyle.MELEE_SLASH || style == AttackStyle.MELEE_CRUSH)
            {
                MeleeOptimizationReport report = optimizerCacheHit ? cachedMeleeReports.get(style) : null;
                if (report == null)
                {
                    report = meleeOptimizer.optimizeWithReportRanked(ranked, style, monster, requestedSlayerMode);
                    cachedMeleeReports.put(style, report);
                }
                OptimizedLoadout load = report == null ? null : report.getBest();
                meleeLoads.put(style, load);
                List<Integer> ids = load == null ? new ArrayList<>() : load.itemIds();
                styleRecommendations.put(style, ids);
            }
            else if (style == AttackStyle.RANGED)
            {
                RangedOptimizationReport report = optimizerCacheHit ? cachedRangedReports.get(style) : null;
                if (report == null)
                {
                    report = rangedOptimizer.optimizeWithReportRanked(ranked, monster, requestedSlayerMode);
                    cachedRangedReports.put(style, report);
                }
                OptimizedRangedLoadout load = report == null ? null : report.getBest();
                rangedLoads.put(style, load);
                List<Integer> ids = load == null ? new ArrayList<>() : load.itemIds();
                styleRecommendations.put(style, ids);
            }
            else if (style == AttackStyle.MAGIC)
            {
                MagicOptimizationReport mr = optimizerCacheHit ? cachedMagicReports.get(style) : null;
                if (mr == null)
                {
                    mr = magicOptimizer.optimizeWithReportRanked(ownedGearSnapshot, ranked, monster, requestedSlayerMode);
                    cachedMagicReports.put(style, mr);
                }
                OptimizedMagicLoadout load = mr == null ? null : mr.getBest();
                magicLoads.put(style, load);
                List<Integer> ids = load == null ? new ArrayList<>() : load.itemIds();
                styleRecommendations.put(style, ids);
            }
        }
        if(generation!=calculationGeneration.get())return;
        optimizerCacheKey = cacheKey;
        // A selected-style result is rendered immediately for sub-tick responsiveness.
        // Only label a style "best" once exact reports exist for all five styles;
        // otherwise the sole selected style would inevitably (and incorrectly) win.
        final AttackStyle resultBestStyle=cachedBestStyleIfComplete();

        // Bank mode follows the explicit sidebar choice. Style sections may use
        // zero-quantity placeholders for gear that is currently worn or carried;
        // the separate Recommended supply section remains positive-bank-owned only.
        List<AttackStyle> bankStyles=new ArrayList<>();
        bankStyles.add(requestedStyle);
        Map<AttackStyle,List<Integer>> selectedBankRecommendations=new EnumMap<>(AttackStyle.class);
        List<Integer> selectedIds=styleRecommendations.get(requestedStyle);
        if(selectedIds==null)selectedIds=new ArrayList<>();
        Set<Integer> bankOwnedIds=new HashSet<>();
        for(BankItem item:bankSnapshot)bankOwnedIds.add(item.getItemId());
        selectedBankRecommendations.put(requestedStyle,new ArrayList<>(selectedIds));
        Set<Integer> selectedRecommendedIds=new HashSet<>();
        for(Integer id:selectedIds)if(bankOwnedIds.contains(id))selectedRecommendedIds.add(id);
        final List<RecommendedSupply> resultSupplies=SimpleSupplyRecommender.recommend(
            requestedStyle, magicLoads.get(requestedStyle), rangedLoads.get(requestedStyle), bankSnapshot, monster, account.real(Skill.PRAYER));
        clientThread.invokeLater(() -> {
            if(generation!=calculationGeneration.get())return;
            bankFilter.setTarget(monster);
            bankFilter.setBestStyle(resultBestStyle);
            bankFilter.setRecommendations(selectedBankRecommendations,bankStyles,selectedRecommendedIds);
            bankFilter.setSupplies(resultSupplies);
        });

        final Map<EquipmentSlot, EquipmentCandidate> selectedGear = new EnumMap<>(EquipmentSlot.class);
        final CombatResult selectedCombat;
        OptimizedLoadout selectedLoad=meleeLoads.get(requestedStyle);
        final RangedCombatResult selectedRanged;
        if(selectedLoad!=null){selectedGear.putAll(selectedLoad.getItems());selectedCombat=selectedLoad.getResult();selectedRanged=null;}
        else if(requestedStyle==AttackStyle.RANGED && rangedLoads.get(requestedStyle)!=null){
            OptimizedRangedLoadout rl=rangedLoads.get(requestedStyle);selectedGear.putAll(rl.getItems());selectedCombat=null;selectedRanged=rl.getResult();
        }
        else if(requestedStyle==AttackStyle.MAGIC && magicLoads.get(requestedStyle)!=null){
            OptimizedMagicLoadout ml=magicLoads.get(requestedStyle);selectedGear.putAll(ml.getItems());selectedCombat=null;selectedRanged=null;
        }
        else {if(selectedRanked==null)selectedRanked=rankedSnapshot.get(requestedStyle);selectedRanked=SlayerTargetRequirements.constrainRanked(monster,selectedRanked);for(EquipmentSlot s:EquipmentSlot.values()){EquipmentCandidate c=firstUsable(selectedRanked.get(s));if(c!=null)selectedGear.put(s,c);}selectedCombat=null;selectedRanged=null;}
        String prayer=account.recommendedPrayer(requestedStyle);
        final OptimizedRangedLoadout selectedRangedLoad = rangedLoads.get(requestedStyle);
        final RangedCombatResult uiRanged = selectedRangedLoad == null ? selectedRanged : selectedRangedLoad.getResult();
        final OptimizedMagicLoadout selectedMagicLoad = magicLoads.get(requestedStyle);
        final MagicCombatResult uiMagic = selectedMagicLoad == null ? null : selectedMagicLoad.getResult();

        final AttackStyle uiBestStyle=resultBestStyle;
        SwingUtilities.invokeLater(()->{
            if(generation!=calculationGeneration.get())return;
            calculatedBestStyle=uiBestStyle;
            updateStyleHighlight();
            String calculatedPrayer = selectedCombat != null && selectedCombat.getPrayer() != null
                ? selectedCombat.getPrayer().toString()
                : (uiRanged != null && uiRanged.getPrayer()!=null ? uiRanged.getPrayer().toString() : prayer);
            prayerIcon.setToolTipText("Prayer: " + calculatedPrayer);
            prayerIcon.setText(calculatedPrayer == null || calculatedPrayer.equalsIgnoreCase("None") ? "—" : "");
            prayerIcon.setIcon(null);
            int prayerSprite = prayerSpriteId(calculatedPrayer);
            if (prayerSprite >= 0)
            {
                spriteManager.getSpriteAsync(prayerSprite, 0, image ->
                    SwingUtilities.invokeLater(() -> prayerIcon.setIcon(image == null ? null : new ImageIcon(image))));
            }
            if(selectedCombat!=null)resultTitle.setText(String.format("Best owned setup — %s  •  %.2f DPS",requestedStyle,selectedCombat.getDps()));
            else if(uiRanged!=null)resultTitle.setText(String.format("Best owned setup — %s  •  %.2f DPS",requestedStyle,uiRanged.getDps()));
            else resultTitle.setText("Best owned setup — "+requestedStyle);
            if (monster == null) monsterInfo.setText("Target: —");
            else {
                String requirement = SlayerTargetRequirements.requiredItem(monster);
                String warning = !SlayerTargetRequirements.allowed(monster, account.real(Skill.SLAYER))
                    ? "  •  Slayer level too low (requires " + SlayerTargetRequirements.requiredLevel(monster) + ")" : "";
                monsterInfo.setText(monster.getName()+(monster.getVersion().isEmpty()?"":" ["+monster.getVersion()+"]")+"  •  lvl "+monster.getCombatLevel()+"  •  def "+monster.getDefenceLevel()+" / "+monster.defenceFor(requestedStyle)+(requirement.isEmpty()?"":"  •  Requires "+requirement)+warning);
                monsterInfo.setForeground(warning.isEmpty() ? OSRS_CREAM : Color.RED);
            }
            for (int i=0; i<EquipmentSlot.values().length; i++)
            {
                EquipmentSlot slot=EquipmentSlot.values()[i];
                EquipmentCandidate best=selectedGear.get(slot);
                JLabel label=gear[i];
                label.putClientProperty("personalbis.itemId", best == null ? null : best.getItem().getItemId());
                if (best==null)
                {
                    label.setText("—");
                    label.setIcon(null);
                    label.setToolTipText(slot.getDisplayName()+": no item selected");
                }
                else
                {
                    label.setText("");
                    label.setIcon(null);
                    final int requestedItemId = best.getItem().getItemId();
                    itemManager.getImage(requestedItemId, 1, false).addTo(label);
                    String tip=slot.getDisplayName()+": "+best.getItem().getName()+" — "+best.getRequirementResult().getMessage();
                    if(selectedCombat!=null) tip+=String.format(" | setup DPS %.3f | accuracy %.1f%% | max hit %d",selectedCombat.getDps(),selectedCombat.getAccuracy()*100,selectedCombat.getMaxHit());
                    else if(uiRanged!=null) tip+=String.format(" | setup DPS %.3f | accuracy %.1f%% | max hit %d",uiRanged.getDps(),uiRanged.getAccuracy()*100,uiRanged.getMaxHit());
                    label.setToolTipText(tip);
                }
            }
            int prayerBonus = 0;
            for (EquipmentCandidate candidate : selectedGear.values())
            {
                if (candidate != null) prayerBonus += candidate.getPrayer();
            }

            StringBuilder summary = new StringBuilder();
            EquipmentSlot requiredSlayerSlot=SlayerTargetRequirements.requiredEquipmentSlot(monster);
            boolean missingSlayerItem=requiredSlayerSlot!=null&&!selectedGear.containsKey(requiredSlayerSlot);
            if(missingSlayerItem){
                slayerItemWarning.setText("⚠ Empty "+requiredSlayerSlot.getDisplayName()+" slot — requires "+SlayerTargetRequirements.requiredItem(monster));
                slayerItemWarning.setVisible(true);
            }else{
                slayerItemWarning.setText("");
                slayerItemWarning.setVisible(false);
            }
            if (selectedCombat != null)
            {
                summary.append(String.format("DPS: %.4f%n", selectedCombat.getDps()));
                summary.append("Estimated TTK: ").append(displayTtk(selectedCombat.getExpectedTtkSeconds())).append(System.lineSeparator());
                summary.append(String.format("Max Hit: %d%n", selectedCombat.getMaxHit()));
                summary.append(String.format("Accuracy: %.2f%%%n", selectedCombat.getAccuracy() * 100.0));
                summary.append(String.format("Style: %s%n", selectedCombat.getStance()));
                summary.append(String.format("Prayer Bonus: %+d", prayerBonus));
            }
            else if (uiRanged != null)
            {
                summary.append(String.format("DPS: %.4f%n", uiRanged.getDps()));
                summary.append("Estimated TTK: ").append(displayTtk(uiRanged.getExpectedTtkSeconds())).append(System.lineSeparator());
                summary.append(String.format("Max Hit: %d%n", uiRanged.getMaxHit()));
                summary.append(String.format("Accuracy: %.2f%%%n", uiRanged.getAccuracy() * 100.0));
                summary.append(String.format("Style: %s%n", uiRanged.getStance()));
                summary.append(String.format("Prayer Bonus: %+d", prayerBonus));
            }
            else if (uiMagic != null)
            {
                summary.append(String.format("DPS: %.4f%n", uiMagic.getDps()));
                summary.append("Estimated TTK: ").append(displayTtk(uiMagic.getExpectedTtkSeconds())).append(System.lineSeparator());
                summary.append(String.format("Max Hit: %d%n", uiMagic.getMaxHit()));
                summary.append(String.format("Accuracy: %.2f%%%n", uiMagic.getAccuracy() * 100.0));
                summary.append(String.format("Style: %s%n", uiMagic.getSpell()));
                summary.append(String.format("Prayer Bonus: %+d", prayerBonus));
            }
            else
            {
                summary.append(requestedStyle == AttackStyle.MAGIC
                    ? "No castable combat spell found with your current Magic level and bank runes."
                    : "No calculated loadout is available for this style.");
            }
            meleeValidation.setText(summary.toString());
            meleeValidation.setCaretPosition(0);

            loadoutsGenerated=true;
            loadoutReady=styleExplicitlySelected;
            generateLoadoutButton.setEnabled(true);
            setStyleControlsEnabled(true);
            generateMessage.setForeground(OSRS_CREAM);
            generateMessage.setText(selectedCombat == null && uiRanged == null && uiMagic == null
                ? "<html>No usable setup.<br>Check ownership and exclusions.</html>"
                : (calculateAllStyles ? "Loadouts ready." : " "));
            updateExclusionWarning();
            updateProgressiveVisibility();
            status.setText(monsterDatabase.getAll().size()+" unique monster variants  •  "+bankSnapshot.size()+" bank items"+(selectedCombat==null?"":"  •  melee validation alpha7"));
        });
        if(!calculateAllStyles && resultBestStyle==null)
            optimizerExecutor.execute(() -> calculateMissingBestStyles(generation,cacheKey,ownedGearSnapshot,
                rankedSnapshot,monster,requestedSlayerMode));
    }

    private AttackStyle cachedBestStyleIfComplete()
    {
        Map<AttackStyle,Double> scores=new EnumMap<>(AttackStyle.class);
        for(AttackStyle style:AttackStyle.values())
        {
            double ttk=Double.POSITIVE_INFINITY;
            if(style==AttackStyle.MELEE_STAB||style==AttackStyle.MELEE_SLASH||style==AttackStyle.MELEE_CRUSH)
            {
                MeleeOptimizationReport report=cachedMeleeReports.get(style);
                if(report==null)return null;
                if(report.getBest()!=null&&report.getBest().getResult()!=null)ttk=report.getBest().getResult().getExpectedTtkSeconds();
            }
            else if(style==AttackStyle.RANGED)
            {
                RangedOptimizationReport report=cachedRangedReports.get(style);
                if(report==null)return null;
                if(report.getBest()!=null&&report.getBest().getResult()!=null)ttk=report.getBest().getResult().getExpectedTtkSeconds();
            }
            else
            {
                MagicOptimizationReport report=cachedMagicReports.get(style);
                if(report==null)return null;
                if(report.getBest()!=null&&report.getBest().getResult()!=null)ttk=report.getBest().getResult().getExpectedTtkSeconds();
            }
            scores.put(style,Double.isFinite(ttk)?-ttk:Double.NEGATIVE_INFINITY);
        }
        return BestStyleSelection.best(scores);
    }

    private void calculateMissingBestStyles(long generation,String cacheKey,List<BankItem> bankSnapshot,
        Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> rankedSnapshot,
        MonsterDefinition monster,boolean requestedSlayerMode)
    {
        for(AttackStyle style:AttackStyle.values())
        {
            if(generation!=calculationGeneration.get()||!cacheKey.equals(optimizerCacheKey))return;
            Map<EquipmentSlot,List<EquipmentCandidate>> ranked=rankedSnapshot.get(style);
            if(style==AttackStyle.MELEE_STAB||style==AttackStyle.MELEE_SLASH||style==AttackStyle.MELEE_CRUSH)
            {
                if(!cachedMeleeReports.containsKey(style))
                    cachedMeleeReports.put(style,meleeOptimizer.optimizeWithReportRanked(ranked,style,monster,requestedSlayerMode));
            }
            else if(style==AttackStyle.RANGED)
            {
                if(!cachedRangedReports.containsKey(style))
                    cachedRangedReports.put(style,rangedOptimizer.optimizeWithReportRanked(ranked,monster,requestedSlayerMode));
            }
            else if(!cachedMagicReports.containsKey(style))
                cachedMagicReports.put(style,magicOptimizer.optimizeWithReportRanked(bankSnapshot,ranked,monster,requestedSlayerMode));
        }
        AttackStyle best=cachedBestStyleIfComplete();
        if(best==null||generation!=calculationGeneration.get()||!cacheKey.equals(optimizerCacheKey))return;
        clientThread.invokeLater(() -> bankFilter.setBestStyle(best));
        SwingUtilities.invokeLater(() ->
        {
            if(generation!=calculationGeneration.get())return;
            calculatedBestStyle=best;
            updateStyleHighlight();
        });
    }

    private EquipmentCandidate firstUsable(List<EquipmentCandidate> candidates)
    {
        if (candidates == null)
        {
            return null;
        }
        for (EquipmentCandidate c : candidates)
        {
            if (!c.getRequirementResult().isBlocked())
            {
                return c;
            }
        }
        return null;
    }

    public void showBankView()
    {
        bankFilter.toggle();
    }

    private class MonsterRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setToolTipText(value == null ? null : value.toString());
            label.setFont(FontManager.getRunescapeFont());
            label.setBorder(new EmptyBorder(4, 5, 4, 5));
            if (isSelected)
            {
                label.setBackground(OSRS_BORDER);
                label.setForeground(Color.WHITE);
            }
            else
            {
                label.setBackground(ColorScheme.DARK_GRAY_COLOR);
                label.setForeground(OSRS_CREAM);
            }
            return label;
        }
    }

}
