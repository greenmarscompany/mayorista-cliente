package com.greenmarscompany.cliente.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.greenmarscompany.cliente.R
import com.greenmarscompany.cliente.models.Financiar

data class FinanciarAdapter(val context: Context, var financiar: ArrayList<Financiar>) :
    RecyclerView.Adapter<FinanciarAdapter.ViewHolder>(), View.OnClickListener {

    lateinit var onClickListener: View.OnClickListener
    lateinit var listener: View.OnClickListener

    override fun onClick(v: View?) {
        listener.onClick(v)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_financiar, parent, false)
        view.setOnClickListener(this)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(financiar[position])

    override fun getItemCount(): Int = financiar.size

    fun setOnCliclListener(onClickListener: View.OnClickListener) {
        this.listener = onClickListener
    }


    // public static class ViewHolder extends RecyclerView.ViewHolder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val bancoNombre: TextView = itemView.findViewById(R.id.bancoTitle)
        private val imageBanco: ImageView = itemView.findViewById(R.id.bancoImagen)

        fun bind(financiar: Financiar) {
            bancoNombre.text = financiar.name;
            when (financiar.imageUrl) {
                "banco-nacion" -> imageBanco.setImageResource(R.drawable.bbnc)
                "banbif" -> imageBanco.setImageResource(R.drawable.bambif)
                "bbva" -> imageBanco.setImageResource(R.drawable.bbva)
                "bcp" -> imageBanco.setImageResource(R.drawable.bcp)
                "caja-arequipa" -> imageBanco.setImageResource(R.drawable.cja_arequipa)
                "caja-cusco" -> imageBanco.setImageResource(R.drawable.caja_cusco)
                "interbank" -> imageBanco.setImageResource(R.drawable.interbank)
                "scotiabank" -> imageBanco.setImageResource(R.drawable.scotibank)
            }
        }
    }
}

