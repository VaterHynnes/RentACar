package de.rentacar.shared.security;

/**
 * Rollen für RBAC (NFR3, NFR4)
 */
public enum Role {
    ROLE_CUSTOMER("Kunde"),
    ROLE_EMPLOYEE("Mitarbeiter"),
    ROLE_ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

