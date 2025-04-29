# 🔑 Keycloak Attribute Imporyer with Value Mapper

[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://github.com/FSnikers/KeycloakAttributeImporterValueMapper/blob/master/LICENSE)
[![Keycloak Version](https://img.shields.io/badge/Keycloak-26.0%2B-blue)](https://www.keycloak.org)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](https://openjdk.org)

Advanced attribute mapping extension for Keycloak with value transformation capabilities.

## ✨ Features

- **Value Transformation**: Map claim values using JSON configuration
- **Regex Support**: Advanced pattern matching for dynamic values
- **Default Values**: Fallback for missing or null claims
- **Multi-Provider**: Compatible with OIDC, SAML and social providers
- **Performance Optimized**: Only updates attributes when values change
- **Nested Claims**: Supports dot notation (e.g. `address.locality`)

## 🚀 Installation

### Checked with:
✅ Keycloak 26.0

### Build from Source
```bash
git clone https://github.com/FSnikers/keycloak-advanced-attribute-mapper.git
cd keycloak-advanced-attribute-mapper
mvn clean package
```

## 🛠 Configuration

### Admin Console Setup
1. **Navigate** to:  
   `Identity Providers` → `Your Provider` → `Mappers` → `Add Mapper`
2. **Select**:  
   `Advanced Attribute Mapper` from dropdown
3. **Configure** parameters:

| Parameter        | Type    | Description                     | Example Value                |
|------------------|---------|---------------------------------|------------------------------|
| `Claim Name`     | String  | Source claim path               | `email`, `address.city`      |
| `User Attribute` | String  | Target attribute                | `custom_role`                |
| `Value Mappings` | JSON    | Value transformations           | `{"admin":"superuser"}`      |
| `Default Value`  | String  | Fallback value                  | `guest`                      |
| `Enable Regex`   | Boolean | Use regex patterns              | `true`                       |

## 📋 Examples

**Basic Value Mapping:**
```json
{
  "external_admin": "internal_admin",
  "external_user": "basic_user",
  "external_.*: "matched_by_regex_user"
}
```


📜 License
Distributed under the MIT License. See LICENSE for more information.
