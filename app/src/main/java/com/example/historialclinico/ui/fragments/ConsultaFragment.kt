package com.example.historialclinico.ui.fragments
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.historialclinico.data.models.Paciente
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.historialclinico.R

class ConsultaFragment : Fragment() {

    private var paciente: Paciente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paciente = arguments?.getSerializable("paciente") as? Paciente
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_consulta, container, false)
    }
}