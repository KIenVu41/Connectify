package com.kv.connectify.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kv.connectify.ui.activities.FragmentReplacerActivity
import com.kv.connectify.ui.activities.MainActivity
import com.kv.connectify.R
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
                createAccount(name, email, password)
            }
        }
    }

    private fun createAccount(name: String, email: String, password: String) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(OnCompleteListener<AuthResult> {
                if (it.isSuccessful) {
                    val user = auth?.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener(OnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(activity, activity?.resources?.getString(R.string.verify_email_link), Toast.LENGTH_SHORT).show()
                            }
                        })
                    uploadUser(user, name, email)
                } else {
                    binding.progressBar.visibility = View.GONE
                    val exception = it.exception?.message ?: ""
                    activity?.let {
                        Toast.makeText(it, it.resources?.getString(R.string.error) + exception, Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun uploadUser(user: FirebaseUser?, name: String, email: String) {
        val list = listOf<String>()
        val list1 = listOf<String>()
        val map: Map<String, Any> = hashMapOf("name" to name, "email" to email,
        "profileImage" to " ", "uid" to (user?.uid ?: ""), "status" to " ", "followers" to list, "following" to list1)


        user?.uid?.let {
            FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME).document(it)
                .set(map)
                .addOnCompleteListener(OnCompleteListener {
                    binding.progressBar.visibility = View.GONE
                    if (it.isSuccessful) {
                        startActivity(Intent(activity?.applicationContext, MainActivity::class.java))
                        activity?.finish()
                    } else {
                        val exception = it.exception?.message ?: ""
                        activity?.let {
                            Toast.makeText(it, it.resources?.getString(R.string.error) + exception, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListener()
    }

}