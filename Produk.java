package com.example.crudprodukapp.model;

import java.io.Serializable;

public class Produk implements Serializable {

    // Field tanpa anotasi, kompatibel dengan Firestore.
    // Firestore secara otomatis menggunakan ID ini jika kita setDocumentId
    private String id;
    private String namaProduk;
    private double harga;
    private String fotoProduk; // URL/Base64 foto

    // Constructor kosong (WAJIB untuk Firebase)
    public Produk() {
    }

    // Constructor dengan parameter
    public Produk(String id, String namaProduk, double harga, String fotoProduk) {
        this.id = id;
        this.namaProduk = namaProduk;
        this.harga = harga;
        this.fotoProduk = fotoProduk;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public double getHarga() {
        return harga;
    }

    public String getFotoProduk() {
        return fotoProduk;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public void setFotoProduk(String fotoProduk) {
        this.fotoProduk = fotoProduk;
    }
}