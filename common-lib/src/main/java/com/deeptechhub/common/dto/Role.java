package com.deeptechhub.common.dto;

public enum Role {
    USER, // Default role
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }

}
