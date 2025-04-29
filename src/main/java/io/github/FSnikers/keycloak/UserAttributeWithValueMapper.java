package io.github.FSnikers.keycloak;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class UserAttributeWithValueMapper extends AbstractClaimMapper {

    public static final String[] COMPATIBLE_PROVIDERS = new String[]{"keycloak-oidc", "oidc", "saml"};
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    public static final String
            USER_ATTRIBUTE = "user.attribute",
            CLAIM_NAME = "claim",
            VALUE_MAPPINGS = "value.mappings",
            DEFAULT_VALUE = "default.value",
            REGEX_ENABLED = "regex.enabled";

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    static {
        // Базовые параметры
        addConfigProperty(CLAIM_NAME, "Claim Name",
                "Name of claim to import (use '.' for nested claims)",
                ProviderConfigProperty.STRING_TYPE, null);

        addConfigProperty(USER_ATTRIBUTE, "User Attribute",
                "Target user attribute name",
                ProviderConfigProperty.STRING_TYPE, null);

        // Дополнительные параметры
        addConfigProperty(VALUE_MAPPINGS, "Value Mappings",
                "JSON map for value transformations. Example: {\"external\":\"internal\"}",
                ProviderConfigProperty.STRING_TYPE, "{}");

        addConfigProperty(DEFAULT_VALUE, "Default Value",
                "Value to use when claim is missing",
                ProviderConfigProperty.STRING_TYPE, null);

        addConfigProperty(REGEX_ENABLED, "Enable Regex",
                "Treat mapping keys as regex patterns",
                ProviderConfigProperty.BOOLEAN_TYPE, "false");
    }

    private static void addConfigProperty(String name, String label,
                                          String helpText, String type, String defaultValue) {
        ProviderConfigProperty prop = new ProviderConfigProperty();
        prop.setName(name);
        prop.setLabel(label);
        prop.setHelpText(helpText);
        prop.setType(type);
        if (defaultValue != null) {
            prop.setDefaultValue(defaultValue);
        }
        configProperties.add(prop);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return "advanced-attribute-mapper";
    }

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getDisplayCategory() {
        return "Attribute Importer";
    }

    @Override
    public String getDisplayType() {
        return "Attribute Importer with Value Mapper";
    }

    @Override
    public String getHelpText() {
        return "Imports claim with value transformations, regex support and default values";
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
                                   IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        setMappedAttribute(user, mapperModel, context);
    }

    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm,
                                            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        context.setUserAttribute(
                mapperModel.getConfig().get(USER_ATTRIBUTE),
                getTransformedValue(mapperModel, context)
        );
    }

    private void setMappedAttribute(UserModel user, IdentityProviderMapperModel mapperModel,
                                    BrokeredIdentityContext context) {
        String attribute = mapperModel.getConfig().get(USER_ATTRIBUTE);
        String newValue = getTransformedValue(mapperModel, context);

        if (newValue != null) {
            String currentValue = user.getFirstAttribute(attribute);
            if (!newValue.equals(currentValue)) {
                user.setSingleAttribute(attribute, newValue);
            }
        }
    }

    private String getTransformedValue(IdentityProviderMapperModel mapperModel,
                                       BrokeredIdentityContext context) {
        try {
            String attribute = mapperModel.getConfig().get(USER_ATTRIBUTE);
            Object claimValue = getClaimValue(mapperModel, context);
            String value = claimValue != null ? claimValue.toString() : null;

            // Получаем конфигурацию
            String mappingsJson = mapperModel.getConfig().get(VALUE_MAPPINGS);
            String defaultValue = mapperModel.getConfig().get(DEFAULT_VALUE);
            boolean regexEnabled = Boolean.parseBoolean(
                    mapperModel.getConfig().getOrDefault(REGEX_ENABLED, "false"));

            // Применяем преобразования
            return transformValue(value, mappingsJson, defaultValue, regexEnabled);
        } catch (Exception e) {
            throw new RuntimeException("Error processing attribute mapping", e);
        }
    }

    private String transformValue(String originalValue, String mappingsJson,
                                  String defaultValue, boolean regexEnabled) throws Exception {
        if (originalValue == null) {
            return defaultValue;
        }

        if (mappingsJson != null && !mappingsJson.trim().isEmpty()) {
            Map<String, String> mappings = jsonMapper.readValue(
                    mappingsJson, new TypeReference<Map<String, String>>() {
                    });

            if (regexEnabled) {
                // Режим regex: ищем первое совпадение с паттернами
                for (Map.Entry<String, String> entry : mappings.entrySet()) {
                    if (Pattern.compile(entry.getKey()).matcher(originalValue).matches()) {
                        return entry.getValue();
                    }
                }
            } else if (mappings.containsKey(originalValue)) {
                // Обычный режим: точное совпадение
                return mappings.get(originalValue);
            }
        }

        return originalValue != null ? originalValue : defaultValue;
    }
}