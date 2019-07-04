package net.andresi.queryendpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.api.client.json.JsonGenerator;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class DatastoreManager {

    DatastoreService datastore;
    EntitySettings datastoreSettings;

    public DatastoreManager(EntitySettings settingsIn) {
        this.datastoreSettings = settingsIn;
        this.datastore = DatastoreServiceFactory.getDatastoreService();
    }

    public EntitySettings getSettings() {
        return this.datastoreSettings;
    }

    public ArrayList<String> getErrorList(
        JsonHashList query
    ) {
        ArrayList<String> errorList = QueryValidator
            .validateJsonHashList(
                this.datastoreSettings,
                query
            )
        ;
        return errorList;
    }

    public void outputQueryResults(
        JsonHashList query,
        JsonGenerator jsonGenerator
    ) {

        String queryAction = query
            .getJsonItem("action")
            .toString()
            .toUpperCase()
        ;

        if (queryAction.equals("CREATE")) {
            createQuery(query, jsonGenerator);
        } else if (queryAction.equals("RETRIEVE")) {
            retrieveQuery(query, jsonGenerator);
        } else if (queryAction.equals("UPDATE")) {
            updateQuery(query, jsonGenerator);
        } else if (queryAction.equals("DELETE")) {
            deleteQuery(query, jsonGenerator);
        }
    }

    private void createQuery(
        JsonHashList query,
        JsonGenerator jsonGenerator
    ) {

        Entity entity = new Entity(query.getJsonItem("entity").toString());
        HashMap<String, Object> objHash = (HashMap<String, Object>) query.getJsonItem("object");
        Object[] keySet = objHash.keySet().toArray();

        for (int i = 0; i < keySet.length; i++) {
            entity.setProperty(
                keySet[i].toString(),
                query.getJsonItem("object", keySet[i].toString())
            );
        }

        this.datastore.put(entity);

        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("key");
            jsonGenerator.writeString(
                KeyFactory.keyToString(entity.getKey())
            );
            jsonGenerator.writeEndObject();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                jsonGenerator.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void retrieveQuery(
        JsonHashList query,
        JsonGenerator jsonGenerator
    ) {

        ArrayList<String> propList = (ArrayList<String>) query.getJsonItem("properties");
        Query dsQuery = new Query(query.getJsonItem("entity").toString());

        List<Entity> entityList = this.datastore.prepare(
            dsQuery
        ).asList(
            FetchOptions.Builder.withDefaults()
        );

        try {
            jsonGenerator.writeStartArray();
            for (int i = 0; i < entityList.size(); i++) {
                jsonGenerator.writeStartObject();
                for (int j = 0; j < propList.size(); j++) {
                    writeEntityItem(
                        entityList.get(i),
                        propList.get(j),
                        jsonGenerator
                    );
                }
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                jsonGenerator.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    private void updateQuery(
        JsonHashList query,
        JsonGenerator jsonGenerator
    ) {
    }

    private void deleteQuery(
        JsonHashList query,
        JsonGenerator jsonGenerator
    ) {
    }

    private void writeEntityItem(
        Entity entity,
        String prop,
        JsonGenerator jsonGenerator
    ) {
        String type = this.datastoreSettings.getValue(
            entity.getKind(),
            prop,
            "type"
        )
        .toString()
        .toUpperCase()
        ;

        try {
            switch (type) {
            case "STRING":
            case "TIMESTAMP":
                jsonGenerator.writeFieldName(prop);
                jsonGenerator.writeString(
                    entity.getProperty(prop)
                    .toString()
                );
                break;
            default:
                break;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }       
}
