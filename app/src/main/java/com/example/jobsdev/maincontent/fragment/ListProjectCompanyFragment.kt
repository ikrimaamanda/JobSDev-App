 package com.example.jobsdev.maincontent.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobsdev.R
import com.example.jobsdev.databinding.FragmentListProjectCompanyBinding
import com.example.jobsdev.maincontent.projectcompany.*
import com.example.jobsdev.remote.ApiClient
import com.example.jobsdev.sharedpreference.ConstantProjectCompany
import com.example.jobsdev.sharedpreference.PreferencesHelper
import kotlinx.coroutines.*

class ListProjectCompanyFragment : Fragment(), ListProjectAdapter.OnListProjectCompanyClickListener, ListProjectContract.ListProjectView {

    private lateinit var binding : FragmentListProjectCompanyBinding
    private lateinit var coroutineScope : CoroutineScope
    private var listProjectCompany = ArrayList<ProjectCompanyModel>()
    private lateinit var sharedPref : PreferencesHelper
    private var presenter : ListProjectPresenter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list_project_company, container, false)
        coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        val service = ApiClient.getApiClient(requireContext())?.create(ProjectsCompanyApiService::class.java)
        sharedPref = PreferencesHelper(requireContext())
        presenter = ListProjectPresenter(coroutineScope, service, sharedPref)

        binding.rvListProject.adapter = ListProjectAdapter(listProjectCompany, this)
        binding.rvListProject.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        binding.btnAddProjectCompany.setOnClickListener {
            startActivity(Intent(context, AddNewProjectActivity::class.java))
        }

        return binding.root
    }

    private fun View.showOrGone(show: Boolean) {
        visibility = if(show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onProjectCompanyItemClicked(position: Int) {
        sharedPref.putValue(ConstantProjectCompany.projectId, listProjectCompany[position].projectId)
        sharedPref.putValue(ConstantProjectCompany.projectName, listProjectCompany[position].projectName)
        sharedPref.putValue(ConstantProjectCompany.projectDeadline, listProjectCompany[position].projectDeadline)
        sharedPref.putValue(ConstantProjectCompany.projectDesc, listProjectCompany[position].projectDesc)
        sharedPref.putValue(ConstantProjectCompany.projectImage, listProjectCompany[position].projectImage)

        val intent = Intent(requireContext(), DetailProjectCompanyActivity::class.java)
        intent.putExtra("projectName", listProjectCompany[position].projectName)
        intent.putExtra("deadline", listProjectCompany[position].projectDeadline)
        intent.putExtra("createdAt", listProjectCompany[position].projectCreateAt)
        intent.putExtra("updatedAt", listProjectCompany[position].projectUpdateAt)
        intent.putExtra("description", listProjectCompany[position].projectDesc)
        intent.putExtra("image", listProjectCompany[position].projectImage)

        startActivity(intent)
    }

    override fun addListProject(list: List<ProjectCompanyModel>) {
        (binding.rvListProject.adapter as ListProjectAdapter).addListProjectCompany(list)
        binding.rvListProject.showOrGone(true)
        binding.progressBar.showOrGone(false)
        binding.ivEmptyIllustration.showOrGone(false)
        binding.tvEmptyList.showOrGone(false)
    }

    override fun failedAdd(message: String) {
        binding.rvListProject.showOrGone(false)
        if (message == "expired") {
            Toast.makeText(requireContext(), "Please sign in!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
        binding.ivEmptyIllustration.showOrGone(true)
        binding.tvEmptyList.showOrGone(true)
        binding.progressBar.showOrGone(false)
    }

    override fun showProgressBar() {
        binding.rvListProject.showOrGone(false)
        binding.progressBar.showOrGone(true)
        binding.ivEmptyIllustration.showOrGone(false)
        binding.tvEmptyList.showOrGone(false)
    }

    override fun hideProgressBar() {
        binding.progressBar.showOrGone(false)
    }

    override fun onStart() {
        super.onStart()
        presenter?.bindToView(this)
        presenter?.callProjectApiByCnId()
    }

    override fun onStop() {
        super.onStop()
        presenter?.unbind()
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        presenter = null
        super.onDestroy()
    }

}