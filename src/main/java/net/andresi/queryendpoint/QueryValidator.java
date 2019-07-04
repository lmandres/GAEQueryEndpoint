package net.andresi.queryendpoint;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import com.google.appengine.api.search.DateUtil;

public class QueryValidator {

    public static ArrayList<String> validateJsonHashList(
        EntitySettings settings,
        JsonHashList query
    ) {

        ArrayList<String> errorList = new ArrayList<String>();
        ArrayList<String> entityErrors = validateEntity(
            settings,
            query.getJsonItem("entity")
        );

        if (query.getJsonItem("action") == null) {
            errorList.add(
                "Required key/value pair not specified (\"action\")."
            );
        }
        for (int i = 0; i < entityErrors.size(); i++) {
            errorList.add(entityErrors.get(i));
        }

        if (errorList.size() <= 0) {

            String queryAction = query
                .getJsonItem("action")
                .toString()
                .toUpperCase()
            ;

            if (
                !(
                    queryAction.equals("CREATE") ||
                    queryAction.equals("RETRIEVE") ||
                    queryAction.equals("UPDATE") ||
                    queryAction.equals("DELETE")
                )
            ) {
                errorList.add(
                    "Specified action value is not valid (\"" +
                    query.getJsonItem("action") + "\"). " + 
                    "Action can be one of the following (" +
                    "\"CREATE\", \"RETRIEVE\", " +
                    "\"UPDATE\", \"DELETE\")."
                );
            } else {

                ArrayList<String> queryErrors = new ArrayList<String>();

                if (queryAction.equals("CREATE")) {
                    queryErrors = validateCreateQuery(settings, query);
                } else if (queryAction.equals("RETRIEVE")) {
                    queryErrors = validateRetrieveQuery(settings, query);
                } else if (queryAction.equals("UPDATE")) {
                    queryErrors = validateUpdateQuery(settings, query);
                } else if (queryAction.equals("DELETE")) {
                    queryErrors = validateDeleteQuery(settings, query);
                }

                for (int i = 0; i < queryErrors.size(); i++) {
                    errorList.add(queryErrors.get(i));
                }
            }
        }

        return errorList;
    }

    private static ArrayList<String> validateEntity(
        EntitySettings settings,
        Object entityObj
    ) {

        ArrayList<String> errorList = new ArrayList<String>();

        if (entityObj == null) {
            errorList.add(
                "Required key/value pair not specified (\"entity\")."
            );
        } else if (
            !settings.checkEntity(
                entityObj.toString()
            )
        ) {
            errorList.add(
                "Requested entity \"" + entityObj.toString() + "\" " +
                "does not exist in datastore."
            );
        }

        return errorList;
    }

    private static ArrayList<String> validateCreateQuery(
        EntitySettings settings,
        JsonHashList query
    ) {
        ArrayList<String> errorList = validateQueryObject( 
            settings,
            query
        );
        return errorList;
    }

    private static ArrayList<String> validateRetrieveQuery(
        EntitySettings settings,
        JsonHashList query
    ) {
        ArrayList<String> errorList = new ArrayList<String>();

        if (query.getJsonItem("properties") == null) {
            errorList.add(
                "Required key/value pair not specified " +
                "for RETRIEVE query (\"properties\")."
            );
        } else {
            ArrayList<String> propErrors = validateProperties(
                settings,
                query.getJsonItem("entity"),
                query.getJsonItem("properties")
            );
            for (int i = 0; i < propErrors.size(); i++) {
                errorList.add(propErrors.get(i));
            }
        }

        return errorList;
    }

    private static ArrayList<String> validateUpdateQuery(	
        EntitySettings settings,
        JsonHashList query
    ) {
        ArrayList<String> errorList = validateQueryObject( 
            settings,
            query
        );
        return errorList;
    }

    private static ArrayList<String> validateDeleteQuery(
        EntitySettings settings, 
        JsonHashList query
    ) {
        return null;
    }

    private static ArrayList<String> validateQueryObject(
        EntitySettings settings,
        JsonHashList query
    ) {

        ArrayList<String> errorList = new ArrayList<String>();

        if (query.getJsonItem("object") == null) {
            errorList.add(
                "Required key/value pair for query not specified " +
                "(\"object\")."
            );
        } else if (query.getJsonItem("object").getClass() != HashMap.class) {
            errorList.add(
                "Item specified for \"object\" is not a proper type " +
                "(\"object\")."
            );
        } else {

            HashMap<String, Object> objHash = (HashMap<String, Object>) query.getJsonItem("object");
            Object[] keySet = objHash.keySet().toArray();
            String[] entityProps = settings
                .getEntityProperties(
                    query.getJsonItem("entity").toString()
                )
            ;

            for (int i = 0; i < keySet.length; i++) {
                if (
                    !settings.checkEntityProperty(
                        query.getJsonItem("entity").toString(),
                        keySet[i].toString()
                    )
                ) {
                    errorList.add(
                        "Trying to put unknown property to datastore " +
                        "(\"" + keySet[i].toString() + "\")."
                    );
                }
            }

            for (int i = 0; i < entityProps.length; i++) {

                Object propNotNull;

                propNotNull = settings.getValue(
                    query.getJsonItem("entity").toString(),
                    entityProps[i],
                    "notnull"
                );
                if (
                    (propNotNull != null) &&
                    (propNotNull
                        .toString()
                        .toUpperCase()
                        .equals("TRUE")
                    )
                ) {
                    if (
                        query.getJsonItem("action")
                            .toString()
                            .equals("CREATE")
                    ) {
                        if (query.getJsonItem("object", entityProps[i]) == null) {
                            errorList.add(
                                "Value for \"" + entityProps[i] + "\" " +
                                "cannot be NULL."
                            );
                        }
                    }
                }

                if (query.getJsonItem("object", entityProps[i]) != null) {
                    ArrayList<String> propErrors = validateEntityObjectProperties(
                        settings,
                        query,
                        entityProps[i]
                    );
                    for (int j = 0; j < propErrors.size(); j++) {
                        errorList.add(propErrors.get(j));
                    }
                }
            } 
        }

        return errorList;
    }

