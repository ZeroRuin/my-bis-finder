package com.personalbis;

import java.util.List;
import java.util.Map;

/** The recommended gear for one attack style and what it does to the target. */
public interface StyleLoadout
{
    Map<EquipmentSlot, EquipmentCandidate> getItems();

    CombatOutcome getResult();

    List<Integer> itemIds();
}
