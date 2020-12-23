package com.example.jobsdev.maincontent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import com.example.jobsdev.R
import com.example.jobsdev.databinding.ActivityMainContentBinding
import com.example.jobsdev.maincontent.fragment.*
import com.example.jobsdev.sharedpreference.Constant
import com.example.jobsdev.sharedpreference.PreferencesHelper

class MainContentActivity : AppCompatActivity() {

    private lateinit var sharedPref : PreferencesHelper

    private lateinit var binding : ActivityMainContentBinding
    private lateinit var homeFragment : HomeFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var messageFragment: MessageFragment
    private lateinit var accountEngineerFragment: AccountEngineerFragment
    private lateinit var accountCompanyFragment : AccountCompanyFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_content)

        sharedPref = PreferencesHelper(this)

        setSupportActionBar(binding.toolbar)

        homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fl_container, homeFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()

        binding.bottomNavMenu.setOnNavigationItemSelectedListener{
            item -> when(item.itemId) {
            R.id.home -> {
                homeFragment = HomeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fl_container, homeFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                binding.tvTitleToolbar.setText("Home")
            }
            R.id.search -> {
                searchFragment = SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fl_container, searchFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                binding.tvTitleToolbar.setText("Search")
            }
            R.id.list -> {
                messageFragment = MessageFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fl_container, messageFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                if(sharedPref.getValueInt(Constant.acLevel) == 0) {
                    binding.tvTitleToolbar.setText("List Hire")
                } else if(sharedPref.getValueInt(Constant.acLevel) == 1) {
                    binding.tvTitleToolbar.setText("List Project")
                }
            }
            R.id.account -> {
                accountEngineerFragment = AccountEngineerFragment()
                accountCompanyFragment = AccountCompanyFragment()

                if(sharedPref.getValueInt(Constant.acLevel) == 0) {
                    supportFragmentManager.beginTransaction().replace(R.id.fl_container, accountEngineerFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                } else if(sharedPref.getValueInt(Constant.acLevel) == 1) {
                    supportFragmentManager.beginTransaction().replace(R.id.fl_container, accountCompanyFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                }
                binding.tvTitleToolbar.setText("Profile")
            }
        }
            true
        }

    }
}