package gg.ethereallabs.heavenkits.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.ReplaceOptions;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MongoStorage implements Storage {
    private final MongoCollection<Document> collection;

    public MongoStorage(MongoDB mongoDB) {
        this.collection = mongoDB.getKitCollection();
    }

    @Override
    public void loadAllKits(Map<String, KitTemplate> kitsOut) {
        Document filter = new Document("_id", new Document("$ne", "cooldowns"));
        for (Document doc : collection.find(filter)) {
            if (doc.containsKey("name") && doc.getString("name") != null) {
                KitTemplate kit = KitSerializer.deserializeKit(doc);
                kitsOut.put(kit.getName(), kit);
            }
        }
    }

    @Override
    public void insertKit(KitTemplate kit) {
        CompletableFuture.runAsync(() -> {
            Document doc = KitSerializer.serializeKit(kit);
            collection.insertOne(doc);
        });
    }

    @Override
    public void replaceKit(KitTemplate kit) {
        CompletableFuture.runAsync(() -> {
            Document query = new Document("name", kit.getName());
            Document updatedDoc = KitSerializer.serializeKit(kit);
            collection.replaceOne(query, updatedDoc, new ReplaceOptions().upsert(true));
        });
    }

    @Override
    public void deleteKit(String name) {
        CompletableFuture.runAsync(() -> {
            collection.deleteOne(new Document("name", name));
        });
    }

    @Override
    public void updatePlayerCooldown(String playerUUID, String kitName, long redeemTime) {
        CompletableFuture.runAsync(() -> {
            Document query = new Document("_id", "cooldowns");
            Document update = new Document("$set",
                    new Document("players." + playerUUID + "." + kitName, redeemTime));
            UpdateOptions options = new UpdateOptions().upsert(true);
            collection.updateOne(query, update, options);
        });
    }

    @Override
    public Map<String, Long> loadPlayerCooldowns(UUID playerUUID) {
        Document cooldownsDoc = collection.find(new Document("_id", "cooldowns")).first();
        if (cooldownsDoc == null) return Collections.emptyMap();

        Document players = cooldownsDoc.get("players", Document.class);
        if (players == null) return Collections.emptyMap();

        Document playerCooldowns = players.get(playerUUID.toString(), Document.class);
        if (playerCooldowns == null) return Collections.emptyMap();

        Map<String, Long> cooldownMap = new HashMap<>();
        for (String kitName : playerCooldowns.keySet()) {
            Long redeemTime = playerCooldowns.getLong(kitName);
            if (redeemTime != null) {
                cooldownMap.put(kitName, redeemTime);
            }
        }
        return cooldownMap;
    }

    @Override
    public void removeExpiredCooldown(String playerUUID, String kitName) {
        CompletableFuture.runAsync(() -> {
            Document query = new Document("_id", "cooldowns");
            Document update = new Document("$unset",
                    new Document("players." + playerUUID + "." + kitName, ""));
            collection.updateOne(query, update);
        });
    }

    @Override
    public void cleanExpiredCooldowns() {
        CompletableFuture.runAsync(() -> {
            Document cooldownsDoc = collection.find(new Document("_id", "cooldowns")).first();
            if (cooldownsDoc == null) return;

            Document players = cooldownsDoc.get("players", Document.class);
            if (players == null) return;

            for (String uuidString : players.keySet()) {
                Document playerCooldowns = players.get(uuidString, Document.class);
                List<String> toRemove = new ArrayList<>();

                for (String kitName : playerCooldowns.keySet()) {
                    long redeemTime = playerCooldowns.getLong(kitName);
                    if (redeemTime < System.currentTimeMillis()) {
                        toRemove.add(kitName);
                    }
                }

                if (!toRemove.isEmpty()) {
                    Document update = new Document();
                    for (String kitName : toRemove) {
                        update.put("players." + uuidString + "." + kitName, "");
                    }
                    collection.updateOne(
                            new Document("_id", "cooldowns"),
                            new Document("$unset", update)
                    );
                }
            }
        });
    }
}