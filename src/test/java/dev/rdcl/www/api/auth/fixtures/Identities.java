package dev.rdcl.www.api.auth.fixtures;

import dev.rdcl.www.api.auth.entities.Identity;

import java.util.UUID;

public class Identities {

    public static final Identity VALID_IDENTITY = Identity.builder()
        .id(UUID.fromString("f277b076-f061-403c-bf7b-266eab926677"))
        .name("John Doe")
        .email("john.doe@example.com")
        .build();

    public static final Identity INVALID_IDENTITY = Identity.builder()
        .id(UUID.fromString("957621e8-f97c-410d-a0b8-80f72427afd4"))
        .name("Jane Doe")
        .email("jane.doe@example.com")
        .build();
}
