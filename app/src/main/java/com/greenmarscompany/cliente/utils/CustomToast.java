package com.greenmarscompany.cliente.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.greenmarscompany.cliente.R;

public class CustomToast {
    // Custom Toast Method
    public void Show_Toast(Context context, android.view.View view, String error) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        android.view.View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) view.findViewById(R.id.toast_root));
        android.widget.TextView text = (android.widget.TextView) layout.findViewById(R.id.toast_error);
        text.setText(error);
        Toast toast = new Toast(context);// Get Toast Context
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);// Set
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        toast.show();
    }
}
