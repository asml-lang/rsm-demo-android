package com.github.saman.rsm.android;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

class JsonSchemaValidator {
    private final Context context;
    private ObjectMapper mapper = new ObjectMapper();
    private Set<ValidationMessage> errors;

    public JsonSchemaValidator(Context ctx) {
        this.context = ctx;
    }

    public Set<ValidationMessage> getLastErrors() {
        return errors;
    }

    protected JsonSchema getJsonSchemaFromStringContent(String schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStringContent(String content) throws IOException {
        return mapper.readTree(content);
    }

    public boolean isModelValid(String model) {
        try {
            JsonSchema schema = getMainSchema();
            JsonNode node = getJsonNodeFromStringContent(model);
            errors = schema.validate(node);
            return errors != null && errors.isEmpty();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String convertStreamToString(InputStream is) throws IOException {
        // http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                sb.append(line);
                firstLine = false;
            } else {
                sb.append("\n").append(line);
            }
        }
        reader.close();
        return sb.toString();
    }

    private JsonSchema getMainSchema() {
        try (InputStream is = context.getResources().openRawResource(R.raw.schema)) {

            String content = convertStreamToString(is);
            return getJsonSchemaFromStringContent(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isStateValid(String modelSchema, String state) {
        try {
            JsonSchema schema = getJsonSchemaFromStringContent(modelSchema);
            JsonNode node = getJsonNodeFromStringContent(state);
            errors = schema.validate(node);
            return errors != null && errors.isEmpty();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
