package org.superbiz.moviefun;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DatabaseServiceCredentials {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, List<DatabaseServiceCredentials.VcapService>>> jsonType = new TypeReference<Map<String, List<DatabaseServiceCredentials.VcapService>>>() {
    };
    private final String vcapServicesJson;

    public DatabaseServiceCredentials(String vcapServicesJson) {
        this.vcapServicesJson = vcapServicesJson;
    }

    public String jdbcUrl(String name) {
        try {
            Map<String, List<DatabaseServiceCredentials.VcapService>> vcapServices = (Map)objectMapper.readValue(this.vcapServicesJson, jsonType);
            return (String)vcapServices.values().stream().flatMap(Collection::stream).filter((service) -> {
                return service.name.equalsIgnoreCase(name);
            }).findFirst().map((service) -> {
                return service.credentials;
            }).flatMap((credentials) -> {
                return Optional.ofNullable((String)credentials.get("jdbcUrl"));
            }).orElseThrow(() -> {
                return new IllegalStateException("No " + name + " found in VCAP_SERVICES");
            });
        } catch (IOException var4) {
            throw new IllegalStateException("No VCAP_SERVICES found", var4);
        }
    }

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static class VcapService {
        String name;
        Map<String, Object> credentials;

        VcapService() {
        }

        void setName(String name) {
            this.name = name;
        }

        void setCredentials(Map<String, Object> credentials) {
            this.credentials = credentials;
        }
    }
}
