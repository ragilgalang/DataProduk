package com.example.crudprodukapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crudprodukapp.activity.TambahActivity;
import com.example.crudprodukapp.adapter.ProdukAdapter;
import com.example.crudprodukapp.model.Produk;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProdukAdapter adapter;
    private List<Produk> produkList;
    private FloatingActionButton fabTambah;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Inisialisasi UI
        // Perbaikan ID: Sesuai dengan ID baru di activity_main.xml
        recyclerView = findViewById(R.id.recycler_view);
        fabTambah = findViewById(R.id.fab_tambah);
        progressBar = findViewById(R.id.progress_bar);

        // Setup RecyclerView
        produkList = new ArrayList<>();
        adapter = new ProdukAdapter(this, produkList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Listener FAB untuk tambah produk
        fabTambah.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TambahActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProduk();
    }

    private void loadProduk() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("produk")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    produkList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Produk produk = document.toObject(Produk.class);
                        produk.setId(document.getId());
                        produkList.add(produk);
                    }

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (produkList.isEmpty()) {
                        // Logika untuk menampilkan empty state jika ada
                    }

                    Log.d(TAG, "Berhasil load " + produkList.size() + " produk");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading produk", e);
                });
    }
}