package gg.ethereallabs.heavenkits.data;

import gg.ethereallabs.heavenkits.kits.models.KitTemplate;

import java.util.Map;
import java.util.UUID;

public interface Storage {
    void loadAllKits(Map<String, KitTemplate> kitsOut);
    void insertKit(KitTemplate kit);
    void replaceKit(KitTemplate kit);
    void deleteKit(String name);

    void updatePlayerCooldown(String playerUUID, String kitName, long redeemTime);
    Map<String, Long> loadPlayerCooldowns(UUID playerUUID);
    void removeExpiredCooldown(String playerUUID, String kitName);
    void cleanExpiredCooldowns();
}