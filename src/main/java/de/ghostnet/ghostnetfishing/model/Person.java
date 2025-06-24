package de.ghostnet.ghostnetfishing.model;

import jakarta.persistence.*;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phone;

    private boolean isAnonymous;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        MELDEND,
        BERGEND
    }

    public Person() {
    }

    public Person(String name, String phone, boolean isAnonymous, Role role) {
        this.name = name;
        this.phone = phone;
        this.isAnonymous = isAnonymous;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public Role getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}