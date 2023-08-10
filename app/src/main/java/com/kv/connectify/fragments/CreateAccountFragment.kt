package com.kv.connectify.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.kv.connectify.FragmentReplacerActivity
import com.kv.connectify.R
import com.kv.connectify.databinding.ActivityFragmentReplacerBinding
import com.kv.connectify.databinding.FragmentCreateAccountBinding
import com.kv.connectify.utils.Constants
import com.kv.connectify.utils.Utils

class CreateAccountFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentCreateAccountBinding
    private var auth: FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        initClickListener()
    }

    private fun initClickListener() {
        if (::binding.isInitialized) {
            binding.loginTV.setOnClickListener(this)
            binding.signUpBtn.setOnClickListener(this)
        }
    }

    private fun removeListener() {
        if (::binding.isInitialized) {
            binding.loginTV.setOnClickListener(null)
            binding.signUpBtn.setOnClickListener(null)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.loginTV -> {
                (activity as? FragmentReplacerActivity)?.setFragment(LoginFragment())
            }
            R.id.signUpBtn -> {
                val name = binding.nameET.text.toString()
                val email = binding.emailET.text.toString()
                val password = binding.passwordET.text.toString()
                val confirmPassword = binding.confirmPassET.text.toString()

                if (name.isEmpty() || name.equals(" ")) {
                    binding.nameET.error = activity?.resources?.getString(R.string.invalid_name) ?: ""
                    return
                }
                if (email.isEmpty() || !Utils.validateEmail(email)) {
                    binding.emailET.error = activity?.resources?.getString(R.string.invalid_email)
                    return
                }
                if (password.isEmpty() || password.length < 6) {
                    binding.passwordET.error = activity?.resources?.getString(R.string.invalid_password)
                    return
                }
                if (!password.equals(confirmPassword)) {
                    binding.confirmPassET.error = activity?.resources?.getString(R.string.password_not_match)
                    return
                }

                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListener()
    }

}