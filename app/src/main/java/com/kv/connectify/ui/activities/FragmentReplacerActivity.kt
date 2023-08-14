package com.kv.connectify.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kv.connectify.databinding.ActivityFragmentReplacerBinding
import com.kv.connectify.ui.fragments.Comment
import com.kv.connectify.ui.fragments.CreateAccountFragment
import com.kv.connectify.ui.fragments.LoginFragment

class FragmentReplacerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFragmentReplacerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentReplacerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isComment = intent.getBooleanExtra("", false)

        if (isComment) {
            setFragment(Comment())
        } else {
            setFragment(LoginFragment())
        }
    }

    fun setFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (fragment is CreateAccountFragment) {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.replace(binding.frameLayout.id, fragment)
        fragmentTransaction.commit()
    }
}