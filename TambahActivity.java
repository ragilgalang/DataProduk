package com.example.crudprodukapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.crudprodukapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TambahActivity extends AppCompatActivity {

    private static final String TAG = "TambahActivity";

    private TextInputEditText etNamaProduk, etHarga;
    private ImageView imgPreviewFoto;
    private Button btnPilihFoto, btnSimpan;
    private ProgressBar progressBar;
    private String selectedBase64Image = "";

    private FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            // Convert image to Base64
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            // Resize untuk menghemat space
                            Bitmap resizedBitmap = resizeBitmap(bitmap, 800);

                            // Convert to Base64
                            selectedBase64Image = bitmapToBase64(resizedBitmap);

                            // Show preview
                            imgPreviewFoto.setImageBitmap(resizedBitmap);
                            imgPreviewFoto.setVisibility(View.VISIBLE);

                            Log.d(TAG, "Foto berhasil dipilih dan dikonversi ke Base64");
                            Toast.makeText(this, "Foto berhasil dipilih", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Log.e(TAG, "Error memproses foto", e);
                            Toast.makeText(this, "Gagal memproses foto", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah); // Pastikan ini ID layout yang benar

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();

        // Inisialisasi UI
        etNamaProduk = findViewById(R.id.et_nama_produk);
        etHarga = findViewById(R.id.et_harga);
        imgPreviewFoto = findViewById(R.id.img_preview_foto);
        btnPilihFoto = findViewById(R.id.btn_pilih_foto);
        btnSimpan = findViewById(R.id.btn_simpan);
        progressBar = findViewById(R.id.progress_bar);

        // Listeners
        btnPilihFoto.setOnClickListener(v -> openFileChooser());
        btnSimpan.setOnClickListener(v -> createProduct());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickImageLauncher.launch(intent);
    }

    private void createProduct() {
        String namaProduk = etNamaProduk.getText().toString().trim();
        String hargaStr = etHarga.getText().toString().trim();

        if (namaProduk.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(this, "Nama dan Harga harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format harga tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSimpan.setEnabled(false);
        btnPilihFoto.setEnabled(false);

        // Simpan produk dengan foto Base64
        saveProductToFirestore(namaProduk, harga, selectedBase64Image);
    }

    private void saveProductToFirestore(String namaProduk, double harga, String fotoBase64) {
        Map<String, Object> produk = new HashMap<>();
        produk.put("namaProduk", namaProduk);
        produk.put("harga", harga);
        produk.put("fotoProduk", fotoBase64 != null ? fotoBase64 : "");

        Log.d(TAG, "Menyimpan produk ke Firestore...");

        db.collection("produk")
                .add(produk)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Produk berhasil disimpan dengan ID: " + documentReference.getId());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Produk berhasil ditambahkan!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error menyimpan produk", e);
                    progressBar.setVisibility(View.GONE);
                    btnSimpan.setEnabled(true);
                    btnPilihFoto.setEnabled(true);
                    Toast.makeText(this, "Gagal menambahkan produk: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // Resize bitmap untuk menghemat space
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min(
                (float) maxSize / width,
                (float) maxSize / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    // Convert Bitmap to Base64
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}