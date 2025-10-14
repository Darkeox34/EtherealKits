package gg.ethereallabs.etherealkits.data;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

public class MongoDB {
    private final MongoClient client;
    private final MongoDatabase database;

    public MongoDB(FileConfiguration config) {
        String host = config.getString("mongodb.host");
        int port = config.getInt("mongodb.port");
        String dbName = config.getString("mongodb.database");
        String user = config.getString("mongodb.username");
        String pass = config.getString("mongodb.password");

        String uri;

        if(user.isEmpty() || pass.isEmpty()) {
            uri = "mongodb://" + host + ":" + port;
        } else {
            uri = "mongodb://" + user + ":" + pass + "@" + host + ":" + port;
        }

        client = MongoClients.create(uri);
        database = client.getDatabase(dbName);
    }

    public MongoCollection<Document> getKitCollection() {
        return database.getCollection("kits");
    }

    public void close() {
        client.close();
    }
}
