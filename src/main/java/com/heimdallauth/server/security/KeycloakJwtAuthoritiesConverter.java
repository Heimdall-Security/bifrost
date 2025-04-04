package com.heimdallauth.server.security;

import com.heimdallauth.server.configuration.HeimdallOauth2ClientConfiguration;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String SCOPE_CLAIM = "scope";

    private final String CLIENT_ID;

    public KeycloakJwtAuthoritiesConverter(HeimdallOauth2ClientConfiguration config) {
        CLIENT_ID = config.getClientId();
    }

    @Override
    public Set<GrantedAuthority> convert(@NonNull Jwt source) {
        Stream<GrantedAuthority> realmRoles = extractRealmRoles(source);
        Stream<GrantedAuthority> resourceAccessRoles = extractResourceAccessRoles(source);
        Stream<GrantedAuthority> scopeClaims = extractScopeClaim(source);
        return Stream.concat(Stream.concat(realmRoles,resourceAccessRoles), scopeClaims).collect(Collectors.toSet());
    }

    private Stream<GrantedAuthority> extractRealmRoles(Jwt authToken){
        Map<String, Object> realmAccess = authToken.getClaimAsMap(REALM_ACCESS_CLAIM);
        return getGrantedAuthorityStream(realmAccess);
    }
    @SuppressWarnings("unchecked")
    private Stream<GrantedAuthority> extractResourceAccessRoles(Jwt authToken){
        Map<String, Object> resourceAccess = authToken.getClaimAsMap(RESOURCE_ACCESS_CLAIM);
        if(resourceAccess == null){
            return Stream.empty();
        }
        Stream<Map.Entry<String, Object>> resourceEntriesStream;
        if(CLIENT_ID != null){
            resourceEntriesStream = resourceAccess.entrySet().stream().filter(entry -> CLIENT_ID.equals(entry.getKey()) && entry.getValue() instanceof Map);
        } else {
            resourceEntriesStream = resourceAccess.entrySet().stream().filter(entry -> entry.getValue() instanceof Map);
        }
        return resourceEntriesStream.flatMap(
                entry -> {
                    Map<String, Object> clientRolesClaims = (Map<String, Object>) entry.getValue();
                    if(clientRolesClaims.isEmpty()){
                        return Stream.empty();
                    }
                    return getGrantedAuthorityStream(clientRolesClaims);
                }
        );
    }

    private Stream<GrantedAuthority> extractScopeClaim(Jwt authToken){
        String scopeClaim = authToken.getClaimAsString(SCOPE_CLAIM);
        if(scopeClaim == null){
            return Stream.empty();
        }
        return Stream.of(scopeClaim.split("\\s+")).map(roleEntry -> String.format("SCOPE_%s", roleEntry)).map(String::toUpperCase).map(SimpleGrantedAuthority::new);
    }
    @NotNull
    @SuppressWarnings("unchecked")
    private Stream<GrantedAuthority> getGrantedAuthorityStream(Map<String, Object> clientRoles) {
        if(clientRoles == null){
            return Stream.empty();
        }
        Collection<String> roles = (Collection<String>) clientRoles.get(ROLES_CLAIM);
        return roles.stream().map(roleString -> String.format("ROLE_%s", roleString)).map(String::toUpperCase).map(SimpleGrantedAuthority::new);
    }
}
