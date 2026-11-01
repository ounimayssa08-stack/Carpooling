package com.carpooling.model;

import com.carpooling.enums.Role;
import com.carpooling.enums.UserStatus;
import java.util.Objects;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private UserStatus status;
    private int failedAttempts;

    public User() {}

    public User(int id, String name, String email, String password,
                Role role, UserStatus status, int failedAttempts) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.failedAttempts = failedAttempts;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return id == u.id && Objects.equals(email, u.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email
                + "', role=" + role + ", status=" + status + "}";
    }
}
