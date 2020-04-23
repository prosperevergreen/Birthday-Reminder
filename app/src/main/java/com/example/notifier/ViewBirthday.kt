package com.example.notifier

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_view_birthday.*
import kotlinx.android.synthetic.main.add_birthday_local.view.*
import kotlinx.android.synthetic.main.view_wish_text.view.*
import org.json.JSONArray
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ViewBirthday : AppCompatActivity() {


    var arrListRelation = arrayListOf<String>()
    var arrListQuote = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_birthday)

        var intent: Intent

//        val person = intent.getSerializableExtra("person") as? Model
        val chosen = Global.Chosen
        val person = chosen.returnSelected()
        val surname = person.surname
        val name = person.name
        val age = person.age
        val day = person.date.get(java.util.Calendar.DAY_OF_MONTH)
        val month = person.date.get(java.util.Calendar.MONTH)
        val year = person.date.get(java.util.Calendar.YEAR)

        textViewName.text = name
        textViewSurname.text = surname

        if (age < 200) {
            textViewDOB.text = "Birthday: $day/$month/$year"
        } else {
            textViewDOB.text = "Birthday: $day/$month"
        }


        buttonBack.setOnClickListener {
            intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        readWishJson()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.edit_delete_db, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val chosen = Global.Chosen
        return when (item.itemId) {
            R.id.action_delete -> {
                chosen.canDelete = true
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_edit -> {
                Toast.makeText(applicationContext, "Clicked edit", Toast.LENGTH_LONG).show()

                val mDialogue = LayoutInflater.from(this).inflate(R.layout.add_birthday_local, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogue)
                    .setTitle("Edit New Birthday")
                val person = chosen.returnSelected()
                val surname = person.surname
                val name = person.name
                val prev = textViewDOB.text.toString().split("Birthday: ")
                val prevDate = prev[1].split("/")
//                println(prevDate.size)
                mDialogue.addNameLocalInput.setText(name)
                mDialogue.addSurnameLocalInput.setText(surname)
                val now: Calendar = Calendar.getInstance()
                val selectedDate: Calendar = Calendar.getInstance()

                if (prevDate.size == 3) {
                    mDialogue.viewDate.text = "${prevDate[0]}.${prevDate[1]}.${prevDate[2]}"
                    now.set(prevDate[2].toInt(),prevDate[1].toInt(),prevDate[0].toInt())
                } else {
                    mDialogue.viewDate.text = "${prevDate[0]}.${prevDate[1]}"
                    mDialogue.switch_year.isChecked = true
                    now.set(prevDate[1].toInt(),prevDate[0].toInt())
                }

                val mAlertDialog = mBuilder.show()



                mDialogue.selectDate.setOnClickListener {
                    DatePickerDialog(
                        this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                            selectedDate.set(year, month, dayOfMonth)
                            var myFormat: String

                            if (mDialogue.switch_year.isChecked) {
                                myFormat = "dd/MM" // mention the format you need
                            } else {
                                myFormat = "dd/MM/yyyy" // mention the format you need
                            }
                            val sdf = SimpleDateFormat(myFormat, Locale.US)
                            mDialogue.viewDate.text = sdf.format(selectedDate.time)
                        },
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                mDialogue.switch_year.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        val myFormat = "dd/MM" // mention the format you need
                        val sdf = SimpleDateFormat(myFormat, Locale.US)
                        mDialogue.viewDate.text = sdf.format(selectedDate.time)
                        textViewDOB.text = sdf.format(selectedDate.time)
                    } else {
                        val myFormat = "dd/MM/yyyy" // mention the format you need
                        val sdf = SimpleDateFormat(myFormat, Locale.US)
                        mDialogue.viewDate.text = sdf.format(selectedDate.time)
                        textViewDOB.text = sdf.format(selectedDate.time)
                    }
                }
                mDialogue.fabAddLocalUser.setOnClickListener {
                    val editedName = mDialogue.addNameLocalInput.text.toString()
                    val editedSurname = mDialogue.addSurnameLocalInput.text.toString()
                    if (mDialogue.switch_year.isChecked) {
                        selectedDate.set(Calendar.YEAR, 0) // mention the format you need
                    }


                    //save new values for update
                    chosen.canEdit = true
                    chosen.editUser = Model(editedSurname, editedName, selectedDate)

                    //set new value on show birthday page
                    textViewName.text = editedName
                    textViewSurname.text = editedSurname

                    mAlertDialog.dismiss()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //to read wishes
    fun readWishJson() {
        var json: String?

        try {
            val inputStream: InputStream = assets.open("wishes.json")
            json = inputStream.bufferedReader().use { it.readText() }

            var jsonArr = JSONArray(json)

            for (i in 0 until jsonArr.length()) {
                var jsonObj = jsonArr.getJSONObject(i)
                arrListRelation.add(jsonObj.getString("relation"))
                arrListQuote.add(jsonObj.getString("quote"))
            }

            val adpt = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrListRelation)

            listViewWish.adapter = adpt

            listViewWish.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val mDialogue = LayoutInflater.from(this).inflate(R.layout.view_wish_text, null)

                    val mBuilder = AlertDialog.Builder(this)
                        .setView(mDialogue)
                        .setTitle("Birthday Wish")

                    mDialogue.wishTextView.text = arrListQuote[position]
                    val mAlertDialog = mBuilder.show()


                    //To copy wish
                    mDialogue.copyWish.setOnClickListener {
                        var clipboard =
                            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        var clip = ClipData.newPlainText("label", arrListQuote[position])
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, "Wish Copied Successfully!!", Toast.LENGTH_LONG).show()
                    }

                    //To close wish window
                    mDialogue.backWish.setOnClickListener {
                        mAlertDialog.dismiss()
                    }


                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
