package com.jvmapp.panaderialautaro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Cliente
import com.jvmapp.panaderialautaro.data.Venta
import com.jvmapp.panaderialautaro.fragment.FragmentCliente
import com.jvmapp.panaderialautaro.fragment.FragmentHistorial
import com.jvmapp.panaderialautaro.fragment.FragmentInicio
import kotlinx.coroutines.launch

lateinit var db: AppDatabase
private lateinit var btnMenuClientes : Button

private lateinit var btnMenuHistorial : Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Cargar fragment inicio
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragments, FragmentInicio())
                .commit()
        }

        //INICIAR BASE DE DATOS
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "panaderia_db"
        ).build()

        initView()
        initListener()


    }

    fun initView(){

        btnMenuClientes = findViewById(R.id.btn_menu_clientes)
        btnMenuHistorial = findViewById(R.id.btn_menu_historial)

    }

    fun initListener(){

        btnMenuClientes.setOnClickListener { abrirClientes() }
        btnMenuHistorial.setOnClickListener { abrirHistorial() }

    }

    //FUNSIONES

    fun abrirDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.main)
        drawerLayout.openDrawer(GravityCompat.END)
    }

    fun abrirClientes() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragments, FragmentCliente())
            .addToBackStack(null)
            .commit()
    }

    fun abrirHistorial(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragments, FragmentHistorial())
            .addToBackStack(null)
            .commit()
    }

}