package com.greenmarscompany.mayoristacliente;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.MenuItem;
import android.widget.Toast;

import com.greenmarscompany.mayoristacliente.persistence.DatabaseClient;
import com.greenmarscompany.mayoristacliente.persistence.entity.ECart;
import com.greenmarscompany.mayoristacliente.utils.CartChangeColor;
import com.google.android.material.navigation.NavigationView;

import java.io.File;

public class InicioMapsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener, CategoriesFragment.OnFragmentInteractionListener,
        BrandsFragment.OnFragmentInteractionListener, NewMapsFragment.OnFragmentInteractionListener,
        ProductNewFragment.OnFragmentInteractionListener, GasNewFragment.OnFragmentInteractionListener,
        GasDetailFragment.OnFragmentInteractionListener, ProductDetailFragment.OnFragmentInteractionListener {

    androidx.appcompat.widget.Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;


    com.google.android.material.floatingactionbutton.FloatingActionButton flo_order_pedido;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciomaps);

        toolbar = findViewById(R.id.navigationToolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigationDrawermaps);
        navigationView = findViewById(R.id.navigationView);

        // estaclecer el evento onclick de navigation
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        //iniciando el frgament de los mapas y los condejos o sugerencias
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.navigationContainer, new MainFragment());
        transaction.commit();


        CartChangeColor.flo_cart = findViewById(R.id.fad_cart_order);
        CartChangeColor.badge_count_cart = findViewById(R.id.badge_count_cart);

        CartChangeColor.flo_cart.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent intent = new Intent(getApplicationContext(), Cart.class);
                startActivity(intent);
                //finish();
            }
        });

        flo_order_pedido = findViewById(R.id.siquiente_pedidos);
        flo_order_pedido.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent intent = new Intent(getBaseContext(), PedidosActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        java.util.List<ECart> cartDetails = DatabaseClient.getInstance(this)
                .getAppDatabase()
                .getCartDao()
                .getCarts();
        if (cartDetails.size() > 0) {
            CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.parseColor("#64dd17")));
            CartChangeColor.badge_count_cart.setVisibility(android.view.View.VISIBLE);
            CartChangeColor.badge_count_cart.setText(DatabaseClient.getInstance(this)
                    .getAppDatabase()
                    .getCartDao()
                    .getCarts().size() + "");
        } else {
            CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.parseColor("#065FD3")));
            CartChangeColor.badge_count_cart.setVisibility(android.view.View.INVISIBLE);
        }


    }

    @Override
    public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        switch (menuItem.getItemId()) {
            case R.id.home:
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                
                break;
            case R.id.account:
                Intent intent1 = new Intent(getBaseContext(), PerfilActivity.class);
                intent1.putExtra("id", R.id.account);
                startActivity(intent1);
                Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
                break;
            case R.id.Perfil:
                Intent intent2 = new Intent(getBaseContext(), PerfilActivity.class);
                String title = menuItem.getTitle().toString();
                intent2.putExtra("name", title);
                intent2.putExtra("id", R.id.Perfil);
                startActivity(intent2);
                return true;
            case R.id.MisPedidos:
                Intent intent3 = new Intent(getBaseContext(), PedidosActivity.class);
                startActivity(intent3);
                break;

            case R.id.mVaciarCache:
                deleteCache(getBaseContext());
                Toast.makeText(getBaseContext(), "Caché vaciadas correctamente", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    //-- Vaciar las caches
    public void deleteCache(Context context) {
        try {
            File file = context.getCacheDir();
            deleteDir(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDir(File file) {
        if (file != null && file.isDirectory()) {
            String[] children = file.list();
            if (children == null) return false;
            for (String child : children) {
                boolean success = deleteDir(new File(file, child));
                if (!success) {
                    return false;
                }
            }
            return file.delete();
        } else if (file != null && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }

}
