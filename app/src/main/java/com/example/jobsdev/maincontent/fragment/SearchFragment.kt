package com.example.jobsdev.maincontent.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobsdev.R
import com.example.jobsdev.databinding.FragmentSearchBinding
import com.example.jobsdev.maincontent.dataclass.ItemEngineerDataClass
import com.example.jobsdev.maincontent.hireengineer.DetailEngineerActivity
import com.example.jobsdev.maincontent.listengineer.DetailEngineerModel
import com.example.jobsdev.maincontent.listengineer.EngineerApiService
import com.example.jobsdev.maincontent.listengineer.ListEngineerAdapter
import com.example.jobsdev.maincontent.listengineer.ListEngineerResponse
import com.example.jobsdev.maincontent.recyclerview.OnListEngineerClickListener
import com.example.jobsdev.maincontent.recyclerview.RecyclerViewListEngineerAdapter
import com.example.jobsdev.remote.ApiClient
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment(), OnListEngineerClickListener {

    private lateinit var binding : FragmentSearchBinding
    private var listEngineer = ArrayList<DetailEngineerModel>()
    private var displayList = ArrayList<DetailEngineerModel>()
    private lateinit var coroutineScope : CoroutineScope

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

        getListEngineer()
//        displayList.addAll(listEngineer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewSearchEngineer.adapter = ListEngineerAdapter(listEngineer,this)
        binding.recyclerViewSearchEngineer.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        if(binding.svSearch != null) {
            var searchView = binding.svSearch

            searchView.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.isNotEmpty()) {
                        displayList.clear()

                        var search = newText.toLowerCase(Locale.getDefault())

                        for (engineer in listEngineer) {
                            if (engineer.engineerName!!.toLowerCase(Locale.getDefault())!!.contains(search)) {
                                displayList.add(engineer)
                            }
                            binding.recyclerViewSearchEngineer.adapter!!.notifyDataSetChanged()
                        }
                    } else {
                        displayList.clear()
                        displayList.addAll(listEngineer)
                        binding.recyclerViewSearchEngineer.adapter!!.notifyDataSetChanged()
                    }
                    return true
                }

            })

        }

    }

    fun getListEngineer() {
        val service = ApiClient.getApiClient(requireContext())?.create(EngineerApiService::class.java)

        coroutineScope.launch {
            Log.d("listengineer", "Start: ${Thread.currentThread().name}")

            val response = withContext(Dispatchers.IO) {
                Log.d("listengineer", "CallApi: ${Thread.currentThread().name}")

                try {
                    service?.getAllEngineer()
                } catch (e:Throwable) {
                    e.printStackTrace()
                }
            }

            Log.d("listengineer Response", response.toString())

            if(response is ListEngineerResponse) {
                val list = response.data?.map {
                    DetailEngineerModel(it.engineerId, it.accountId, it.accountName, it.accountEmail, it.accountPhoneNumber, it.engineerJobTitle, it.engineerJobType, it.engineerLocation, it.engineerDescription, it.engineerProfilePict, it.skillEngineer)
                }
                (binding.recyclerViewSearchEngineer.adapter as ListEngineerAdapter).addListEngineer(list)

            }
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onEngineerItemClicked(position: Int) {
        Toast.makeText(requireContext(), "${listEngineer[position].engineerName} clicked", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), DetailEngineerActivity::class.java)
        intent.putExtra("name", listEngineer[position].engineerName)
        intent.putExtra("jobTitle", listEngineer[position].engineerJobTitle)
        intent.putExtra("jobType", listEngineer[position].engineerJobType)
        intent.putExtra("image", listEngineer[position].engineerProfilePict)
        intent.putExtra("email", listEngineer[position].engineerEmail)
        intent.putExtra("location", listEngineer[position].engineerLocation)
        intent.putExtra("description", listEngineer[position].engineerDescription)

        startActivity(intent)
    }
}