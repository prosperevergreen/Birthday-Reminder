package com.example.notifier

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MyAdaptor(var mCtx: Context, var resources: Int, var items: List<Model>) :
    ArrayAdapter<Model>(mCtx, resources, items) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(resources, null)

        val surnameTextView: TextView = view.findViewById(R.id.surnamePlaceHolder)
        val nameTextView: TextView = view.findViewById(R.id.namePlaceHolder)
        val ageTextView: TextView = view.findViewById(R.id.agePlaceHolder)
        val dayTextView: TextView = view.findViewById(R.id.dayPlaceHolder)
        val iDTextView: TextView = view.findViewById(R.id.rowUserID)

        var mItem: Model = items[position]

        surnameTextView.text = mItem.surname
        nameTextView.text = mItem.name
        if(mItem.age < 200){
            ageTextView.text = "Turns: " + mItem.age.toString()
        }else{
            ageTextView.text = "Turns: Top Secret"
        }
        dayTextView.text = mItem.daysLeft.toString() + " days left"
        return view
    }
}