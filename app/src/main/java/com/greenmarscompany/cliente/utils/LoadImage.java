package com.greenmarscompany.cliente.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

public class LoadImage extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewWeakReference;

    public LoadImage(ImageView imageView) {
        imageViewWeakReference = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        return downloadBitMap(strings[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        ImageView imageView = imageViewWeakReference.get();
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private Bitmap downloadBitMap(String url) {
        HttpURLConnection urlConnection = null;
        try {
            java.net.URL uri = new java.net.URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statuscode = urlConnection.getResponseCode();
            if (statuscode != HttpURLConnection.HTTP_OK) {
                return null;
            }
            java.io.InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            assert urlConnection != null;
            urlConnection.disconnect();
            android.util.Log.e("Load image class", "Descargando imagen de la url: " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
