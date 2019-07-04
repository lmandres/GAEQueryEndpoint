package net.andresi.queryendpoint;

import java.util.HashMap;

public class EntitySettings {

    HashMap<String, HashMap<String, HashMap<String, Object>>> entityHash;

    public EntitySettings() {
        entityHash = new HashMap<String, HashMap<String, HashMap<String, Object>>>();
    }

    public void setValue(
        String entityIn, 
        String propertyIn, 
        String attributeIn,
        Object valueIn
    ) {
        if (!this.entityHash.containsKey(entityIn)) {
            this.entityHash.put(
                entityIn,
                new HashMap<String, HashMap<String, Object>>()
            );
        }
        if (!this.entityHash.get(entityIn).containsKey(propertyIn)) {
            this.entityHash
                .get(entityIn)
                .put(
                    propertyIn,
                    new HashMap<String, Object>()
                )
            ;
        }
        this.entityHash
            .get(entityIn)
            .get(propertyIn)
            .put(attributeIn, valueIn)
        ;
    }

    public Object getValue(
        String entityIn,
        String propertyIn,
        String attributeIn
    ) {
        Object returnVal = null;
        try {
            returnVal = this.entityHash.get(entityIn).get(propertyIn).get(attributeIn);
        } catch (NullPointerException npe) {
        }
        return returnVal;
    }

    public String[] getEntities() {
        Object[] entityObjs = this.entityHash.keySet().toArray();
        String[] entityKeys = new String[entityObjs.length];

        for (int i = 0; i < entityObjs.length; i++) {
            entityKeys[i] = entityObjs[i].toString();
        }
         
        return entityKeys;
    }

    public String[] getEntityProperties(String entityIn) {
        Object[] propertyObjs = this.entityHash
            .get(entityIn)
            .keySet()
            .toArray()
        ;
        String[] propertyKeys = new String[propertyObjs.length];

        for (int i = 0; i < propertyObjs.length; i++) {
            propertyKeys[i] = propertyObjs[i].toString();
        }

        return propertyKeys;
    }

    public boolean checkEntityProperty(
        String entityIn,
        String propertyIn
    ) {
        boolean returnVal = false;
        try{
            if (this.entityHash.get(entityIn).get(propertyIn) != null) {
                returnVal = true;
            }
        } catch (NullPointerException npe) {
        }
        return returnVal;
    }

    public boolean checkEntity(String entityIn) {
        boolean returnVal = false;
        if (this.entityHash.get(entityIn) != null) {
            returnVal = true;
        }
        return returnVal;
    }
}
