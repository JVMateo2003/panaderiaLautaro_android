package com.jvmapp.panaderialautaro.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jvmapp.panaderialautaro.MainActivity
import com.jvmapp.panaderialautaro.R

class FragmentInicio : Fragment(R.layout.fragment_inicio) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.btnMenu).setOnClickListener {
            (activity as MainActivity).abrirDrawer()
        }

        view.findViewById<Button>(R.id.btnCargaProduccion).setOnClickListener {
            (activity as MainActivity).abrirFragment(FragmentProduccion())
        }

        view.findViewById<Button>(R.id.btnCargarVentas).setOnClickListener {
            (activity as MainActivity).abrirFragment(FragmentVenta())
        }

        view.findViewById<Button>(R.id.btnResumen).setOnClickListener {
            (activity as MainActivity).abrirFragment(FragmentResumen())
        }
    }
}