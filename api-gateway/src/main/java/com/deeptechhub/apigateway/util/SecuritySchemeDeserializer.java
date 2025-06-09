package com.deeptechhub.apigateway.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.io.IOException;

public class SecuritySchemeDeserializer extends StdDeserializer<SecurityScheme> {
    public SecuritySchemeDeserializer() {
        super(SecurityScheme.class);
    }

    @Override
    public SecurityScheme deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        SecurityScheme scheme = new SecurityScheme();

        if (node.has("type")) {
            String type = node.get("type").asText();
            // Normalize the type value
            scheme.setType(SecurityScheme.Type.valueOf(type.toUpperCase()));
        }

        if (node.has("scheme")) scheme.setScheme(node.get("scheme").asText());
        if (node.has("bearerFormat")) scheme.setBearerFormat(node.get("bearerFormat").asText());

        return scheme;
    }
}
