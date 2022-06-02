package com.example.u_farm.profile


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.u_farm.HomeActivity
import com.example.u_farm.MainActivity
import com.example.u_farm.R
import com.example.u_farm.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {
  lateinit var choseAValue:String
    @SuppressLint("ResourceType", "StringFormatInvalid")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        // Inflate the layout for this fragment
        val binding:FragmentProfileBinding=
            DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false)
        val application: Application = requireNotNull(this.activity).application
        val activity: Activity = this.requireActivity()
        val viewModelFactory= ProfileViewModelFactory(application,activity)
        val profileViewModel = ViewModelProvider(this,viewModelFactory).get(ProfileViewModel::class.java)

        binding.profileViewModel=profileViewModel
        binding.lifecycleOwner=this

        profileViewModel.snackbar.observe(viewLifecycleOwner,Observer{
            if(it==true){
                Snackbar.make(binding.root, "Login to access the settings", Snackbar.LENGTH_SHORT)
                    .show()
            }
        })

        profileViewModel.navigateToEditProfile.observe(viewLifecycleOwner, Observer {
            if(it==true) {
                this.findNavController().navigate(ProfileFragmentDirections.actionProfileToEditProfileActivity())
                profileViewModel.navigateToEditProfileDone()
//            }else{
//                profileViewModel.snackbar()
            }
        })
        profileViewModel.expert.observe(viewLifecycleOwner, Observer {
            if(it==true ) {
                expertTick.visibility=View.VISIBLE
            }
        })

        profileViewModel.arguments.observe(viewLifecycleOwner, Observer {
            if(it!=-1) {
            choseAValue=arguments.toString()
                val intent=Intent(application,HomeActivity::class.java)
                startActivity(intent)

                }
        })



        profileViewModel.language.observe(viewLifecycleOwner, Observer {
            if(it==true) {

                profileViewModel.languageIntentDone()
            }
        })

        profileViewModel.getData.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                profileViewModel.function()
            }
        })

        profileViewModel.loggedUser.observe(viewLifecycleOwner, Observer{
            if(it == true){
                val intent= Intent(application, HomeActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)



            }
        })
        profileViewModel.share.observe(viewLifecycleOwner, Observer {
            if(it==true) {
                val something= Uri.parse("http://www.u-farm.com/profile/")
                    .buildUpon()
                    .appendPath("1")

                val shareIntent = androidx.core.app.ShareCompat.IntentBuilder.from(activity)
                    .setText(getString(R.string.share_text, something))
                    .setType("text/plain")
                    .intent
                try {
                    startActivity(shareIntent)
                } catch (ex: android.content.ActivityNotFoundException) {
                    android.widget.Toast.makeText(
                        application, getString(R.string.sharing_not_available),
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                profileViewModel.shareIntentDone()
            }
        })



            return binding.root
    }

}



