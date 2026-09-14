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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
    private final RangedCombatCalculator rangedCalculator;
    private final MagicLoadoutOptimizer magicOptimizer;
    private final SlayerPluginService slayerService;
    private final MonsterDatabase monsterDatabase;
    private final AccountSnapshot account;
    private final PersonalBisBankFilter bankFilter;

    private final JPanel content = new JPanel();
    private final JComboBox<MonsterDefinition> monsterBox = new JComboBox<>();
    private final JTextField monsterSearch = new JTextField();
    private final JCheckBox slayerTask = new JCheckBox("Load Slayer Task");
    private final JComboBox<String> prayerBox = new JComboBox<>();
    private final JComboBox<String> boostBox = new JComboBox<>();
    private final JLabel status = new JLabel("Log in to load account and monsters");
    private final JLabel resultTitle = new JLabel("Best owned setup");
    private final JLabel accountLevels = new JLabel("Levels: —");
    private final JLabel monsterInfo = new JLabel("Target: —");
    private final JTextArea meleeValidation = new JTextArea();
    private final JLabel slayerItemWarning = new JLabel();
    private final JTextArea loadoutValidation = new JTextArea();
    private final JLabel[] gear = new JLabel[EquipmentSlot.values().length];
    private final long[] gearImageGeneration = new long[EquipmentSlot.values().length];
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
    private final JLabel loadoutHeading = new JLabel("Your best loadout");
    private AttackStyle calculatedBestStyle;
    private boolean targetExplicitlySelected;
    private boolean bankChecked;
    private boolean styleExplicitlySelected;
    private boolean populatingTargets;
    private boolean filteringTargets;

    private List<BankItem> cachedBank = Collections.emptyList();
    private String lastSlayerTask;
    private volatile boolean slayerMode;
    private volatile boolean slayerTargetMode;
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
            .append('|').append(slayer);
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
        BankScanner bankScanner, EquipmentIntelligence intelligence, MeleeLoadoutOptimizer meleeOptimizer, RangedLoadoutOptimizer rangedOptimizer, RangedCombatCalculator rangedCalculator, MagicLoadoutOptimizer magicOptimizer, SlayerPluginService slayerService,
        MonsterDatabase monsterDatabase, AccountSnapshot account, PersonalBisBankFilter bankFilter)
    {
        super(false);
        this.client = client;
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        this.spriteManager = spriteManager;
        this.bankScanner = bankScanner;
        this.intelligence = intelligence;
        this.meleeOptimizer = meleeOptimizer;
        this.rangedOptimizer = rangedOptimizer;
        this.rangedCalculator = rangedCalculator;
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
        content.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 660));
        content.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH, Integer.MAX_VALUE));
        buildUi();

        // PluginPanel's default constructor creates a JScrollPane with a Look&Feel border.
        // Use the unwrapped panel and provide our own borderless scroll pane instead.
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(OSRS_PANEL);
        northPanel.add(content, BorderLayout.NORTH);
        JScrollPane panelScroll = new JScrollPane(northPanel);
        panelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelScroll.setBorder(null);
        panelScroll.setViewportBorder(null);
        add(panelScroll, BorderLayout.CENTER);
    }

    private void buildUi()
    {
        JLabel title = new JLabel("My BiS Finder", SwingConstants.LEFT);
        title.setFont(FontManager.getRunescapeBoldFont().deriveFont(17f));
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
        monsterCountLabel.setFont(FontManager.getRunescapeSmallFont());
        monsterCountLabel.setForeground(OSRS_CREAM);
        monsterCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(monsterCountLabel);
        content.add(Box.createVerticalStrut(4));

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
                    updateProgressiveVisibility();
                }
            }
            refreshRecommendationsAsync();
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
                refreshRecommendationsAsync();
            });
        });
        content.add(targetCard);
        content.add(Box.createVerticalStrut(4));
        targetCard.setVisible(false);
        monsterCountLabel.setVisible(false);

        // Prayer and boost controls are intentionally hidden; recommendations are automatic.

        resultTitle.setFont(FontManager.getRunescapeBoldFont().deriveFont(15f));
        resultTitle.setForeground(OSRS_GREEN);
        resultTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
monsterInfo.setFont(FontManager.getRunescapeSmallFont());
        monsterInfo.setForeground(OSRS_CREAM);
        monsterInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
