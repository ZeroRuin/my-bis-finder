package com.personalbis.wiki;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WikiDataPack
{
    private final Gson gson;
    private List<WikiEquipment> equipment = Collections.emptyList();
    private List<WikiMonster> monsters = Collections.emptyList();
    private List<WikiSpell> spells = Collections.emptyList();
    private Map<Integer, WikiEquipment> equipmentById = Collections.emptyMap();

    @Inject
    public WikiDataPack(Gson gson)
    {
        this.gson = gson;
        reload();
    }

    public void reload()
    {
        equipment = loadList("/wiki-data/equipment.json", new TypeToken<List<WikiEquipment>>(){}.getType());
        monsters = loadList("/wiki-data/monsters.json", new TypeToken<List<WikiMonster>>(){}.getType());
        spells = loadList("/wiki-data/spells.json", new TypeToken<List<WikiSpell>>(){}.getType());

        Map<Integer, WikiEquipment> byId = new HashMap<>();
        for (WikiEquipment item : equipment)
        {
            WikiEquipment current = byId.get(item.id);
            if (current == null || preferredVersion(item.version, current.version))
            {
                byId.put(item.id, item);
            }
        }
        equipmentById = Collections.unmodifiableMap(byId);
    }

    private boolean preferredVersion(String candidate, String current)
    {
        String c = candidate == null ? "" : candidate.trim();
        String old = current == null ? "" : current.trim();
        if (c.isEmpty() && !old.isEmpty()) return true;
        return "Standard".equalsIgnoreCase(c) && !"Standard".equalsIgnoreCase(old);
    }

    private <T> List<T> loadList(String resource, Type type)
    {
        try (InputStream in = WikiDataPack.class.getResourceAsStream(resource))
        {
            if (in == null) return Collections.emptyList();
            List<T> result = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
            return result == null ? Collections.emptyList() : Collections.unmodifiableList(result);
        }
        catch (Exception ex)
        {
            return Collections.emptyList();
        }
    }

    public WikiEquipment getEquipment(int id) { return equipmentById.get(id); }
    public List<WikiEquipment> getEquipment() { return equipment; }
    public List<WikiMonster> getMonsters() { return monsters; }
    public List<WikiSpell> getSpells() { return spells; }

    public WikiMonster findMonster(String name)
    {
        if (name == null) return null;
        String target = name.trim().toLowerCase(Locale.ROOT);
        for (WikiMonster monster : monsters)
        {
            if (monster.name != null && monster.name.toLowerCase(Locale.ROOT).equals(target)) return monster;
        }
        return null;
    }

    public String status()
    {
        return "Wiki pack: " + equipment.size() + " equipment / " + monsters.size() + " monsters / " + spells.size() + " spells";
    }
}
