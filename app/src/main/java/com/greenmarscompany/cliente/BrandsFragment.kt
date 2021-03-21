package com.greenmarscompany.cliente

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.toolbox.JsonArrayRequest
import com.greenmarscompany.cliente.login.LoginActivity
import com.greenmarscompany.cliente.adapters.BrandsAdapter
import com.greenmarscompany.cliente.persistence.Session
import com.greenmarscompany.cliente.pojo.Brands
import com.greenmarscompany.cliente.utils.VolleySingleton
import com.todkars.shimmer.ShimmerRecyclerView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*

class BrandsFragment : Fragment() {

    private val urlBase: String = Global.URL_BASE

    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var brandsAdapter: BrandsAdapter
    private lateinit var recyclerView: ShimmerRecyclerView
    private lateinit var brandsList: ArrayList<Brands>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_brands, container, false)

        // get views items
        recyclerView = view.findViewById(R.id.BrandsContainer)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.setItemViewType { layoutManagerType, position ->
            when (layoutManagerType) {
                ShimmerRecyclerView.LAYOUT_GRID ->
                    if (position % 2 == 0) R.layout.list_item_shimmer_grid
                    else R.layout.list_item_shimmer_grid_alternate
                ShimmerRecyclerView.LAYOUT_LIST ->
                    if (position == 0 || position % 2 == 0) R.layout.list_item_shimmer
                    else R.layout.list_item_shimmer_alternate
                else ->
                    if (position == 0 || position % 2 == 0) R.layout.list_item_shimmer
                    else R.layout.list_item_shimmer_alternate
            }
        }
        recyclerView.showShimmer()
        llenarDatos()

        return view
    }

    fun onButtonPressed(uri: Uri) {
        mListener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) mListener = context
        else throw RuntimeException("$context must implement OnFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    // Llenar datos
    private fun llenarDatos() {
        brandsList = arrayListOf()
        val bundle: Bundle? = arguments
        val url: String

        if (bundle != null) {
            // url = this.urlBase + "/api/markes/" + bundle.getInt("idCategory");
            url = "$urlBase/api/markes/${bundle.getInt("idCategory")}"
            if (bundle.getInt("idCategory") == 2) {
                brandsList.add(Brands(1, "Gas Normal", "", "$urlBase/media/images/gas-regular.png"))
                brandsList.add(Brands(2, "Gas Premium", "", "$urlBase/media/images/gas-premium.png"))
                brandsList.add(Brands(3, "Camion", "", "$urlBase/media/images/gas-cisterna.png"))

                brandsAdapter = BrandsAdapter(context!!, brandsList)
                recyclerView.adapter = brandsAdapter

                brandsAdapter.setOnCliclListener {
                    val manager: FragmentManager = activity!!.supportFragmentManager
                    val transaction: FragmentTransaction = manager.beginTransaction()
                    var brandsTitle: String = brandsList[recyclerView.getChildAdapterPosition(it)].name
                    brandsTitle = brandsTitle.toLowerCase(Locale.ENGLISH)
                    Toast.makeText(context, brandsTitle, Toast.LENGTH_SHORT).show()
                    if (brandsTitle == "gas normal" || brandsTitle == "gas premium") {
                        val gasNewFragment = GasFragment()
                        val b = Bundle()
                        if (brandsTitle == "gas normal") {
                            b.putString("type", "gas-normal")
                        } else {
                            b.putString("type", "gas-premium")

                        }
                        gasNewFragment.arguments = b
                        transaction.replace(R.id.mainContainer, gasNewFragment)
                    } else {
                        val gasCisternaFragment = GasCisternaFragment()
                        transaction.replace(R.id.mainContainer, gasCisternaFragment)
                    }
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                return
            }
        } else {
            url = this.urlBase + "/api/markes"
            brandsList.add(Brands(1, "Gas Normal", "", "$urlBase/media/images/gas-regular.png"))
            brandsList.add(Brands(2, "Gas Premium", "", "$urlBase/media/images/gas-premium.png"))
            brandsList.add(Brands(3, "Camion", "", "$urlBase/media/images/gas-cisterna.png"))
        }

        val jsonArray = JSONArray()
        val arrayRequest = JsonArrayRequest(Request.Method.GET, url, jsonArray, { response ->
            try {
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val imageURL = urlBase + obj.getString("image")
                    val brands = Brands(
                            Integer.parseInt(obj.getString("id")),
                            obj.getString("name"),
                            "",
                            imageURL)
                    brandsList.add(brands)
                }

                brandsAdapter = BrandsAdapter(context!!, brandsList)
                recyclerView.adapter = brandsAdapter

                recyclerView.hideShimmer()
                brandsAdapter.setOnCliclListener {
                    val manager = activity!!.supportFragmentManager
                    val transaction = manager.beginTransaction()

                    var brandsTitle = brandsList[recyclerView.getChildAdapterPosition(it)].name
                    brandsTitle = brandsTitle.toLowerCase(Locale.ENGLISH)
                    if (brandsTitle.equals("gas normal") || brandsTitle.equals("gas premium")) {
                        val gasNewFragment = GasFragment()
                        val b = Bundle()
                        if (brandsTitle.equals("gas normal")) {
                            b.putString("type", "gas-normal")
                        } else {
                            b.putString("type", "gas-premium")
                        }
                        gasNewFragment.arguments = b
                        transaction.replace(R.id.mainContainer, gasNewFragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    } else if (brandsTitle.equals("camion")) {
                        val gasCisternaFragment = GasCisternaFragment()
                        transaction.replace(R.id.mainContainer, gasCisternaFragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    } else {
                        val b = Bundle()
                        b.putInt("IdMarke", brandsList[recyclerView.getChildAdapterPosition(it)].id)
                        val productsFragment = ProductsFragment()
                        productsFragment.arguments = b
                        transaction.replace(R.id.mainContainer, productsFragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { error ->
            Log.d("volley get", "error voley $error ")
            val response = error.networkResponse
            if (error is ServerError && response != null) {
                try {
                    // val res = String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"))
                    val res = String(response.data)
                    val obj = JSONObject(res)
                    Log.d("Voley post", obj.toString())
                    val msj = obj.getString("message")
                    Toast.makeText(context, msj, Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    when (e) {
                        is UnsupportedEncodingException, is JSONException -> {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })

        VolleySingleton.getInstance(context).addToRequestQueue(arrayRequest)
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}