content.add(Box.createVerticalStrut(5));

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
        content.add(styles);
        content.add(Box.createVerticalStrut(4));

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
        loadoutHeading.setFont(FontManager.getRunescapeBoldFont().deriveFont(14f));
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
            label.setFont(FontManager.getRunescapeSmallFont());
            label.setForeground(OSRS_CREAM);
            label.setOpaque(true);
            label.setBackground(new Color(38, 38, 38));
            label.setPreferredSize(new Dimension(48, 50));
            label.setMinimumSize(new Dimension(40, 40));
            label.setBorder(BorderFactory.createEmptyBorder());
            gear[slot.ordinal()] = label;
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
        validationCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
        validationCard.add(sectionLabel("Loadout stats"));
        slayerItemWarning.setForeground(Color.RED);
        slayerItemWarning.setFont(FontManager.getRunescapeSmallFont());
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
        meleeValidation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        validationCard.add(meleeValidation);

        validationSection = validationCard;
        content.add(validationCard);
        updateProgressiveVisibility();

        status.setFont(FontManager.getRunescapeSmallFont());
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

    private JLabel sectionLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeBoldFont().deriveFont(14f));
        label.setForeground(OSRS_GOLD);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        return label;
    }

    private JLabel fieldLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeFont());
        label.setForeground(OSRS_CREAM);
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
                b.setFont(FontManager.getRunescapeSmallFont());
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

    private void updateSlayerUiState()
    {
        boolean selected = slayerTargetMode;
        slayerTask.setSelected(selected);
        slayerTask.setText("Load Slayer Task");
        slayerTask.setForeground(selected ? OSRS_GREEN : OSRS_CREAM);
        slayerTask.setOpaque(false);
        slayerTask.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        monsterBox.setEnabled(true);
        // Keep the single target field editable at all times. Slayer mode fills it
        // automatically and suppresses filtering through slayerTargetMode, while
        // manual mode must remain typeable after the checkbox is cleared.
        if (monsterBox.getEditor().getEditorComponent() != null)
            monsterBox.getEditor().getEditorComponent().setEnabled(true);
        monsterBox.setEnabled(true);
        monsterBox.setToolTipText(selected
            ? "Eligible NPC variants for your current Slayer task."
            : "Choose a monster target.");
    }

    private ImageIcon attackStyleIcon(String label)
    {
        String name=label==null?"":label.toLowerCase();
        java.net.URL url=PersonalBisPanel.class.getResource("/attack-styles/"+name+".png");
        return url==null?null:new ImageIcon(url);
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

    private GridBagConstraints slotPosition(EquipmentSlot slot)
    {
        // Compact 4 x 3 equipment layout:
        // [Head] [Neck] [Ammo]   [Prayer]
        // [Body] [Cape] [Weapon] [Shield]
        // [Legs] [Feet] [Hands]  [Ring]
        int x = 0;
        int y = 0;
        switch (slot)
        {
            case HEAD: x = 0; y = 0; break;
            case NECK: x = 1; y = 0; break;
            case AMMO: x = 2; y = 0; break;
            case BODY: x = 0; y = 1; break;
            case CAPE: x = 1; y = 1; break;
            case WEAPON: x = 2; y = 1; break;
            case SHIELD: x = 3; y = 1; break;
            case LEGS: x = 0; y = 2; break;
            case FEET: x = 1; y = 2; break;
            case HANDS: x = 2; y = 2; break;
            case RING: x = 3; y = 2; break;
        }
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.insets = new Insets(1, 1, 1, 1);
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1;
        return g;
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
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,180));
        JLabel h=new JLabel(heading,SwingConstants.CENTER);
        h.setFont(FontManager.getRunescapeBoldFont().deriveFont(15f));
        h.setForeground(OSRS_GOLD); h.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel b=new JLabel("<html><div style='text-align:center;width:205px'>"+body+"</div></html>",SwingConstants.CENTER);
        b.setFont(FontManager.getRunescapeFont()); b.setForeground(OSRS_CREAM); b.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(20)); panel.add(h); panel.add(Box.createVerticalStrut(8)); panel.add(b); panel.add(Box.createVerticalStrut(20));
        return panel;
    }

    private void ensureStatePanels()
    {
        if(loggedOutPanel==null){
            loggedOutPanel=messagePanel("🔒  Log in to begin","Log in to RuneScape to search monsters, scan your bank and find your best gear.");
            noTargetPanel=messagePanel("Select a target","Search for a monster above to see attack styles, gear and analysis.");
            chooseStylePanel=messagePanel("Choose an attack style","Select Stab, Slash, Crush, Ranged or Magic to show your best gear and analysis.");
            content.add(loggedOutPanel); content.add(noTargetPanel); content.add(chooseStylePanel);
        }
    }

    private void updateProgressiveVisibility()
    {
        ensureStatePanels();
        boolean loggedIn=client.getGameState()==GameState.LOGGED_IN;
        boolean targetReady=loggedIn && targetExplicitlySelected && monsterBox.getSelectedItem() instanceof MonsterDefinition;
        boolean stylesReady=RecommendationReadiness.stylesVisible(targetReady);
        boolean resultsReady=RecommendationReadiness.resultsVisible(targetReady,bankChecked,styleExplicitlySelected);
        if(targetCard!=null)targetCard.setVisible(loggedIn);
        if(monsterCountLabel!=null){
            monsterCountLabel.setVisible(loggedIn);
            if(loggedIn)monsterCountLabel.setText(monsterDatabase.getAll().size()+" monsters loaded");
        }
        if(loggedOutPanel!=null)loggedOutPanel.setVisible(!loggedIn);
        if(noTargetPanel!=null)noTargetPanel.setVisible(loggedIn&&!targetReady);
        if(chooseStylePanel!=null)chooseStylePanel.setVisible(stylesReady&&!styleExplicitlySelected);
        if (attackStyleHeading != null) attackStyleHeading.setVisible(stylesReady);
        if (stylesPanel != null) stylesPanel.setVisible(stylesReady);
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
        selectedStyle = style;
        styleExplicitlySelected = true;
        loadoutHeading.setText("Your best loadout ("+displayAttackStyle(style)+")");
        updateStyleHighlight();

        // Style selection alone must not imply that the bank has been loaded.
        // Owned-gear recommendations become available only after refreshBankItems().
        SwingUtilities.invokeLater(this::updateProgressiveVisibility);
        if (bankChecked)
        {
            refreshRecommendationsAsync();
            if (bankFilter.isActive()) bankFilter.refreshLayout();
        }
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
            button.setFont((selected||best) ? FontManager.getRunescapeBoldFont() : FontManager.getRunescapeSmallFont());
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
            refreshRecommendationsAsync();
        });
    }

    public void updatePlayerStats()
    {
        invalidateOptimizerCache();
        account.refresh();
        refreshRecommendationsAsync();
    }

    private void updateAccountText()
    {
        accountLevels.setText("Atk " + account.real(Skill.ATTACK) + "  Str " + account.real(Skill.STRENGTH) +
            "  Def " + account.real(Skill.DEFENCE) + "  Rng " + account.real(Skill.RANGED) + "  Mag " + account.real(Skill.MAGIC) +
            "  Pray " + account.real(Skill.PRAYER));
    }

    public void refreshBankItems()
    {
        invalidateOptimizerCache();
        // ItemContainerChanged runs on RuneLite's client thread. Keep this path to
        // a lightweight immutable bank snapshot so opening the OSRS bank is not
        // blocked by BiS optimisation or Swing layout work.
        cachedBank = bankScanner.scan();
        bankChecked = true;

        SwingUtilities.invokeLater(() ->
        {
            updateProgressiveVisibility();

            // Let the bank-opening event finish before Personal BiS performs its
            // expensive recommendation pass.
            SwingUtilities.invokeLater(() ->
            {
                if (bankFilter.isActive())
                {
                    bankFilter.refreshLayout();
                }
                refreshRecommendationsAsync();
            });
        });
    }

    public void updateSlayerTask()
    {
        String task = slayerService.getTask();
        boolean changed = task == null ? lastSlayerTask != null : !task.equals(lastSlayerTask);
        lastSlayerTask = task;
        if (!slayerTargetMode || task == null || task.trim().isEmpty())
        {
            return;
        }

        List<MonsterDefinition> variants = monsterDatabase.findTaskVariants(task);
        variants.removeIf(m -> !SlayerTargetRequirements.allowed(m, account.real(Skill.SLAYER)));
        SwingUtilities.invokeLater(() ->
        {
            MonsterDefinition previous = (MonsterDefinition) monsterBox.getSelectedItem();
            DefaultComboBoxModel<MonsterDefinition> model = new DefaultComboBoxModel<>();
            for (MonsterDefinition variant : variants)
            {
                model.addElement(variant);
            }
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
                    styleExplicitlySelected = false;
                    clearStyleSelection();
                    updateProgressiveVisibility();
                }
            }
            else
            {
                monsterInfo.setText("Slayer: " + task + " • no monster variants found");
            }
            updateSlayerUiState();
        });
    }

    public void refreshSlayerTaskIfChanged()
    {
        String task = slayerService.getTask();
        if (task == null ? lastSlayerTask != null : !task.equals(lastSlayerTask))
        {
            updateSlayerTask();
            if (slayerTargetMode) refreshRecommendationsAsync();
        }
    }

    private void refreshRecommendationsAsync()
    {
        final long generation=calculationGeneration.incrementAndGet();
        clientThread.invokeLater(() -> captureAndCalculate(generation));
    }

    private void captureAndCalculate(long generation)
    {
        if (client.getGameState() != GameState.LOGGED_IN) return;
        if (!targetExplicitlySelected || !bankChecked || !styleExplicitlySelected)
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
        final List<BankItem> bankSnapshot=new ArrayList<>(cachedBank);
        final Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> rankedSnapshot=new EnumMap<>(AttackStyle.class);
        for(AttackStyle style:AttackStyle.values())
            rankedSnapshot.put(style,intelligence.rank(bankSnapshot,style,slayerMode));
        final AttackStyle requestedStyle=selectedStyle;
        final boolean requestedSlayerMode=slayerMode;

        // alpha50.4.26.2: build the DEV-only synthetic ammo candidates alongside
        // the normal intelligence snapshots, before the optimiser/render callbacks.
        // This keeps the validation independent of calculated alternatives and avoids
        // doing ItemManager-backed intelligence work from the Swing render callback.
        final List<EquipmentCandidate> syntheticBoltValidationAmmo = new ArrayList<>();
        if (requestedStyle == AttackStyle.RANGED)
        {
            List<BankItem> validationBank = new ArrayList<>(bankSnapshot);
            int[] enchantedBoltIds = {9236,9237,9238,9239,9240,9241,9242,9243,9244,9245};
            String[] enchantedBoltNames = {"Opal bolts (e)","Jade bolts (e)","Pearl bolts (e)","Topaz bolts (e)","Sapphire bolts (e)","Emerald bolts (e)","Ruby bolts (e)","Diamond bolts (e)","Dragonstone bolts (e)","Onyx bolts (e)"};
            Set<Integer> presentBoltIds = new HashSet<>();
            for (BankItem bi : validationBank) presentBoltIds.add(bi.getItemId());
            for (int i=0;i<enchantedBoltIds.length;i++)
                if (!presentBoltIds.contains(enchantedBoltIds[i]))
                    validationBank.add(new BankItem(enchantedBoltIds[i],1,enchantedBoltNames[i]));
            List<EquipmentCandidate> ammoChoices = intelligence.rank(validationBank, AttackStyle.RANGED, requestedSlayerMode).get(EquipmentSlot.AMMO);
            if (ammoChoices != null)
                for (EquipmentCandidate ammo : ammoChoices)
                    if (!ammo.getRequirementResult().isBlocked() && EnchantedBoltEffects.enchanted(ammo))
                        syntheticBoltValidationAmmo.add(ammo);
            syntheticBoltValidationAmmo.sort(Comparator.comparing(a -> a.getItem().getName()));
        }

        // alpha50.4.34: DEV-only synthetic melee weapon suite. Armour comes only from
        // the real bank snapshot; only the listed weapons are injected virtually.
        final Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> syntheticMeleeRanked = new EnumMap<>(AttackStyle.class);
        if (requestedStyle == AttackStyle.MELEE_STAB || requestedStyle == AttackStyle.MELEE_SLASH || requestedStyle == AttackStyle.MELEE_CRUSH)
        {
            List<BankItem> validationBank = new ArrayList<>(bankSnapshot);
            int[] weaponIds = {22978,29589,19675,25979,27291,25981,26219,22324,11902,20727,4587,4151,13263,22325,28338,24417,28997,4747,29084,29889,6523};
            String[] weaponNames = {"Dragon hunter lance","Emberlight","Arclight","Keris partisan","Keris partisan of the sun","Keris partisan of breaching","Osmumten's fang","Ghrazi rapier","Leaf-bladed sword","Leaf-bladed battleaxe","Dragon scimitar","Abyssal whip","Abyssal bludgeon","Scythe of Vitur","Soulreaper axe","Inquisitor's mace","Dual macuahuitl","Torag's hammers","Sulphur blades","Glacial temotli","Toktz-xil-ak","Colossal blade","Barronite mace","Granite hammer"};
            Set<Integer> present = new HashSet<>();
            for (BankItem bi : validationBank) present.add(bi.getItemId());
            for (int i=0;i<weaponIds.length;i++) if (!present.contains(weaponIds[i]))
                validationBank.add(new BankItem(weaponIds[i],1,weaponNames[i]));
            // alpha50.4.38: DEV-only Slayer headgear reference. Ownership remains
            // untouched; this exists only so the fixed Wiki harness can compare
            // identical off-task/on-task melee calculations.
            if (!present.contains(11865)) validationBank.add(new BankItem(11865,1,"Slayer helmet (i)"));
            // alpha50.4.42: current Inquisitor armour is injected only into the DEV
            // parity harness so every 2026 per-piece combination can be reproduced
            // without requiring ownership. Production optimisation stays bank-aware.
            if (!present.contains(24419)) validationBank.add(new BankItem(24419,1,"Inquisitor's great helm"));
            if (!present.contains(24420)) validationBank.add(new BankItem(24420,1,"Inquisitor's hauberk"));
            if (!present.contains(24421)) validationBank.add(new BankItem(24421,1,"Inquisitor's plateskirt"));
            for (AttackStyle ms : new AttackStyle[]{AttackStyle.MELEE_STAB,AttackStyle.MELEE_SLASH,AttackStyle.MELEE_CRUSH})
                syntheticMeleeRanked.put(ms, intelligence.rank(validationBank, ms, requestedSlayerMode));
        }

        final String cacheKey=optimizerCacheKey(monster,bankSnapshot,requestedSlayerMode);
        optimizerExecutor.execute(() -> calculateRecommendations(generation,bankSnapshot,rankedSnapshot,monster,requestedStyle,requestedSlayerMode,cacheKey,syntheticBoltValidationAmmo,syntheticMeleeRanked));
    }

    private void calculateRecommendations(long generation,List<BankItem> bankSnapshot,
        Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> rankedSnapshot,
        MonsterDefinition monster,AttackStyle requestedStyle,boolean requestedSlayerMode,String cacheKey,
        List<EquipmentCandidate> syntheticBoltValidationAmmo,
        Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> syntheticMeleeRanked)
    {
        if(generation!=calculationGeneration.get())return;
        final long calculationStartedNanos = System.nanoTime();
        Map<AttackStyle, List<Integer>> styleRecommendations = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, Double> styleScores = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, OptimizedLoadout> meleeLoads = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, MeleeOptimizationReport> meleeReports = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, OptimizedRangedLoadout> rangedLoads = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, RangedOptimizationReport> rangedReports = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, OptimizedMagicLoadout> magicLoads = new EnumMap<>(AttackStyle.class);
        Map<AttackStyle, MagicOptimizationReport> magicReports = new EnumMap<>(AttackStyle.class);
        Set<Integer> allRecommended = new HashSet<>();
        Map<EquipmentSlot, List<EquipmentCandidate>> selectedRanked = null;
        // alpha50.4.13 profiling: keep mechanics untouched and expose where the
        // background optimiser spends its time. Values are deliberately local
        // to this calculation so concurrent/stale generations cannot mix data.
        final Map<AttackStyle, Long> optimizerStageMillis = new EnumMap<>(AttackStyle.class);
        String rangedProfileSummary = "";
        final boolean optimizerCacheHit = cacheKey.equals(optimizerCacheKey);
        boolean selectedStyleCacheHit = false;
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
            // Calculate the selected style immediately. Other styles are evaluated
            // when selected, avoiding a multi-second all-style sweep on first load.
            if (style != requestedStyle) continue;
            final long styleStartedNanos = System.nanoTime();
            final Map<EquipmentSlot,List<EquipmentCandidate>> ranked = rankedSnapshot.get(style);
            if (style == requestedStyle) selectedRanked = ranked;

            double actualDps = 0.0;
            if (style == AttackStyle.MELEE_STAB || style == AttackStyle.MELEE_SLASH || style == AttackStyle.MELEE_CRUSH)
            {
                MeleeOptimizationReport report = optimizerCacheHit ? cachedMeleeReports.get(style) : null;
                selectedStyleCacheHit = report != null;
                if (report == null)
                {
                    report = meleeOptimizer.optimizeWithReportRanked(ranked, style, monster, requestedSlayerMode);
                    cachedMeleeReports.put(style, report);
                }
                meleeReports.put(style, report);
                OptimizedLoadout load = report == null ? null : report.getBest();
                meleeLoads.put(style, load);
                if (load != null && load.getResult() != null) actualDps = load.getResult().getDps();
                List<Integer> ids = load == null ? new ArrayList<>() : load.itemIds();
                styleRecommendations.put(style, ids); allRecommended.addAll(ids);
            }
            else if (style == AttackStyle.RANGED)
            {
                RangedOptimizationReport report = optimizerCacheHit ? cachedRangedReports.get(style) : null;
                selectedStyleCacheHit = report != null;
                if (report == null)
                {
                    report = rangedOptimizer.optimizeWithReportRanked(ranked, monster, requestedSlayerMode);
                    rangedProfileSummary = rangedOptimizer.getLastProfileSummary();
                    cachedRangedReports.put(style, report);
                }
                rangedReports.put(style, report);
                OptimizedRangedLoadout load = report == null ? null : report.getBest();
                rangedLoads.put(style, load);
                if (load != null && load.getResult() != null) actualDps = load.getResult().getDps();
                List<Integer> ids = load == null ? new ArrayList<>() : load.itemIds();
                styleRecommendations.put(style, ids); allRecommended.addAll(ids);
            }
            else if (style == AttackStyle.MAGIC)
            {
                MagicOptimizationReport mr = optimizerCacheHit ? cachedMagicReports.get(style) : null;
                selectedStyleCacheHit = mr != null;
                if (mr == null)
                {
                    mr = magicOptimizer.optimizeWithReportRanked(bankSnapshot, ranked, monster, requestedSlayerMode);
                    cachedMagicReports.put(style, mr);
                }
                magicReports.put(style, mr);
                OptimizedMagicLoadout load = mr == null ? null : mr.getBest();
                magicLoads.put(style, load);
                if (load != null && load.getResult() != null) actualDps = load.getResult().getDps();
                List<Integer> ids = load == null ? new ArrayList<>() : load.itemIds();
                styleRecommendations.put(style, ids); allRecommended.addAll(ids);
            }
            styleScores.put(style, actualDps);
            optimizerStageMillis.put(style, (System.nanoTime()-styleStartedNanos)/1_000_000L);
        }
        final long styleSearchFinishedNanos = System.nanoTime();
        if(generation!=calculationGeneration.get())return;
        optimizerCacheKey = cacheKey;
        // A selected-style result is rendered immediately for sub-tick responsiveness.
        // Only label a style "best" once exact reports exist for all five styles;
        // otherwise the sole selected style would inevitably (and incorrectly) win.
        final AttackStyle resultBestStyle=cachedBestStyleIfComplete();

        // Bank mode follows the explicit sidebar choice: show only that style plus Recommended.
        List<AttackStyle> bankStyles=new ArrayList<>();
        bankStyles.add(requestedStyle);
        Map<AttackStyle,List<Integer>> selectedBankRecommendations=new EnumMap<>(AttackStyle.class);
        List<Integer> selectedIds=styleRecommendations.get(requestedStyle);
        if(selectedIds==null)selectedIds=new ArrayList<>();
        selectedBankRecommendations.put(requestedStyle,selectedIds);
        Set<Integer> selectedRecommendedIds=new HashSet<>(selectedIds);
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
        final MeleeOptimizationReport selectedReport = meleeReports.get(requestedStyle);
        final OptimizedRangedLoadout selectedRangedLoad = rangedLoads.get(requestedStyle);
        final RangedOptimizationReport selectedRangedReport = rangedReports.get(requestedStyle);
        final RangedCombatResult uiRanged = selectedRangedLoad == null ? selectedRanged : selectedRangedLoad.getResult();
        final OptimizedMagicLoadout selectedMagicLoad = magicLoads.get(requestedStyle);
        final MagicCombatResult uiMagic = selectedMagicLoad == null ? null : selectedMagicLoad.getResult();
        final MagicOptimizationReport selectedMagicReport = magicReports.get(requestedStyle);

        final AttackStyle uiBestStyle=resultBestStyle;
        final String uiRangedProfileSummary=rangedProfileSummary;
        final boolean uiSelectedStyleCacheHit=selectedStyleCacheHit;
        final long postProcessMillis=(System.nanoTime()-styleSearchFinishedNanos)/1_000_000L;
        final long calculationMillis=(System.nanoTime()-calculationStartedNanos)/1_000_000L;
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
                if (best==null)
                {
                    gearImageGeneration[i]++;
                    label.setText("—");
                    label.setIcon(null);
                    label.setToolTipText(slot.getDisplayName()+": no item selected");
                }
                else
                {
                    label.setText("");
                    label.setIcon(null);
                    final int slotIndex = i;
                    final int requestedItemId = best.getItem().getItemId();
                    final long requestGeneration = ++gearImageGeneration[slotIndex];
                    // AsyncBufferedImage.addTo accepts Swing components directly.
                    // Attach it to a temporary JLabel so its async repaint/update lifecycle
                    // is used, while guarding the real gear slot against stale refreshes.
                    final JLabel imageLoader = new JLabel();
                    itemManager.getImage(requestedItemId, 1, false).addTo(imageLoader);
                    SwingUtilities.invokeLater(() -> {
                        if (gearImageGeneration[slotIndex] == requestGeneration)
                        {
                            label.setIcon(imageLoader.getIcon());
                            // If RuneLite is still loading, attach the real label too; its
                            // generation guard is rechecked on the next panel refresh.
                            if (label.getIcon() == null)
                            {
                                itemManager.getImage(requestedItemId, 1, false).addTo(label);
                            }
                        }
                    });
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
                summary.append(String.format("Max Hit: %d%n", selectedCombat.getMaxHit()));
                summary.append(String.format("Accuracy: %.2f%%%n", selectedCombat.getAccuracy() * 100.0));
                summary.append(String.format("Style: %s%n", selectedCombat.getStance()));
                summary.append(String.format("Prayer Bonus: %+d", prayerBonus));
            }
            else if (uiRanged != null)
            {
                summary.append(String.format("DPS: %.4f%n", uiRanged.getDps()));
                summary.append(String.format("Max Hit: %d%n", uiRanged.getMaxHit()));
                summary.append(String.format("Accuracy: %.2f%%%n", uiRanged.getAccuracy() * 100.0));
                summary.append(String.format("Style: %s%n", uiRanged.getStance()));
                summary.append(String.format("Prayer Bonus: %+d", prayerBonus));
            }
            else if (uiMagic != null)
            {
                summary.append(String.format("DPS: %.4f%n", uiMagic.getDps()));
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

            StringBuilder validation = new StringBuilder();
            validation.append("Calculation: ").append(calculationMillis).append(" ms").append(System.lineSeparator());
            validation.append("Optimizer cache: ").append(uiSelectedStyleCacheHit ? "HIT" : "MISS").append(System.lineSeparator());
            Long selectedMs = optimizerStageMillis.get(requestedStyle);
            if (selectedMs != null)
                validation.append(displayAttackStyle(requestedStyle)).append(": ").append(selectedMs).append(" ms").append(System.lineSeparator());
            validation.append("Result: ").append((selectedCombat != null || uiRanged != null || uiMagic != null) ? "PASS" : "NO LOADOUT");
            if (!uiRangedProfileSummary.isEmpty())
                validation.append(System.lineSeparator()).append(uiRangedProfileSummary);
            if (LeafyTargetRules.restricted(monster))
            {
                EquipmentCandidate w=selectedGear.get(EquipmentSlot.WEAPON);
                EquipmentCandidate a=selectedGear.get(EquipmentSlot.AMMO);
                boolean allowed; String via;
                if (requestedStyle==AttackStyle.MELEE_STAB || requestedStyle==AttackStyle.MELEE_SLASH || requestedStyle==AttackStyle.MELEE_CRUSH)
                { allowed=LeafyTargetRules.meleeCanDamage(monster,w); via=w==null?"no weapon":w.getItem().getName(); }
                else if (requestedStyle==AttackStyle.RANGED)
                { allowed=LeafyTargetRules.rangedCanDamage(monster,a); via=a==null?"no ammo":a.getItem().getName(); }
                else
                { allowed=uiMagic!=null && "Magic Dart".equalsIgnoreCase(uiMagic.getSpell()); via=uiMagic==null?"no spell":uiMagic.getSpell(); }
                validation.append(System.lineSeparator()).append("Leafy restriction: ").append(allowed?"ALLOWED":"BLOCKED").append(" via ").append(via);
            }

            // alpha50.4.28: compact melee parity trace.  This exposes every integer
            // checkpoint needed to compare PBIS with the Wiki calculator without
            // changing production combat behaviour.
            if ((requestedStyle == AttackStyle.MELEE_SLASH || requestedStyle == AttackStyle.MELEE_STAB || requestedStyle == AttackStyle.MELEE_CRUSH)
                && selectedCombat != null)
            {
                validation.append(System.lineSeparator()).append(System.lineSeparator());
                validation.append("Melee Parity Audit").append(System.lineSeparator());
                validation.append("Target: ").append(monster.getName());
                if (!monster.getVersion().isEmpty()) validation.append(" [").append(monster.getVersion()).append("]");
                validation.append("  id=").append(monster.getId()).append(System.lineSeparator());
                validation.append("Target def: lvl ").append(monster.getDefenceLevel()).append(" / style +").append(monster.defenceFor(requestedStyle)).append(System.lineSeparator());
                EquipmentCandidate auditBody=selectedGear.get(EquipmentSlot.BODY);
                EquipmentCandidate auditLegs=selectedGear.get(EquipmentSlot.LEGS);
                validation.append("Body: ").append(auditBody==null?"—":auditBody.getItem().getName()+" [atk "+auditBody.getAttackBonus()+", str "+auditBody.getStrengthBonus()+"]").append(System.lineSeparator());
                validation.append("Legs: ").append(auditLegs==null?"—":auditLegs.getItem().getName()+" [atk "+auditLegs.getAttackBonus()+", str "+auditLegs.getStrengthBonus()+"]").append(System.lineSeparator());
                validation.append("Equipment:").append(System.lineSeparator());
                for (EquipmentSlot slot : EquipmentSlot.values())
                {
                    EquipmentCandidate equipped = selectedGear.get(slot);
                    if (equipped != null) validation.append("  ").append(slot.getDisplayName()).append(": ").append(equipped.getItem().getName()).append(" id=").append(equipped.getItem().getItemId()).append(System.lineSeparator());
                }
                validation.append("Effective Attack: ").append(selectedCombat.getEffectiveAttack()).append(System.lineSeparator());
                validation.append("Effective Strength: ").append(selectedCombat.getEffectiveStrength()).append(System.lineSeparator());
                validation.append("Attack bonus: ").append(selectedCombat.getAttackBonus()).append(System.lineSeparator());
                validation.append("Strength bonus: ").append(selectedCombat.getStrengthBonus()).append(System.lineSeparator());
                validation.append("Pre-weapon attack roll: ").append(selectedCombat.getPreWeaponAttackRoll()).append(System.lineSeparator());
                validation.append("Final attack roll: ").append(selectedCombat.getAttackRoll()).append(System.lineSeparator());
                validation.append("NPC defence roll: ").append(selectedCombat.getDefenceRoll()).append(System.lineSeparator());
                validation.append("Pre-weapon max: ").append(selectedCombat.getPreWeaponMaxHit()).append(System.lineSeparator());
                validation.append("Final max: ").append(selectedCombat.getMaxHit()).append(System.lineSeparator());
                validation.append("Prayer: ").append(selectedCombat.getPrayer()).append(System.lineSeparator());
                validation.append("Attack speed: ").append(selectedCombat.getAttackSpeed()).append(" ticks");
                // alpha50.4.35: the temporary BODY/LEGS/KERIS/TOP optimizer trace
                // served its purpose during parity work. Keep the compact internal
                // parity/synthetic report, but do not render it in the side panel.
            }

            // alpha50.4.50.13: Wiki-style parity checkpoints for every combat family.
            // Keep these internal checkpoints so tests and future debugging can locate
            // the first divergent integer stage without rendering DEV data in the UI.
            if (requestedStyle == AttackStyle.RANGED && uiRanged != null)
            {
                validation.append(System.lineSeparator()).append(System.lineSeparator());
                validation.append("Ranged Parity Audit").append(System.lineSeparator());
                validation.append("Effective accuracy level: ").append(uiRanged.getEffectiveRanged()).append(System.lineSeparator());
                validation.append("Effective damage level: ").append(uiRanged.getEffectiveDamage()).append(System.lineSeparator());
                validation.append("Equipment ranged attack: ").append(uiRanged.getRangedAttack()).append(System.lineSeparator());
                validation.append("Equipment ranged strength: ").append(uiRanged.getRangedStrength()).append(System.lineSeparator());
                validation.append("Final attack roll: ").append(uiRanged.getAttackRoll()).append(System.lineSeparator());
                validation.append("NPC defence roll: ").append(uiRanged.getDefenceRoll()).append(System.lineSeparator());
                validation.append(String.format("Accuracy: %.6f%%%n", uiRanged.getAccuracy()*100.0));
                validation.append("Final max: ").append(uiRanged.getMaxHit()).append(System.lineSeparator());
                validation.append("Prayer: ").append(uiRanged.getPrayer()).append(System.lineSeparator());
                validation.append("Attack speed: ").append(uiRanged.getAttackSpeed()).append(" ticks").append(System.lineSeparator());
                validation.append(String.format("Expected damage/attack: %.8f%n", uiRanged.getDps()*uiRanged.getAttackSpeed()*0.6));
                validation.append(String.format("Final DPS: %.8f", uiRanged.getDps()));
            }
            if (requestedStyle == AttackStyle.MAGIC && uiMagic != null)
            {
                validation.append(System.lineSeparator()).append(System.lineSeparator());
                validation.append("Magic Parity Audit").append(System.lineSeparator());
                validation.append("Effective Magic level: ").append(uiMagic.getEffectiveMagic()).append(System.lineSeparator());
                validation.append("Equipment Magic attack: ").append(uiMagic.getMagicAttack()).append(System.lineSeparator());
                validation.append(String.format("Equipment Magic damage: %.6f%n", uiMagic.getMagicDamageBonus()));
                validation.append("Final attack roll: ").append(uiMagic.getAttackRoll()).append(System.lineSeparator());
                validation.append("NPC defence roll: ").append(uiMagic.getDefenceRoll()).append(System.lineSeparator());
                validation.append(String.format("Accuracy: %.6f%%%n", uiMagic.getAccuracy()*100.0));
                validation.append("Pre-NPC-transform max: ").append(uiMagic.getPreTransformMax()).append(System.lineSeparator());
                validation.append("Final max: ").append(uiMagic.getMaxHit()).append(System.lineSeparator());
                validation.append("Prayer: ").append(uiMagic.getPrayer()).append(System.lineSeparator());
                validation.append("Attack speed: ").append(uiMagic.getAttackSpeed()).append(" ticks").append(System.lineSeparator());
                validation.append(String.format("Expected damage/attack: %.8f%n", uiMagic.getDps()*uiMagic.getAttackSpeed()*0.6));
                validation.append(String.format("Final DPS: %.8f", uiMagic.getDps()));
            }

            // alpha50.4.34: fixed-armour synthetic melee suite. The armour is a single
            // reproducible Wiki setup from the user's real bank; ownership is ignored
            // only for the weapon under test. Each weapon is allowed to use its best
            // legal melee style/stance while the armour remains unchanged.
            if ((requestedStyle == AttackStyle.MELEE_STAB || requestedStyle == AttackStyle.MELEE_SLASH || requestedStyle == AttackStyle.MELEE_CRUSH)
                && syntheticMeleeRanked != null && !syntheticMeleeRanked.isEmpty())
            {
                String[] fixedNames = {"Neitiznot faceguard","Fire cape","Amulet of fury","Bandos chestplate","Dragon defender","Bandos tassets","Barrows gloves","Dragon boots","Berserker ring (i)","Honourable blessing"};
                EquipmentSlot[] fixedSlots = {EquipmentSlot.HEAD,EquipmentSlot.CAPE,EquipmentSlot.NECK,EquipmentSlot.BODY,EquipmentSlot.SHIELD,EquipmentSlot.LEGS,EquipmentSlot.HANDS,EquipmentSlot.FEET,EquipmentSlot.RING,EquipmentSlot.AMMO};
                String[] weapons = {"Dragon hunter lance","Emberlight","Arclight","Keris partisan","Keris partisan of the sun","Keris partisan of breaching","Osmumten's fang","Ghrazi rapier","Leaf-bladed sword","Leaf-bladed battleaxe","Dragon scimitar","Abyssal whip","Abyssal bludgeon","Scythe of Vitur","Soulreaper axe","Inquisitor's mace","Dual macuahuitl","Torag's hammers","Sulphur blades","Glacial temotli","Toktz-xil-ak","Colossal blade","Barronite mace","Granite hammer"};
                validation.append(System.lineSeparator()).append(System.lineSeparator()).append("Synthetic Melee Validation").append(System.lineSeparator());
                validation.append("Fixed gear: Neitiznot faceguard | Fire cape | Amulet of fury | Bandos chestplate | Dragon defender | Bandos tassets | Barrows gloves | Dragon boots | Berserker ring (i) | Honourable blessing").append(System.lineSeparator());
                validation.append("Prayer: best available (Piety when unlocked) | synthetic weapons only — ownership ignored").append(System.lineSeparator());
                validation.append("Target: ").append(monster.getName());
                if (!monster.getVersion().isEmpty()) validation.append(" [").append(monster.getVersion()).append("]");
                validation.append(" id=").append(monster.getId()).append(System.lineSeparator());
                for (String wn : weapons)
                {
                    CombatResult bestSynthetic = null; AttackStyle bestSyntheticStyle = null; EquipmentCandidate bestWeapon = null; Map<EquipmentSlot,EquipmentCandidate> bestGear = null;
                    for (AttackStyle ms : new AttackStyle[]{AttackStyle.MELEE_STAB,AttackStyle.MELEE_SLASH,AttackStyle.MELEE_CRUSH})
                    {
                        Map<EquipmentSlot,List<EquipmentCandidate>> rr = syntheticMeleeRanked.get(ms);
                        if (rr == null) continue;
                        EquipmentCandidate wc = wn.equalsIgnoreCase("Scythe of Vitur")
                            ? candidateById(rr.get(EquipmentSlot.WEAPON),22325)
                            : namedCandidate(rr.get(EquipmentSlot.WEAPON), wn);
                        if (wc == null || wc.getRequirementResult().isBlocked() || !MeleeAttackStyleResolver.supports(wc,ms)) continue;
                        Map<EquipmentSlot,EquipmentCandidate> fg = new EnumMap<>(EquipmentSlot.class); fg.put(EquipmentSlot.WEAPON,wc);
                        boolean complete=true;
                        for(int fi=0;fi<fixedNames.length;fi++)
                        {
                            if(wc.isTwoHanded() && fixedSlots[fi]==EquipmentSlot.SHIELD) continue;
                            EquipmentCandidate fc=namedCandidate(rr.get(fixedSlots[fi]),fixedNames[fi]);
                            if(fc==null||fc.getRequirementResult().isBlocked()){complete=false;break;}
                            fg.put(fixedSlots[fi],fc);
                        }
                        if(!complete) continue;
                        CombatResult cr=meleeOptimizer.evaluateFixed(fg,ms,monster,requestedSlayerMode);
                        if(cr!=null && (bestSynthetic==null || cr.getDps()>bestSynthetic.getDps())){bestSynthetic=cr;bestSyntheticStyle=ms;bestWeapon=wc;bestGear=fg;}
                    }
                    if(bestSynthetic==null){validation.append(wn).append(" | unavailable with fixed gear").append(System.lineSeparator());continue;}
                    double ev=bestSynthetic.getDps()*bestSynthetic.getAttackSpeed()*0.6;
                    int procMax=(MeleeWeaponEffects.isKeris(bestWeapon) && (monster.hasAttribute("kalphite")||monster.hasAttribute("scabarite"))) ? MeleeWeaponEffects.kerisProcMax(bestSynthetic.getMaxHit()) : bestSynthetic.getMaxHit();
                    validation.append(String.format(java.util.Locale.ROOT,"%s | %s/%s | roll %d vs %d | %.2f%% | base %d -> max %d",wn,bestSyntheticStyle,bestSynthetic.getStance(),bestSynthetic.getAttackRoll(),bestSynthetic.getDefenceRoll(),bestSynthetic.getAccuracy()*100.0,bestSynthetic.getPreWeaponMaxHit(),bestSynthetic.getMaxHit()));
                    if(bestWeapon.isTwoHanded()) validation.append(" | 2H — shield omitted");
                    if(procMax!=bestSynthetic.getMaxHit()) validation.append(" -> proc ").append(procMax);
                    if(MeleeWeaponEffects.isFang(bestWeapon))
                    {
                        int fangTrueMax=bestSynthetic.getPreWeaponMaxHit();
                        validation.append(" | fang range ").append(MeleeWeaponEffects.fangMinHit(fangTrueMax)).append("-").append(MeleeWeaponEffects.fangDisplayedMaxHit(fangTrueMax));
                        validation.append(String.format(java.util.Locale.ROOT," avg %.5f",MeleeWeaponEffects.fangAverageHit(fangTrueMax)));
                    }
                    if(MeleeWeaponEffects.isSplitTwoHit(bestWeapon))
                    {
                        int combined=bestSynthetic.getMaxHit();
                        int h1=MeleeWeaponEffects.splitFirstMax(combined), h2=MeleeWeaponEffects.splitSecondMax(combined);
                        validation.append(" | split ").append(h1).append("+").append(h2);
                        if(MeleeWeaponEffects.isDualMacuahuitl(bestWeapon))
                            validation.append(String.format(java.util.Locale.ROOT," sequential P/P^2 expected %.5f",MeleeWeaponEffects.dualMacuahuitlExpectedDamage(combined,bestSynthetic.getAccuracy())));
                        else
                            validation.append(String.format(java.util.Locale.ROOT," independent successEV %.5f",MeleeWeaponEffects.independentTwoHitSuccessfulAverage(combined)));
                    }
                    if(MeleeWeaponEffects.isScythe(bestWeapon))
                    {
                        int scytheBase=bestSynthetic.getPreWeaponMaxHit();
                        int scytheHits=MeleeWeaponEffects.scytheHitCount(bestWeapon,monster);
                        validation.append(" | target size ").append(monster.getSize()).append("x").append(monster.getSize());
                        validation.append(" | scythe hits ").append(scytheHits).append(" caps ").append(scytheBase);
                        if(scytheHits>=2) validation.append("/").append(MeleeWeaponEffects.scytheSecondMax(scytheBase));
                        if(scytheHits>=3) validation.append("/").append(MeleeWeaponEffects.scytheThirdMax(scytheBase));
                        validation.append(" totalMax ").append(MeleeWeaponEffects.scytheCombinedMax(scytheBase,scytheHits));
                        validation.append(String.format(java.util.Locale.ROOT," successEV %.5f",MeleeWeaponEffects.scytheAverageHit(scytheBase,scytheHits)));
                    }
                    validation.append(String.format(java.util.Locale.ROOT," | EV %.5f | %dt | DPS %.5f",ev,bestSynthetic.getAttackSpeed(),bestSynthetic.getDps()));
                    double am=MeleeWeaponEffects.accuracyMultiplier(bestWeapon,monster,bestSyntheticStyle), dm=MeleeWeaponEffects.damageMultiplier(bestWeapon,monster,bestSyntheticStyle);
                    if(am!=1.0||dm!=1.0||MeleeWeaponEffects.isFang(bestWeapon)||MeleeWeaponEffects.isKeris(bestWeapon))
                        validation.append(String.format(java.util.Locale.ROOT," | passive acc x%.3f dmg x%.3f",am,dm));
                    validation.append(System.lineSeparator());
                }
                validation.append("Fixed armour; only weapon/style/stance changes. Use the same gear in Wiki and swap weapons.");

                // alpha50.4.38: synthetic Slayer melee parity harness. Keep every
                // equipment slot fixed and calculate the same Rapier loadout both
                // off-task and on-task. This isolates the 7/6 Black mask/Slayer helm
                // completed-roll modifiers from weapon-specific mechanics.
                Map<EquipmentSlot,List<EquipmentCandidate>> sr = syntheticMeleeRanked.get(AttackStyle.MELEE_STAB);
                if (sr != null)
                {
                    EquipmentCandidate sh = candidateById(sr.get(EquipmentSlot.HEAD),11865);
                    EquipmentCandidate sw = namedCandidate(sr.get(EquipmentSlot.WEAPON),"Ghrazi rapier");
                    if (sh != null && sw != null && !sh.getRequirementResult().isBlocked() && !sw.getRequirementResult().isBlocked())
                    {
                        String[] slayerNames = {"Fire cape","Amulet of fury","Bandos chestplate","Dragon defender","Bandos tassets","Barrows gloves","Dragon boots","Berserker ring (i)","Honourable blessing"};
                        EquipmentSlot[] slayerSlots = {EquipmentSlot.CAPE,EquipmentSlot.NECK,EquipmentSlot.BODY,EquipmentSlot.SHIELD,EquipmentSlot.LEGS,EquipmentSlot.HANDS,EquipmentSlot.FEET,EquipmentSlot.RING,EquipmentSlot.AMMO};
                        Map<EquipmentSlot,EquipmentCandidate> sg = new EnumMap<>(EquipmentSlot.class);
                        sg.put(EquipmentSlot.HEAD,sh); sg.put(EquipmentSlot.WEAPON,sw);
                        boolean sc=true;
                        for(int si=0;si<slayerNames.length;si++)
                        {
                            EquipmentCandidate x=namedCandidate(sr.get(slayerSlots[si]),slayerNames[si]);
                            if(x==null||x.getRequirementResult().isBlocked()){sc=false;break;}
                            sg.put(slayerSlots[si],x);
                        }
                        if(sc)
                        {
                            MonsterDefinition regressionTarget=withoutLeafyRestriction(monster);
                            CombatResult off=meleeOptimizer.evaluateFixed(sg,AttackStyle.MELEE_STAB,regressionTarget,false);
                            CombatResult on=meleeOptimizer.evaluateFixed(sg,AttackStyle.MELEE_STAB,regressionTarget,true);
                            if(off!=null&&on!=null)
                            {
                                validation.append(System.lineSeparator()).append(System.lineSeparator()).append("Synthetic Slayer Melee Validation").append(System.lineSeparator());
                                validation.append("Fixed gear: Slayer helmet (i) | Fire cape | Amulet of fury | Ghrazi rapier | Bandos chestplate | Dragon defender | Bandos tassets | Barrows gloves | Dragon boots | Berserker ring (i) | Honourable blessing").append(System.lineSeparator());
                                validation.append("Synthetic task state; ownership ignored only for Slayer helmet (i) / test weapon as needed.").append(System.lineSeparator());
                                validation.append(String.format(java.util.Locale.ROOT,"OFF TASK | %s | roll %d vs %d | %.2f%% | max %d | EV %.5f | DPS %.5f",off.getStance(),off.getAttackRoll(),off.getDefenceRoll(),off.getAccuracy()*100.0,off.getMaxHit(),off.getDps()*off.getAttackSpeed()*0.6,off.getDps())).append(System.lineSeparator());
                                validation.append(String.format(java.util.Locale.ROOT,"ON TASK  | %s | roll %d vs %d | %.2f%% | max %d | EV %.5f | DPS %.5f",on.getStance(),on.getAttackRoll(),on.getDefenceRoll(),on.getAccuracy()*100.0,on.getMaxHit(),on.getDps()*on.getAttackSpeed()*0.6,on.getDps())).append(System.lineSeparator());
                                validation.append("Expected Slayer stages: completed attack roll x7/6 and ordinary max hit x7/6; Salve takes precedence and does not stack.");
                            }
                        }
                    }
                }

                // alpha50.4.39: Soulreaper axe stack-ramp parity harness.  The
                // production optimiser is deliberately NOT changed yet: first expose
                // 0..5 stack max hits/DPS against the Wiki calculator.  Current 2026
                // mechanics add +6 percentage points of Strength level per stack,
                // additive with prayer; the stack generated by a swing applies only
                // to the following swing.  The 2026 axe has +125 Strength and no
                // longer drains HP while building stacks.
                Map<EquipmentSlot,List<EquipmentCandidate>> soulRanked = syntheticMeleeRanked.get(AttackStyle.MELEE_SLASH);
                if (soulRanked != null)
                {
                    EquipmentCandidate soul = candidateById(soulRanked.get(EquipmentSlot.WEAPON),28338);
                    if (soul != null && !soul.getRequirementResult().isBlocked())
                    {
                        String[] soulNames = {"Neitiznot faceguard","Fire cape","Amulet of fury","Bandos chestplate","Bandos tassets","Barrows gloves","Dragon boots","Berserker ring (i)","Honourable blessing"};
                        EquipmentSlot[] soulSlots = {EquipmentSlot.HEAD,EquipmentSlot.CAPE,EquipmentSlot.NECK,EquipmentSlot.BODY,EquipmentSlot.LEGS,EquipmentSlot.HANDS,EquipmentSlot.FEET,EquipmentSlot.RING,EquipmentSlot.AMMO};
                        Map<EquipmentSlot,EquipmentCandidate> soulGear = new EnumMap<>(EquipmentSlot.class);
                        soulGear.put(EquipmentSlot.WEAPON,soul); // 2H: intentionally no shield
                        boolean soulComplete=true;
                        for(int si=0;si<soulNames.length;si++)
                        {
                            EquipmentCandidate x=namedCandidate(soulRanked.get(soulSlots[si]),soulNames[si]);
                            if(x==null||x.getRequirementResult().isBlocked()){soulComplete=false;break;}
                            soulGear.put(soulSlots[si],x);
                        }
                        if(soulComplete)
                        {
                            CombatResult baseSoul=meleeOptimizer.evaluateFixed(soulGear,AttackStyle.MELEE_SLASH,withoutLeafyRestriction(monster),requestedSlayerMode);
                            if(baseSoul!=null)
                            {
                                validation.append(System.lineSeparator()).append(System.lineSeparator()).append("Synthetic Soulreaper Axe Validation").append(System.lineSeparator());
                                validation.append("Fixed gear: Neitiznot faceguard | Fire cape | Amulet of fury | Soulreaper axe | Bandos chestplate | Bandos tassets | Barrows gloves | Dragon boots | Berserker ring (i) | Honourable blessing | 2H — no shield").append(System.lineSeparator());
                                validation.append("Current 2026 auto-attacks: +125 axe Strength; 5 stacks; each stack adds 6% of boosted Strength, floored separately from prayer; no HP drain. Stack gained after a swing affects the next swing.").append(System.lineSeparator());
                                validation.append("Style/stance: Melee - Slash/").append(baseSoul.getStance()).append(" | Prayer: ").append(baseSoul.getPrayer()).append(" | roll ").append(baseSoul.getAttackRoll()).append(" vs ").append(baseSoul.getDefenceRoll()).append(String.format(java.util.Locale.ROOT," | accuracy %.2f%%",baseSoul.getAccuracy()*100.0)).append(System.lineSeparator());
                                for(int stacks=0;stacks<=5;stacks++)
                                {
                                    int eff=SoulreaperAxeEffects.effectiveStrength(Math.max(1,account.boosted(net.runelite.api.Skill.STRENGTH)),baseSoul.getPrayer().getStrength(),baseSoul.getStance().getStrengthBoost(),false,stacks);
                                    int mh=SoulreaperAxeEffects.maxHit(Math.max(1,account.boosted(net.runelite.api.Skill.STRENGTH)),baseSoul.getPrayer().getStrength(),baseSoul.getStance().getStrengthBoost(),false,stacks,baseSoul.getStrengthBonus());
                                    double successEv=MeleeWeaponEffects.successfulHitAverage(mh);
                                    double ev=baseSoul.getAccuracy()*successEv;
                                    double dps=ev/(baseSoul.getAttackSpeed()*0.6);
                                    validation.append(String.format(java.util.Locale.ROOT,"%d stack%s | effStr %d | max %d | successEV %.5f | EV %.5f | DPS %.5f",stacks,stacks==1?"":"s",eff,mh,successEv,ev,dps)).append(System.lineSeparator());
                                }
                                validation.append("Ramp sequence from zero stacks uses the 0/1/2/3/4-stack rows for the first five attacks; subsequent autos use 5 stacks. Wiki parity locked for the complete 0→5 stack sequence. Production optimizer now uses this fight-length-aware ramp from zero stacks; displayed max is the 5-stack potential max. Ramp-scored DPS: ").append(String.format(java.util.Locale.ROOT,"%.5f",baseSoul.getDps()));
                            }
                        }
                    }
                }
            }

            // alpha50.4.42: current Inquisitor parity harness. July 2026 removed
            // the old full-set/mace amplification: helm=.5%, body=1%, legs=1%,
            // and the mace now owns +102 crush/+96 strength as raw stats.
            if (requestedStyle == AttackStyle.MELEE_CRUSH && syntheticMeleeRanked != null)
            {
                Map<EquipmentSlot,List<EquipmentCandidate>> ir=syntheticMeleeRanked.get(AttackStyle.MELEE_CRUSH);
                if(ir!=null)
                {
                    EquipmentCandidate mace=namedCandidate(ir.get(EquipmentSlot.WEAPON),"Inquisitor's mace");
                    EquipmentCandidate helm=namedCandidate(ir.get(EquipmentSlot.HEAD),"Inquisitor's great helm");
                    EquipmentCandidate body=namedCandidate(ir.get(EquipmentSlot.BODY),"Inquisitor's hauberk");
                    EquipmentCandidate legs=namedCandidate(ir.get(EquipmentSlot.LEGS),"Inquisitor's plateskirt");
                    String[] baseNames={"Fire cape","Amulet of fury","Dragon defender","Barrows gloves","Dragon boots","Berserker ring (i)","Honourable blessing"};
                    EquipmentSlot[] baseSlots={EquipmentSlot.CAPE,EquipmentSlot.NECK,EquipmentSlot.SHIELD,EquipmentSlot.HANDS,EquipmentSlot.FEET,EquipmentSlot.RING,EquipmentSlot.AMMO};
                    if(mace!=null&&helm!=null&&body!=null&&legs!=null)
                    {
                        validation.append(System.lineSeparator()).append(System.lineSeparator()).append("Synthetic Inquisitor Validation").append(System.lineSeparator());
                        validation.append("2026 rules: helm +0.5%, hauberk +1.0%, plateskirt +1.0% Crush accuracy/damage independently; no full-set bonus and no mace amplification. Mace raw stats: +102 Crush / +96 Strength.").append(System.lineSeparator());
                        validation.append("Fixed accessories: Fire cape | Amulet of fury | Inquisitor's mace | Dragon defender | Barrows gloves | Dragon boots | Berserker ring (i) | Honourable blessing | Piety/best available").append(System.lineSeparator());
                        int[][] combos={{0,0,0},{1,0,0},{0,1,0},{0,0,1},{1,1,1}};
                        String[] labels={"No Inquisitor armour","Helm only","Hauberk only","Plateskirt only","Full armour"};
                        for(int ci=0;ci<combos.length;ci++)
                        {
                            Map<EquipmentSlot,EquipmentCandidate> ig=new EnumMap<>(EquipmentSlot.class); ig.put(EquipmentSlot.WEAPON,mace);
                            boolean complete=true;
                            for(int bi=0;bi<baseNames.length;bi++){EquipmentCandidate x=namedCandidate(ir.get(baseSlots[bi]),baseNames[bi]);if(x==null||x.getRequirementResult().isBlocked()){complete=false;break;}ig.put(baseSlots[bi],x);}
                            if(!complete) continue;
                            EquipmentCandidate fallbackHead=namedCandidate(ir.get(EquipmentSlot.HEAD),"Neitiznot faceguard");
                            EquipmentCandidate fallbackBody=namedCandidate(ir.get(EquipmentSlot.BODY),"Bandos chestplate");
                            EquipmentCandidate fallbackLegs=namedCandidate(ir.get(EquipmentSlot.LEGS),"Bandos tassets");
                            ig.put(EquipmentSlot.HEAD,combos[ci][0]==1?helm:fallbackHead);
                            ig.put(EquipmentSlot.BODY,combos[ci][1]==1?body:fallbackBody);
                            ig.put(EquipmentSlot.LEGS,combos[ci][2]==1?legs:fallbackLegs);
                            if(ig.get(EquipmentSlot.HEAD)==null||ig.get(EquipmentSlot.BODY)==null||ig.get(EquipmentSlot.LEGS)==null) continue;
                            CombatResult cr=meleeOptimizer.evaluateFixed(ig,AttackStyle.MELEE_CRUSH,monster,requestedSlayerMode);
                            if(cr==null) continue;
                            double ib=MeleeLoadoutEffects.inquisitorBonus(ig,AttackStyle.MELEE_CRUSH);
                            validation.append(String.format(java.util.Locale.ROOT,"%s | bonus +%.1f%% | %s | rawAtk +%d rawStr +%d | shield %s | roll %d vs %d | %.2f%% | max %d | EV %.5f | DPS %.5f",labels[ci],ib*100.0,cr.getStance(),cr.getAttackBonus(),cr.getStrengthBonus(),ig.containsKey(EquipmentSlot.SHIELD)?ig.get(EquipmentSlot.SHIELD).getItem().getName():"NONE",cr.getAttackRoll(),cr.getDefenceRoll(),cr.getAccuracy()*100.0,cr.getMaxHit(),cr.getDps()*cr.getAttackSpeed()*0.6,cr.getDps())).append(System.lineSeparator());
                        }
                        // Screenshot-parity row: the first full-set Wiki screenshot had an empty shield slot.
                        // Keep this explicit so a missing Dragon defender cannot masquerade as a mechanics bug.
                        Map<EquipmentSlot,EquipmentCandidate> noShield=new EnumMap<>(EquipmentSlot.class);
                        noShield.put(EquipmentSlot.WEAPON,mace);
                        EquipmentCandidate nsCape=namedCandidate(ir.get(EquipmentSlot.CAPE),"Fire cape");
                        EquipmentCandidate nsNeck=namedCandidate(ir.get(EquipmentSlot.NECK),"Amulet of fury");
                        EquipmentCandidate nsHands=namedCandidate(ir.get(EquipmentSlot.HANDS),"Barrows gloves");
                        EquipmentCandidate nsFeet=namedCandidate(ir.get(EquipmentSlot.FEET),"Dragon boots");
                        EquipmentCandidate nsRing=namedCandidate(ir.get(EquipmentSlot.RING),"Berserker ring (i)");
                        EquipmentCandidate nsAmmo=namedCandidate(ir.get(EquipmentSlot.AMMO),"Honourable blessing");
                        if(nsCape!=null&&nsNeck!=null&&nsHands!=null&&nsFeet!=null&&nsRing!=null&&nsAmmo!=null)
                        {
                            noShield.put(EquipmentSlot.HEAD,helm); noShield.put(EquipmentSlot.BODY,body); noShield.put(EquipmentSlot.LEGS,legs);
                            noShield.put(EquipmentSlot.CAPE,nsCape); noShield.put(EquipmentSlot.NECK,nsNeck); noShield.put(EquipmentSlot.HANDS,nsHands);
                            noShield.put(EquipmentSlot.FEET,nsFeet); noShield.put(EquipmentSlot.RING,nsRing); noShield.put(EquipmentSlot.AMMO,nsAmmo);
                            CombatResult ns=meleeOptimizer.evaluateFixed(noShield,AttackStyle.MELEE_CRUSH,monster,requestedSlayerMode);
                            if(ns!=null) validation.append(String.format(java.util.Locale.ROOT,"Full armour — NO SHIELD screenshot check | bonus +2.5%% | %s | rawAtk +%d rawStr +%d | shield NONE | roll %d vs %d | %.2f%% | max %d | EV %.5f | DPS %.5f",ns.getStance(),ns.getAttackBonus(),ns.getStrengthBonus(),ns.getAttackRoll(),ns.getDefenceRoll(),ns.getAccuracy()*100.0,ns.getMaxHit(),ns.getDps()*ns.getAttackSpeed()*0.6,ns.getDps())).append(System.lineSeparator());
                        }
                        validation.append("alpha50.4.47 audit: Wiki screenshot parity: full Inquisitor/no shield +163 Crush/+144 Strength -> roll 28153, 36.70% accuracy, max 43. The +2.5% Crush damage modifies integer max 42 -> 43; expected damage is then rebuilt from the max-43 successful-hit distribution, giving ~3.29106 DPS. Synthetic only; production ownership unchanged.");
                    }
                }
            }

            // alpha50.4.22: compact, reusable forced-ammo harness.  It deliberately
            // evaluates alternate enchanted bolts on one fixed crossbow + armour setup,
            // so a bolt does not need to become BiS before we can compare its mechanics
            // with the Wiki calculator.
            if (requestedStyle == AttackStyle.RANGED && selectedRangedReport != null)
            {
                RangedWeaponComparison crossbow = null;
                for (RangedWeaponComparison c : selectedRangedReport.getComparisons())
                {
                    String wn = c.getWeaponName() == null ? "" : c.getWeaponName().toLowerCase();
                    if (wn.contains("crossbow") && !c.getItems().isEmpty())
                    {
                        crossbow = c;
                        break;
                    }
                }
                if (crossbow != null)
                {
                    EquipmentCandidate weapon = crossbow.getItems().get(EquipmentSlot.WEAPON);
                    // alpha50.4.26.2: candidates were prepared on the optimiser worker.
                    // Rendering only filters them for the fixed crossbow's compatibility.
                    List<EquipmentCandidate> enchanted = new ArrayList<>();
                    for (EquipmentCandidate ammo : syntheticBoltValidationAmmo)
                        if (RangedAmmoRules.compatible(weapon, ammo)) enchanted.add(ammo);
                    if (!enchanted.isEmpty())
                    {
                        validation.append(System.lineSeparator()).append(System.lineSeparator());
                        validation.append("Bolt Validation").append(System.lineSeparator());
                        validation.append("Weapon: ").append(crossbow.getWeaponName()).append(System.lineSeparator());
                        validation.append("Synthetic ammo — ownership ignored").append(System.lineSeparator());
                        for (EquipmentCandidate ammo : enchanted)
                        {
                            Map<EquipmentSlot,EquipmentCandidate> forced = new EnumMap<>(crossbow.getItems());
                            forced.put(EquipmentSlot.AMMO, ammo);
                            RangedCombatResult br = rangedCalculator.calculate(forced, monster, requestedSlayerMode);
                            if (br == null) continue;
                            double ev = br.getDps() * br.getAttackSpeed() * 0.6;
                            int procMax = EnchantedBoltEffects.validatedProcMaxHit(ammo, monster,
                                Math.max(1, account.boosted(net.runelite.api.Skill.RANGED)), br.getMaxHit());
                            String maxText = procMax != br.getMaxHit()
                                ? "normal max " + br.getMaxHit() + " | proc max " + procMax
                                : "max " + br.getMaxHit();
                            validation.append(String.format("%s  %.5f DPS | EV %.5f | %.2f%% | %s",
                                ammo.getItem().getName(), br.getDps(), ev, br.getAccuracy()*100.0, maxText));
                            String effect = EnchantedBoltEffects.validationLabel(ammo, monster, Math.max(1, account.boosted(net.runelite.api.Skill.RANGED)), account.isKandarinHardCompleted());
                            if (effect != null && !effect.trim().isEmpty()) validation.append(" | ").append(effect);
                            validation.append(System.lineSeparator());
                        }
                        validation.append("Fixed armour/weapon; synthetic ammo forced for Wiki parity testing.");
                    }
                }
            }
            loadoutValidation.setText(validation.toString().trim());
            loadoutValidation.setCaretPosition(0);
            status.setText(monsterDatabase.getAll().size()+" unique monster variants  •  "+bankSnapshot.size()+" bank items"+(selectedCombat==null?"":"  •  melee validation alpha7"));
        });
        if(resultBestStyle==null)
            optimizerExecutor.execute(() -> calculateMissingBestStyles(generation,cacheKey,bankSnapshot,
                rankedSnapshot,monster,requestedSlayerMode));
    }

    private AttackStyle cachedBestStyleIfComplete()
    {
        Map<AttackStyle,Double> scores=new EnumMap<>(AttackStyle.class);
        for(AttackStyle style:AttackStyle.values())
        {
            double dps=0.0;
            if(style==AttackStyle.MELEE_STAB||style==AttackStyle.MELEE_SLASH||style==AttackStyle.MELEE_CRUSH)
            {
                MeleeOptimizationReport report=cachedMeleeReports.get(style);
                if(report==null)return null;
                if(report.getBest()!=null&&report.getBest().getResult()!=null)dps=report.getBest().getResult().getDps();
            }
            else if(style==AttackStyle.RANGED)
            {
                RangedOptimizationReport report=cachedRangedReports.get(style);
                if(report==null)return null;
                if(report.getBest()!=null&&report.getBest().getResult()!=null)dps=report.getBest().getResult().getDps();
            }
            else
            {
                MagicOptimizationReport report=cachedMagicReports.get(style);
                if(report==null)return null;
                if(report.getBest()!=null&&report.getBest().getResult()!=null)dps=report.getBest().getResult().getDps();
            }
            scores.put(style,dps);
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

    private EquipmentCandidate candidateById(List<EquipmentCandidate> candidates,int itemId)
    {
        if(candidates!=null) for(EquipmentCandidate c:candidates)
            if(c!=null && c.getItem().getItemId()==itemId) return c;
        return null;
    }

    private EquipmentCandidate namedCandidate(List<EquipmentCandidate> candidates,String name)
    {
        if(candidates!=null) for(EquipmentCandidate c:candidates)
            if(c!=null && c.getItem().getName().equalsIgnoreCase(name)) return c;
        return null;
    }

    public List<AttackStyle> getStylesBestToWorst()
    {
        List<AttackStyle> styles = new ArrayList<>();
        Collections.addAll(styles, AttackStyle.values());
        styles.sort(Comparator.comparingInt(this::styleScore).reversed());
        return styles;
    }

    private int styleScore(AttackStyle style)
    {
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked = intelligence.rank(cachedBank, style, slayerMode);
        int score = 0;
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            EquipmentCandidate c = firstUsable(ranked.get(slot));
            if (c != null)
            {
                score += c.getScore();
            }
        }
        return score;
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

    private static class MonsterRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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

    /** Keep generic mechanics diagnostics independent of Kurask/Turoth immunity. */
    private static MonsterDefinition withoutLeafyRestriction(MonsterDefinition m)
    {
        if (m == null || !LeafyTargetRules.restricted(m)) return m;
        java.util.List<String> attrs = new java.util.ArrayList<>();
        for (String a : m.getAttributes()) if (a == null || !"leafy".equalsIgnoreCase(a)) attrs.add(a);
        return new MonsterDefinition(m.getId(), m.getName()+" [regression fixture]",
            m.getCombatLevel(), m.getDefenceLevel(), m.getMagicLevel(), m.getRangedLevel(), m.getHitpoints(),
            m.getStabDefence(), m.getSlashDefence(), m.getCrushDefence(), m.getMagicDefence(),
            m.getLightDefence(), m.getStandardDefence(), m.getHeavyDefence(), attrs, m.getSize(),
            m.getWeaknessElement(), m.getWeaknessSeverity(), m.getMagicAttack());
    }

}
