package com.kv.connectify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.kv.connectify.databinding.CustomSpinnerCountryItemsBinding


class LocalizeAdapter(val context: Context, val flags: IntArray, val countryNames: Array<String>): BaseAdapter() {

    private var inflater: LayoutInflater? = null

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return flags.size
    }

    override fun getItem(position: Int): Any {
        return countryNames[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding = CustomSpinnerCountryItemsBinding.inflate(inflater!!)
        binding.imageView.setImageResource(flags[position])
        binding.textView.text = countryNames[position]
        return binding.root
    }

}