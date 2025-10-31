package com.example.crudprodukapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * FileUtil - Utility class untuk handling file dan URI
 * Digunakan untuk Firebase Storage upload
 */
public class FileUtil {

    /**
     * Mendapatkan path file dari URI (untuk API < 29)
     */
    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] projection = { MediaStore.Images.Media.DATA };

        try {
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    result = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Mendapatkan nama file dari URI
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;

        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    /**
     * Mendapatkan ekstensi file dari URI
     */
    public static String getFileExtension(Context context, Uri uri) {
        String extension = null;

        if (uri.getScheme().equals("content")) {
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            }
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    /**
     * Validasi apakah file adalah gambar
     */
    public static boolean isImageFile(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Mendapatkan ukuran file dalam bytes
     */
    public static long getFileSize(Context context, Uri uri) {
        long size = 0;

        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;
    }

    /**
     * Copy file dari URI ke cache directory
     */
    public static File copyUriToCache(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = getFileName(context, uri);
            if (fileName == null) {
                fileName = "temp_" + System.currentTimeMillis() + ".jpg";
            }

            File outputFile = new File(context.getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}