    private static ArrayList<String> validateEntityObjectProperties(
        EntitySettings settings,
        JsonHashList query,
        String entityPropKey
    ) {

        ArrayList<String> errorList = new ArrayList<String>();

        Object entityPropObj = query
            .getJsonItem(
                "object",
                entityPropKey
            )
        ;
        Object propTypeObj = settings.getValue(
            query.getJsonItem("entity").toString(),
            entityPropKey,
            "type"
        );

        if (propTypeObj != null) {

            String propType = propTypeObj.toString().toUpperCase();

            if (propType.equals("STRING")) {
                if (entityPropObj.getClass() != String.class) {
                    errorList.add(
                        "Query type is not equal to property type " +
                        "(\"" + propType + "\")."
                    );
                }
            }

            if (propType.equals("KEY")) {
                if (entityPropObj.getClass() != String.class) {
                    errorList.add(
                        "Query type is not correct property type " +
                        "(\"STRING\")."
                    );
                }
            }

            if (propType.equals("TIMESTAMP")) {
                if (entityPropObj.getClass() != String.class) {
                    errorList.add(
                        "Query type is not correct property type " +
                        "(\"STRING\")."
                    );
                } else {
                    try {
                        DatatypeConverter.parseDateTime(
                            query
                                .getJsonItem("object", entityPropKey)
                                .toString()
                        );
                    } catch (IllegalArgumentException iae) {
                        errorList.add(
                            "TIMESTAMP property is not formatted correctly. " +
                            "Please enter an ISO 8601 compliant date."
                        ); 
                    }
                }
            }

            if (propType.equals("ENTITY_INDEX")) {
                if (entityPropObj.getClass() != Integer.class) {
                    errorList.add(
                        "Query type is not correct property type " +
                        "(\"INTEGER\")."
                    );
                } else {
                    String relatedEntity = (String) settings.getValue(
                        query.getJsonItem("entity").toString(),
                        entityPropKey,
                        "related_entity"
                    );
                    String displayProp = (String) settings.getValue(
                        query.getJsonItem("entity").toString(),
                        entityPropKey,
                        "display_property"
                    );
                    ArrayList<Object> valueList = (ArrayList<Object>) settings.getValue(
                        relatedEntity.toString(),
                        displayProp.toString(),
                        "value_list"
                    );

                    try {
                        valueList.get(
                            Integer.parseInt(
                                query
                                    .getJsonItem(
                                        "object", 
                                        entityPropKey
                                    ).toString()
                            )
                        );
                    } catch (IndexOutOfBoundsException ioobe) {
                        errorList.add(
                            "Object field (\"" + entityPropKey + "\") " +
                            "does not point to an entity property value."
                        );
                    }
                }
            } 
        }

        return errorList; 
    }

    private static ArrayList<String> validateProperties(
        EntitySettings settings,
        Object entity,
        Object propertyList
    ) {

        ArrayList<String> errorList = new ArrayList<String>();

        if (propertyList == null) {
            errorList.add(
                "Required key/value pair not specified " +
                "(\"properties\")."
            );
        }

        if (errorList.size() <= 0) {
            if (propertyList.getClass() != ArrayList.class) {
                errorList.add(
                    "Item specified for \"properties\" is not a proper type " +
                    "(\"array\")."
                );
            } else {

                ArrayList<Object> items = (ArrayList<Object>) propertyList;

                for (int i = 0; i < items.size(); i++) {

                    ArrayList<String> itemErrors = validatePropertiesItem(
                        settings,
                        entity.toString(),
                        items.get(i)
                    );
                    for (int j = 0; j < itemErrors.size(); j++) {
                        errorList.add(itemErrors.get(j));
                    }
                }
            }
        }

        return errorList;
    }

