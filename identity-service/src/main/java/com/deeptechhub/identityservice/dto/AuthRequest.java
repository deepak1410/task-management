package com.deeptechhub.identityservice.dto;

import lombok.Data;


public record AuthRequest(String username, String password) {
}
