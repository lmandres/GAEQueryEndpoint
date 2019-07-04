package net.andresi.queryendpoint;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonGenerator;

public class QueryEndpoint extends HttpServlet {

    DatastoreManager datastoreManager;

    @Override
    public void init() throws ServletException {
        EntitySettings settings = EntitySettingsFactory.getSettings();
        this.datastoreManager = new DatastoreManager(settings);
    }

    @Override
    public void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        JsonHashList query = null;
        try {
            query = JsonHashListFactory.parseJsonToHashList(
                JacksonFactory
                    .getDefaultInstance()
                    .createJsonParser(request.getParameter("query"))
            );
            writeQueryResults(response, query);
        } catch (JsonParseException jpe) {
            response.setContentType("application/json");
            response.setStatus(400);
            try {
                response
                    .getWriter()
                    .println(
                        "{" +
                            "\"error\": [\"JSON Parse error. " +
 	                    "Malformed JSON string.\"]" + 
                        "}"
                    )
                ;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        JsonHashList query = null;
        try {
            query = JsonHashListFactory.parseJsonToHashList(
                JacksonFactory
                    .getDefaultInstance()
                    .createJsonParser(request.getReader())
            );
            writeQueryResults(response, query);
        } catch (JsonParseException jpe) {
            response.setContentType("application/json");
            response.setStatus(400);
            try {
                response
                    .getWriter()
                    .println(
                        "{" +
                            "\"error\": [\"JSON Parse error. " +
 	                    "Malformed JSON string.\"]" + 
                        "}"
                    )
                ;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void writeQueryResults(
        HttpServletResponse response,
        JsonHashList query
    ) {
        response.setContentType("application/json");
        try {

            JsonGenerator jsonGenerator = JacksonFactory
                .getDefaultInstance()
                .createJsonGenerator(response.getWriter())
            ;
            ArrayList<String> errorList = this
                .datastoreManager
                .getErrorList(query)
            ;

            if (errorList.size() <= 0) {
                this.datastoreManager.outputQueryResults(
                    query,
                    jsonGenerator
                );
            } else {

                response.setStatus(400);

                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("errors");
                jsonGenerator.writeStartArray();
                for (int i = 0; i < errorList.size(); i++) {
                    jsonGenerator.writeString(errorList.get(i));
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
                jsonGenerator.close();

            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
