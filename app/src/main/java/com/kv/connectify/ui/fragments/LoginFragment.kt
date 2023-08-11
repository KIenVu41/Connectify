package com.kv.connectify.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.kv.connectify.R
import com.kv.connectify.databinding.FragmentLoginBinding
import com.kv.connectify.ui.activities.FragmentReplacerActivity
import com.kv.connectify.ui.activities.MainActivity
import com.kv.connectify.utils.Constants
import com.kv.connectify.utils.Utils

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        clickListener()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun clickListener() {
        binding.forgotTV.setOnClickListener(View.OnClickListener {
            (activity as? FragmentReplacerActivity)?.setFragment(ForgotPassword())
        })
        binding.loginBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val email = binding.emailET.text.toString()
                val password = binding.passwordET.text.toString()

                if (email.isEmpty() || !Utils.validateEmail(email)) {
                    binding.emailET.error = activity?.resources?.getString(R.string.invalid_email)
                    return
                }
                if (password.isEmpty() || password.length < 6) {
                    binding.passwordET.error = activity?.resources?.getString(R.string.invalid_password_length)
                    return
                }
                binding.progressBar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                        override fun onComplete(p0: Task<AuthResult>) {
                            if (p0.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    if (!user.isEmailVerified) {
                                        Toast.makeText(activity, activity?.resources?.getString(R.string.verify_email) ?: "", Toast.LENGTH_SHORT).show()
                                    }
                                    sendUserToApp();
                                }
                            } else {
                                val exception = p0.exception?.message.toString()
                                Toast.makeText(activity, activity?.resources?.getString(R.string.error) + exception, Toast.LENGTH_SHORT).show()
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    })
            }
        })
        binding.googleSignInBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                signInWithGoogle()
            }
        })
        binding.signUpTV.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                (activity as? FragmentReplacerActivity)?.setFragment(CreateAccountFragment())
            }
        })
    }

    private fun sendUserToApp() {
        binding.progressBar.visibility = View.GONE
        activity?.let {
            startActivity(Intent(it.applicationContext, MainActivity::class.java))
            it.finish()
        }
    }

    private fun signInWithGoogle() {
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        activity?.let {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(it, object : OnCompleteListener<AuthResult> {
                    override fun onComplete(p0: Task<AuthResult>) {
                        if (p0.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                updateUI(user)
                            }
                        } else {
                            Log.w("TAG", "signInWithCredential:failure", p0.exception);
                        }
                    }
                })
        }
    }

    private fun updateUI(user: FirebaseUser) {
        val account = GoogleSignIn.getLastSignedInAccount(requireActivity())
        val map: Map<String, Any?> = hashMapOf("name" to account?.displayName, "email" to account?.email,
        "profileImage" to account?.photoUrl, "uid" to user.uid)

        FirebaseFirestore.getInstance().collection("Users").document(user.uid)
            .set(map)
            .addOnCompleteListener(OnCompleteListener {
                if (it.isSuccessful()) {
                    binding.progressBar.visibility = View.GONE
                    sendUserToApp();
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(activity, activity?.resources?.getString(R.string.error) + it.exception?.message,
                        Toast.LENGTH_SHORT).show();
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    it.idToken?.let { it1 -> firebaseAuthWithGoogle(it1) }
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun removeListener() {
        if (::binding.isInitialized) {
            binding.signUpTV.setOnClickListener(null)
            binding.googleSignInBtn.setOnClickListener(null)
            binding.loginBtn.setOnClickListener(null)
            binding.forgotTV.setOnClickListener(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListener()
    }

}