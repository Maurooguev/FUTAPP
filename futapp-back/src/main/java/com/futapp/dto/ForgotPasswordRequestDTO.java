package com.futapp.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ForgotPasswordRequestDTO(@Email @NotBlank @Size(max=254) String email) {}
