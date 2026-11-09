package com.futapp.controller;

import com.futapp.dto.CreateOrganizerRequestDTO;
import com.futapp.dto.OrganizerInvitationDTO;
import com.futapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/organizers")
public class AdminOrganizerController {
    private final AuthService auth;

    public AdminOrganizerController(AuthService auth) { this.auth = auth; }

    @PostMapping
    public OrganizerInvitationDTO create(@Valid @RequestBody CreateOrganizerRequestDTO request) {
        return auth.createOrganizer(request);
    }
}
