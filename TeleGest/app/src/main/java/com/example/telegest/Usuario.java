package com.example.telegest;

import com.google.firebase.Timestamp;

public class Usuario {
    private String uid;
    private String email;
    private String nombre;
    private String photoUrl;
    private String provider;
    private Timestamp fechaCreacion;

    public Usuario() {
        // Constructor vacío requerido para Firestore
        this.fechaCreacion = Timestamp.now();
    }

    public Usuario(String uid, String email, String nombre, String photoUrl, String provider) {
        this.uid = uid;
        this.email = email;
        this.nombre = nombre;
        this.photoUrl = photoUrl;
        this.provider = provider;
        this.fechaCreacion = Timestamp.now();
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
