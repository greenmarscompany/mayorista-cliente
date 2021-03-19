package com.greenmarscompany.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.greenmarscompany.cliente.login.LoginActivity;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.persistence.dao.CartDao;
import com.greenmarscompany.cliente.persistence.entity.Acount;
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
    android.widget.TextView CerrarSecion, lblUsername, lblEmail;

    TextView textCartItemCount;
    int mCartItemCount = 0;

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

        llenarInfoUsuario();
        //-- Cierra la sesión
        CerrarSecion = findViewById(R.id.CerrarSesion);
        CerrarSecion.setOnClickListener(v -> {
            Session session = new Session(getApplicationContext());
            session.destroySession();

            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .getCartDao()
                    .deleteAllCart();

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_actions, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_carrito);

        View actionView = menuItem.getActionView();
        textCartItemCount = actionView.findViewById(R.id.cart_badge);
        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

        llenarBadgeCarrito();
        setupBadge();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_pedidos) {
            Intent intent = new Intent(getBaseContext(), PedidosActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_carrito) {
            Intent intent = new Intent(getApplicationContext(), Cart.class);
            startActivity(intent);
            return true;
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (menuItem.getItemId() == R.id.home) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.Perfil) {
            Intent intent2 = new Intent(getBaseContext(), PerfilActivity.class);
            String title = menuItem.getTitle().toString();
            intent2.putExtra("name", title);
            intent2.putExtra("id", R.id.Perfil);
            startActivity(intent2);
        } else if (menuItem.getItemId() == R.id.MisPedidos) {
            Intent intent3 = new Intent(getBaseContext(), PedidosActivity.class);
            startActivity(intent3);
        } else if (menuItem.getItemId() == R.id.mVaciarCache) {
            deleteCache(getBaseContext());
            Toast.makeText(getBaseContext(), "Caché vaciadas correctamente", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        invalidateOptionsMenu();
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

    private void setupBadge() {
        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void llenarBadgeCarrito() {
        CartDao cartDao = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().getCartDao();
        mCartItemCount = cartDao.getCountCart();
    }

    private void llenarInfoUsuario() {
        Session session = new Session(getApplicationContext());
        final int token = session.getToken();
        CartDao cartDao = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().getCartDao();
        if (token != 0) {
            Acount acount = DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .getAcountDao()
                    .getUser(token);

            if (acount != null) {
                android.view.View view = navigationView.getHeaderView(0);
                lblUsername = view.findViewById(R.id.lblNombreUsuario);
                lblEmail = view.findViewById(R.id.lblEmailUsuario);

                lblUsername.setText(acount.getNombre());
                lblEmail.setText(acount.getEmail());

            } else {
                Toast.makeText(getApplicationContext(), "Error de  loggeado", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }

        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            Log.e(Global.TAG, "llenarInfoUsuario: Las credenciales son invalidas");
        }

    }

}
