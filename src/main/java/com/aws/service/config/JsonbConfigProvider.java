package com.aws.service.config;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonbConfigProvider implements ContextResolver<Jsonb> {

    private final Jsonb jsonb;

    public JsonbConfigProvider() {
        JsonbConfig config = new JsonbConfig()
                .withDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", null)
                .withNullValues(false)
                .withFormatting(true);

        this.jsonb = JsonbBuilder.create(config);
    }

    @Override
    public Jsonb getContext(Class<?> type) {
        return jsonb;
    }
}
