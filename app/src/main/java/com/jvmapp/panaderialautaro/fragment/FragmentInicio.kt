package com.jvmapp.panaderialautaro.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.GravityCompat
import com.jvmapp.panaderialautaro.MainActivity
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.Venta

lateinit var btnMenu: ImageView
lateinit var btnVenta: Button

class FragmentInicio : Fragment(R.layout.fragment_inicio) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initView(view)
        initListener()

    }

    private fun initView(view: View) {
        btnMenu = view.findViewById(R.id.btnMenu)
        btnVenta = view.findViewById(R.id.btnCargarVentas)

    }

    private fun initListener(){
        btnMenu.setOnClickListener{ (activity as MainActivity).abrirDrawer()}
        btnVenta.setOnClickListener{abrirVentas()}
    }

    private fun abrirVentas(){
        parentFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragments,FragmentVenta())
            .addToBackStack(null)
            .commit()
    }
}