package com.example.notifier

import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import co.metalab.asyncawait.async
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.requests.VKRequest
import com.vk.sdk.sample.models.VKUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_birthday_local.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.birthDayListView)
        val now = Calendar.getInstance()
        val setSelectedGlobal = Global.Chosen

        if (setSelectedGlobal.returnCanDelete()) {
            val person = setSelectedGlobal.returnSelected()
            deleteOne(
                this,
                person.surname,
                person.name,
                person.date.get(java.util.Calendar.MONTH),
                person.date.get(java.util.Calendar.DAY_OF_MONTH)
            )
            updateViewFromDB(this, listView)
            Toast.makeText(applicationContext, "Deleted", Toast.LENGTH_LONG).show()
            setSelectedGlobal.canDelete = false
        }

        if (setSelectedGlobal.returnCanEdit()) {
            editDB(this, setSelectedGlobal.returnSelected(), setSelectedGlobal.returnToEdit())
            updateViewFromDB(this, listView)
            setSelectedGlobal.canEdit = false
        }

        var list: MutableList<Model>

        fabGoToAddVK.setOnClickListener {
            if (canLoginVK(this)) {
                try {
                    VK.login(this, arrayListOf(VKScope.FRIENDS, VKScope.STATS))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "Logout First", Toast.LENGTH_LONG).show()
            }
        }


        fabGoToAddLocal.setOnClickListener {
            Toast.makeText(this, "Add New Birthday Reminder", Toast.LENGTH_LONG).show()

            val mDialogue = LayoutInflater.from(this).inflate(R.layout.add_birthday_local, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogue)
                .setTitle("Add New Birthday")
            val mAlertDialog = mBuilder.show()
            val selectedDate: Calendar = Calendar.getInstance()

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
                } else {
                    val myFormat = "dd/MM/yyyy" // mention the format you need
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    mDialogue.viewDate.text = sdf.format(selectedDate.time)
                }
            }

            mDialogue.fabAddLocalUser.setOnClickListener {
                val name = mDialogue.addNameLocalInput.text.toString()
                val surname = mDialogue.addSurnameLocalInput.text.toString()
                if (mDialogue.switch_year.isChecked) {
                    selectedDate.set(Calendar.YEAR, 0) // mention the format you need
                }
                saveToDB(this, surname, name, selectedDate, "local")
                updateViewFromDB(this, listView)
                mAlertDialog.dismiss()
            }
        }
        updateViewFromDB(this, listView)
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(applicationContext, ViewBirthday::class.java)
            list = setSelectedGlobal.returnList()
            val selectedListItem = list[position]
            setSelectedGlobal.selectedUser = selectedListItem
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                println("Signed in success: $token")
                // User passed authorization

                VK.execute(VKFriendsRequest(), object : VKApiCallback<List<VKUser>> {
                    override fun success(result: List<VKUser>) {

                        val listView = findViewById<ListView>(R.id.birthDayListView)
                        async {
                            result.forEachIndexed { _, element ->
                                if (element.bdate == "" || element.bdate.isEmpty()) {
                                    //do nothing
                                } else {
                                    val birthday = element.bdate.split(".")
                                    val surname = element.lastName
                                    val name = element.firstName
                                    val selectedDate = Calendar.getInstance()
                                    val source = "vk"
                                    selectedDate.set(0, birthday[1].toInt(), birthday[0].toInt())
                                    if (birthday.size == 2) {
                                        saveToDB(
                                            this@MainActivity,
                                            surname,
                                            name,
                                            selectedDate,
                                            source
                                        )
//                                    println("${index}. ${element.lastName} ${element.firstName} ${element.bdate} from two")
                                    } else {
                                        selectedDate.set(Calendar.YEAR, birthday[2].toInt())
                                        saveToDB(
                                            this@MainActivity,
                                            surname,
                                            name,
                                            selectedDate,
                                            source
                                        )
//                                    println("${index}. ${element.lastName} ${element.firstName} ${element.bdate} from three")
                                    }
                                }
                            }
                        }
                        updateViewFromDB(this@MainActivity, listView)
                        Toast.makeText(
                            this@MainActivity,
                            "Successfully Added ${result.size} Reminders",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun fail(error: VKApiExecutionException) {
                    }
                })
            }

            override fun onLoginFailed(errorCode: Int) {
                // User didn't pass authorization
                println("Signed in unsuccess")
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }


    }

    class SampleApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            VK.addTokenExpiredHandler(tokenTracker)
        }

        private val tokenTracker = object : VKTokenExpiredHandler {
            override fun onTokenExpired() {
                // token expired
            }
        }
    }

    class VKFriendsRequest(uid: Int = 0) : VKRequest<List<VKUser>>("friends.get") {
        init {
            if (uid != 0) {
                addParam("user_id", uid)
            }
            addParam("fields", "bdate")
        }

        override fun parse(r: JSONObject): List<VKUser> {
            val users = r.getJSONObject("response").getJSONArray("items")
            val result = ArrayList<VKUser>()
            for (i in 0 until users.length()) {
                result.add(VKUser.parse(users.getJSONObject(i)))
            }
            return result
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val listView = findViewById<ListView>(R.id.birthDayListView)
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(applicationContext, "On development", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_clear -> {
                clearDB(this, listView)
                Toast.makeText(applicationContext, "Cleared Database", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.action_exit -> {
                if (!canLoginVK(this)) {
                    Toast.makeText(
                        applicationContext,
                        "Your VK data has been cleared",
                        Toast.LENGTH_LONG
                    ).show()
                    logOutVK(this, listView)
                } else {
                    Toast.makeText(applicationContext, "Login First", Toast.LENGTH_LONG).show()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
