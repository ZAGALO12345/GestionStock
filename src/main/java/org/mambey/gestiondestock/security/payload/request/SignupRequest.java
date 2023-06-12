package org.mambey.gestiondestock.security.payload.request;

import java.util.Set;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    
    private String username;

    private String email;

    private String password;

    private Set<String> roles;

}
