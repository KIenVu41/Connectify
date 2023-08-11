package com.kv.connectify.ui.fragments

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.kv.connectify.R
import com.kv.connectify.databinding.FragmentForgotPasswordBinding
import com.kv.connectify.ui.activities.FragmentReplacerActivity
import com.kv.connectify.utils.Utils

class ForgotPassword : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        clickListener()
    }

    private fun clickListener() {
        binding.loginTV.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                (activity as? FragmentReplacerActivity)?.setFragment(LoginFragment())
            }
        })
        binding.recoverBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val email = binding.emailET.text.toString()
                if (email.isEmpty() || !Utils.validateEmail(email)) {
                    binding.emailET.error = activity?.resources?.getString(R.string.invalid_email)
                    return
                }

                binding.progressBar.visibility = View.VISIBLE
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(OnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(activity, activity?.resources?.getString(R.string.password_reset_email), Toast.LENGTH_SHORT).show()
                            binding.emailET.setText("")
                        } else {
                            val errMsg = it.exception?.message ?: ""
                            Toast.makeText(activity, activity?.resources?.getString(R.string.error) + errMsg, Toast.LENGTH_SHORT).show()
                        }
                        binding.progressBar.visibility = View.GONE
                    })
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListener()
    }

    private fun removeListener() {
        binding.emailET.setOnClickListener(null)
        binding.recoverBtn.setOnClickListener(null)
    }

}