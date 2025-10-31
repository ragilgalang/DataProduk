package com.example.crudprodukapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crudprodukapp.R;
import com.example.crudprodukapp.activity.EditActivity;
import com.example.crudprodukapp.model.Produk;

import java.util.List;
import java.util.Locale;

public class ProdukAdapter extends RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder> {

    private final Context context;
    private final List<Produk> produkList;
    private static final String TAG = "ProdukAdapter"; // Untuk debugging

    public ProdukAdapter(Context context, List<Produk> produkList) {
        this.context = context;
        this.produkList = produkList;
    }

    @NonNull
    @Override
    public ProdukViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produk, parent, false);
        return new ProdukViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdukViewHolder holder, int position) {
        Produk produk = produkList.get(position);

        holder.tvNamaProduk.setText(produk.getNamaProduk());

        // Menampilkan ID Dokumen Firestore (dipotong 8 karakter pertama untuk kerapian)
        String displayId = produk.getId() != null ? produk.getId().substring(0, Math.min(produk.getId().length(), 8)) : "N/A";
        holder.tvNoProduk.setText(String.format(Locale.getDefault(), "ID: %s", displayId));

        // Format harga
        holder.tvHarga.setText(String.format(Locale.getDefault(), "Rp %,.0f", produk.getHarga()));

        // Memuat gambar dari Base64
        if (produk.getFotoProduk() != null && !produk.getFotoProduk().isEmpty()) {
            try {
                Bitmap bitmap = base64ToBitmap(produk.getFotoProduk());
                if (bitmap != null) {
                    holder.imgFotoProduk.setImageBitmap(bitmap);
                } else {
                    // Jika konversi Base64 gagal tapi string tidak kosong
                    holder.imgFotoProduk.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error memproses Base64", e);
                holder.imgFotoProduk.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            // Tampilkan placeholder jika fotoProduk kosong
            holder.imgFotoProduk.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Listener untuk membuka EditActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditActivity.class);
            intent.putExtra("produk_data", produk);
            context.startActivity(intent);
        });

        // Listener untuk tombol aksi (memicu klik pada item view untuk konsistensi)
        holder.btnAksi.setOnClickListener(v -> holder.itemView.performClick());
    }

    @Override
    public int getItemCount() {
        return produkList.size();
    }

    // Convert Base64 to Bitmap
    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            // Menangani error jika string Base64 tidak valid
            Log.e(TAG, "Base64 string is invalid", e);
            return null;
        }
    }

    public static class ProdukViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFotoProduk;
        TextView tvNamaProduk, tvHarga, tvNoProduk;
        ImageButton btnAksi;

        public ProdukViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFotoProduk = itemView.findViewById(R.id.img_foto_produk);
            tvNamaProduk = itemView.findViewById(R.id.tv_nama_produk);
            tvHarga = itemView.findViewById(R.id.tv_harga);
            tvNoProduk = itemView.findViewById(R.id.tv_no_produk);
            btnAksi = itemView.findViewById(R.id.btn_aksi);
        }
    }
}