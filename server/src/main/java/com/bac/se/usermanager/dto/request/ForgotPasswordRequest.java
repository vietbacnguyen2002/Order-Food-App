package com.bac.se.usermanager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public record ForgotPasswordRequest(String newPassword,String confirmPassword) {

}