    private static ArrayList<String> validatePropertiesItem(
        EntitySettings settings,
        String entity,
        Object item
    ) {

        ArrayList<String> errorList = new ArrayList<String>();

        if (item.getClass() != String.class) {
            if (item.getClass() != HashMap.class) {
                errorList.add(
                    "Property key must be string or object."
                );
            } else if (item.getClass() == HashMap.class) {
                HashMap<String, Object> itemHash = (HashMap<String, Object>) item;
                ArrayList<String> itemErrors = validatePropertiesObjectItem(
                    settings,
                    entity,
                    itemHash
                );
                for (int i = 0; i < itemErrors.size(); i++) {
                    errorList.add(itemErrors.get(i));
                }
            }
        } else {
            if (
                !settings.checkEntityProperty(
                    entity.toString(),
                    item.toString()
                )
            ) {
                errorList.add(
                    "Property key \"" +  item.toString() + "\" " +
                    "does not exist in datastore."
                );
            }
        }

        return errorList;
    }

    private static ArrayList<String> validatePropertiesObjectItem(
        EntitySettings settings,
        String entity,
        HashMap<String, Object> queryHash
    ) {

        ArrayList<String> errorList = new ArrayList<String>();

        String ancestorEntity = null;
        String childEntity = null;
        String testEntity = null;

        if (queryHash.get("entity") == null) {
            errorList.add(
                "Required key/value pair not specified " +
                "(\"entity\")."
            );
        } else if (queryHash.get("entity").getClass() != String.class) {
            errorList.add(
                "Item specified for \"entity\" is not a proper type " +
                "(\"string\")."
            );
        }

        if (queryHash.get("join_type") == null) {
            errorList.add(
                "Required key/value pair not specified " +
                "(\"join_type\")."
            );
        } else if (queryHash.get("join_type").getClass() != String.class) {
            errorList.add(
                "Item specified for \"join_type\" is not a proper type " +
                "(\"string\")."
            );
        } else {
            ArrayList<String> joinErrors = validateJoinType(
                settings,
                queryHash.get("join_type").toString()
            );
            for (int i = 0; i < joinErrors.size(); i++) {
                errorList.add(joinErrors.get(i));
            }
        }

        if (queryHash.get("entity") != null) {
            if (queryHash.get("entity").getClass() == String.class) {
                testEntity = queryHash.get("entity").toString();
            }
        }

        if (
            getEntityEntityRelation(
                settings,
                entity,
                testEntity
            ) != null
        ) {
            childEntity = entity;
            ancestorEntity = testEntity;
        } else if (
            getEntityEntityRelation(
                settings,
                testEntity,
                entity
            ) != null
        ) {
            childEntity = testEntity;
            ancestorEntity = entity;
        }

        if (errorList.size() <= 0) {
            errorList = validateEntity(
                settings,
                ancestorEntity
            );
            if (errorList.size() <= 0) {
                errorList = validateEntity(
                    settings,
                    childEntity
                );
            }
            if (errorList.size() > 0) {
                errorList.add(
                    "Datastore entity \"" +  entity + "\" does not " +
                    "relate to \"related_entity\"."
                );
            } else {
                ArrayList<String> propsErrors = validateProperties(
                    settings,
                    testEntity,
                    queryHash.get("properties")
                );
                for (int i = 0; i < propsErrors.size(); i++) {
                    errorList.add(propsErrors.get(i));
                }
            }
        }

        return errorList;
    }

    public static ArrayList<String> getChildEntityProperties(
        EntitySettings settings,
        String relatedEntity
    ) {
        ArrayList<String> entityPropsList = new ArrayList<String>();
        String[] entityProps = settings.getEntityProperties(
            relatedEntity
        );

        for (int i = 0; i < entityProps.length; i++) {
            if (
                (
                    settings.getValue(
                        relatedEntity,
                        entityProps[i],
                        "type"
                    ) != null
                ) && (
                    settings.getValue(
                        relatedEntity,
                        entityProps[i],
                        "type"
                    ).equals("KEY")
                ) && (
                    settings.getValue(
                        relatedEntity,
                        entityProps[i],
                        "related_entity"
                    ) != null
                ) && (
                    settings.getValue(
                        relatedEntity,
                        entityProps[i],
                        "related_entity"
                    ).getClass() == String.class
                )
            ) {
                entityPropsList.add(entityProps[i]);
            }
        }

        return entityPropsList;
    }

    public static String getEntityEntityRelation(
        EntitySettings settings,
        String entity1,
        String entity2
    ) {

        String returnVal = null;
        ArrayList<String> subEntityProperties = getChildEntityProperties(
            settings,
            entity1
        );

        for (int i = 0; i < subEntityProperties.size(); i++) {
            try {
                if (
                    settings.getValue(
                        entity1,
                        subEntityProperties.get(i),
                        "related_entity"
                    ).equals(entity2)
                ) {
                    returnVal = subEntityProperties.get(i);
                    break;
                }
            } catch (NullPointerException npe) {
            }
        }

        return returnVal;
    }

    public static ArrayList<String> validateJoinType(
        EntitySettings settings,
        String joinType
    ) {
        ArrayList<String> errorList = new ArrayList<String>();

        if (
            !(
                joinType.equals("INNER_JOIN") ||
                joinType.equals("OUTER_JOIN") ||
                joinType.equals("SUB_QUERY")
            )
        ) {
            errorList.add(
                "Join type specified is invalid."
            );
        }

        return errorList;
    }
}
