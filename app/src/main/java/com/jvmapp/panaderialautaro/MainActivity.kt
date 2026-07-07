package com.jvmapp.panaderialautaro

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import com.jvmapp.panaderialautaro.fragment.*

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.main)

        // Mostrar FragmentInicio al arrancar
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragments, FragmentInicio())
                .commit()
        }

        // Botones del menú lateral
        findViewById<Button>(R.id.btn_menu_deudas).setOnClickListener {
            abrirFragment(FragmentDeudas())
            drawerLayout.closeDrawer(GravityCompat.END)
        }
        findViewById<Button>(R.id.btn_menu_clientes).setOnClickListener {
            abrirFragment(FragmentCliente())
            drawerLayout.closeDrawer(GravityCompat.END)
        }
        findViewById<Button>(R.id.btn_menu_historial).setOnClickListener {
            abrirFragment(FragmentHistorial())
            drawerLayout.closeDrawer(GravityCompat.END)
        }
        findViewById<Button>(R.id.btn_menu_precios).setOnClickListener {
            abrirFragment(FragmentPrecios())
            drawerLayout.closeDrawer(GravityCompat.END)
        }
        findViewById<Button>(R.id.btn_menu_trandferencia).setOnClickListener {
            abrirFragment(FragmentTransferencia())
            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    fun abrirDrawer() {
        drawerLayout.openDrawer(GravityCompat.END)
    }

    fun abrirFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragments, fragment)
            .addToBackStack(null)
            .commit()
    }
}