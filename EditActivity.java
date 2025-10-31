package com.example.crudprodukapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import com.example.crudprodukapp.model.Produk;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = "EditActivity";

    private TextInputEditText etNamaProduk, etHarga;
    private ImageView imgFotoLama, imgPreviewBaru;
    private Button btnGantiFoto, btnUpdate, btnHapus;
    private ProgressBar progressBar;
    private Produk currentProduk;
    private String newBase64Image = "";
    private boolean isImageChanged = false;

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
                            newBase64Image = bitmapToBase64(resizedBitmap);
                            isImageChanged = true;

                            // Show preview
                            imgPreviewBaru.setImageBitmap(resizedBitmap);
                            imgPreviewBaru.setVisibility(View.VISIBLE);

                            Log.d(TAG, "Foto baru berhasil dipilih");
                            Toast.makeText(this, "Foto baru berhasil dipilih", Toast.LENGTH_SHORT).show();

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
        setContentView(R.layout.activity_edit); // Pastikan ini ID layout yang benar

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();

        // Ambil data produk dari Intent
        currentProduk = (Produk) getIntent().getSerializableExtra("produk_data");
        if (currentProduk == null) {
            Toast.makeText(this, "Data produk tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi UI
        etNamaProduk = findViewById(R.id.et_nama_produk_edit);
        etHarga = findViewById(R.id.et_harga_edit);
        imgFotoLama = findViewById(R.id.img_foto_lama);
        imgPreviewBaru = findViewById(R.id.img_preview_baru);
        btnGantiFoto = findViewById(R.id.btn_ganti_foto);
        btnUpdate = findViewById(R.id.btn_update);
        btnHapus = findViewById(R.id.btn_hapus);
        progressBar = findViewById(R.id.progress_bar);

        // Isi form dengan data lama
        loadProductData();

        // Listeners
        btnGantiFoto.setOnClickListener(v -> openFileChooser());
        btnUpdate.setOnClickListener(v -> updateProduct());
        btnHapus.setOnClickListener(v -> confirmDelete());
    }

    private void loadProductData() {
        etNamaProduk.setText(currentProduk.getNamaProduk());
        etHarga.setText(String.format(Locale.getDefault(), "%.0f", currentProduk.getHarga()));

        // Tampilkan foto lama dari Base64
        if (currentProduk.getFotoProduk() != null && !currentProduk.getFotoProduk().isEmpty()) {
            try {
                Bitmap bitmap = base64ToBitmap(currentProduk.getFotoProduk());
                imgFotoLama.setImageBitmap(bitmap);
            } catch (Exception e) {
                imgFotoLama.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgFotoLama.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickImageLauncher.launch(intent);
    }

    private void updateProduct() {
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
        btnUpdate.setEnabled(false);

        // Gunakan foto baru jika ada, atau foto lama
        String fotoToSave = isImageChanged ? newBase64Image : currentProduk.getFotoProduk();
        updateProductToFirestore(namaProduk, harga, fotoToSave);
    }

    private void updateProductToFirestore(String namaProduk, double harga, String fotoBase64) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("namaProduk", namaProduk);
        updates.put("harga", harga);
        updates.put("fotoProduk", fotoBase64 != null ? fotoBase64 : "");

        db.collection("produk")
                .document(currentProduk.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Produk berhasil diupdate!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(this, "Gagal update produk: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Produk")
                .setMessage("Yakin ingin menghapus produk " + currentProduk.getNamaProduk() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteProduct())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteProduct() {
        progressBar.setVisibility(View.VISIBLE);
        btnHapus.setEnabled(false);

        // Hapus document dari Firestore
        db.collection("produk")
                .document(currentProduk.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Produk berhasil dihapus!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnHapus.setEnabled(true);
                    Toast.makeText(this, "Gagal hapus produk: " + e.getMessage(),
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

    // Convert Base64 to Bitmap
    private Bitmap base64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}