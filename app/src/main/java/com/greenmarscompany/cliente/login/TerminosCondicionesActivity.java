package com.greenmarscompany.cliente.login;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;

import com.greenmarscompany.cliente.R;

public class TerminosCondicionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminos_condiciones);

        Button regresar = findViewById(R.id.regresar);
        regresar.setOnClickListener(v -> onBackPressed());
    }
}
