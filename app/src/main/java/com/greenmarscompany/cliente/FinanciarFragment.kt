package com.greenmarscompany.cliente

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.greenmarscompany.cliente.adapters.FinanciarAdapter
import com.greenmarscompany.cliente.models.Financiar

class FinanciarFragment : Fragment() {

    private lateinit var rvBancos: RecyclerView
    private lateinit var financiarAdapter: FinanciarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_financiar, container, false)

        //--
        rvBancos = view.findViewById(R.id.rvBancos)
        rvBancos.layoutManager = GridLayoutManager(context, 2)

        llenarDatos();

        return view
    }

    private fun llenarDatos() {
        val listFinanciar: ArrayList<Financiar> = ArrayList()
        listFinanciar.add(Financiar(1, "Banco de la nacion", "banco-nacion"))
        listFinanciar.add(Financiar(2, "Banbif", "banbif"))
        listFinanciar.add(Financiar(3, "BBVA", "bbva"))
        listFinanciar.add(Financiar(4, "BCP", "bcp"))
        listFinanciar.add(Financiar(5, "Caja Arequipa", "caja-arequipa"))
        listFinanciar.add(Financiar(6, "Caja Cusco", "caja-cusco"))
        listFinanciar.add(Financiar(7, "Interbank", "interbank"))
        listFinanciar.add(Financiar(8, "Scotiabank", "scotiabank"))

        financiarAdapter = FinanciarAdapter(context!!, listFinanciar)
        rvBancos.adapter = financiarAdapter
        financiarAdapter.setOnCliclListener {
            Toast.makeText(
                context,
                "Mas adelante podras financiar con: ${
                    listFinanciar[rvBancos.getChildAdapterPosition(it)].name
                }",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}