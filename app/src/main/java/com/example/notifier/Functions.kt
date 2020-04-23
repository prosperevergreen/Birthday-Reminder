package com.example.notifier

import android.content.Context
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.concurrent.TimeUnit

fun updateData(date: Calendar): Array<Int> {
    val nextBD: Calendar = Calendar.getInstance()
    val todayDate: Calendar = Calendar.getInstance()
    val year: Int = todayDate.get(java.util.Calendar.YEAR) - date.get(java.util.Calendar.YEAR)
    val month: Int =
        todayDate.get(java.util.Calendar.MONTH) - date.get(java.util.Calendar.MONTH)
    val day: Int =
        todayDate.get(java.util.Calendar.DAY_OF_MONTH) - date.get(java.util.Calendar.DAY_OF_MONTH)
    val daysLeft: Int
    val age: Int
    nextBD.set(
        todayDate.get(java.util.Calendar.YEAR),
        date.get(java.util.Calendar.MONTH),
        date.get(java.util.Calendar.DAY_OF_MONTH)
    )

    if (month == 0) {
        if (day <= 0) {
            nextBD.set(Calendar.YEAR, todayDate.get(java.util.Calendar.YEAR))
            val millionSeconds = nextBD.timeInMillis - todayDate.timeInMillis
            daysLeft = TimeUnit.MILLISECONDS.toDays(millionSeconds).toInt()
            age = year
        } else {
            age = year + 1
            nextBD.set(Calendar.YEAR, todayDate.get(java.util.Calendar.YEAR) + 1)
            val millionSeconds = nextBD.timeInMillis - todayDate.timeInMillis
            daysLeft = TimeUnit.MILLISECONDS.toDays(millionSeconds).toInt()
        }
    } else if (month > 0) {
        age = year + 1
        nextBD.set(Calendar.YEAR, todayDate.get(java.util.Calendar.YEAR) + 1)
        val millionSeconds = nextBD.timeInMillis - todayDate.timeInMillis
        daysLeft = TimeUnit.MILLISECONDS.toDays(millionSeconds).toInt()
    } else {

        nextBD.set(Calendar.YEAR, todayDate.get(java.util.Calendar.YEAR))
        val millionSeconds = nextBD.timeInMillis - todayDate.timeInMillis
        daysLeft = TimeUnit.MILLISECONDS.toDays(millionSeconds).toInt()
        age = year
    }

    var result: Array<Int> = arrayOf(
        age,
        daysLeft
    )
    return result
}

fun clearDB(setter: AppCompatActivity, listV: ListView){
    val myDB = setter.openOrCreateDatabase("BirthdayDB", Context.MODE_PRIVATE, null)
    myDB.execSQL("CREATE TABLE IF NOT EXISTS birthdays (surname VARCHAR, name VARCHAR, day INT, month INT, year INT, source VARCHAR, daysleft INT)")
    myDB.execSQL("DROP TABLE IF EXISTS birthdays")
    updateViewFromDB(setter, listV)
    myDB.close()
}

fun logOutVK(setter: AppCompatActivity, listV: ListView) {
    val myDB = setter.openOrCreateDatabase("BirthdayDB", Context.MODE_PRIVATE, null)
    myDB.execSQL("CREATE TABLE IF NOT EXISTS birthdays (surname VARCHAR, name VARCHAR, day INT, month INT, year INT, source VARCHAR, daysleft INT)")
    myDB.execSQL("DELETE FROM birthdays WHERE source = 'vk'")
    updateViewFromDB(setter, listV)
    myDB.close()
}


fun canLoginVK(setter: AppCompatActivity): Boolean {
    val myDB = setter.openOrCreateDatabase("BirthdayDB", Context.MODE_PRIVATE, null)
    myDB.execSQL("CREATE TABLE IF NOT EXISTS birthdays (surname VARCHAR, name VARCHAR, day INT, month INT, year INT, source VARCHAR, daysleft INT)")
    val cursor = myDB.rawQuery("SELECT * FROM birthdays WHERE source = 'vk'", null)
    val length = cursor.count
    cursor.close()
    myDB.close()
    return length == 0
}


fun updateViewFromDB(setter: AppCompatActivity, lv: ListView): MutableList<Model> {
    val list = mutableListOf<Model>()
    val gobalList = Global.Chosen
    try {
        val myDB = setter.openOrCreateDatabase("BirthdayDB", Context.MODE_PRIVATE, null)
        myDB.execSQL("CREATE TABLE IF NOT EXISTS birthdays (surname VARCHAR, name VARCHAR, day INT, month INT, year INT, source VARCHAR, daysleft INT)")
//        myDB.execSQL("DROP TABLE IF EXISTS birthdays")
        val cursor = myDB.rawQuery("SELECT * FROM birthdays ORDER BY daysleft", null)

        val surnameIndex = cursor.getColumnIndex("surname")
        val nameIndex = cursor.getColumnIndex("name")
        val dayIndex = cursor.getColumnIndex("day")
        val monthIndex = cursor.getColumnIndex("month")
        val yearIndex = cursor.getColumnIndex("year")
        val idIndex = cursor.getColumnIndex("mID")


        cursor.moveToFirst()


        if (cursor!!.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val surname = cursor.getString(surnameIndex)
                val name = cursor.getString(nameIndex)
                val date: Calendar = Calendar.getInstance()
                date.set(cursor.getInt(yearIndex), cursor.getInt(monthIndex), cursor.getInt(dayIndex))
//                val rowId = cursor.getInt(idIndex)
                list.add(Model(surname, name, date))
                cursor.moveToNext()
            }
        }
        cursor?.close()
        myDB.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    gobalList.list = list
    lv.adapter = MyAdaptor(setter, R.layout.birthday_row, list)

    return list
}

//Save Birthday to database
fun saveToDB(
    setter: AppCompatActivity,
    surname: String,
    name: String,
    date: Calendar,
    from: String
) {

    val result = updateData(date)
//    age = result[0]
    val daysLeft = result[1].toLong()
    try {
        val myDB = setter.openOrCreateDatabase("BirthdayDB", Context.MODE_PRIVATE, null)
        myDB.execSQL("CREATE TABLE IF NOT EXISTS birthdays (surname VARCHAR, name VARCHAR, day INT, month INT, year INT, source VARCHAR, daysleft INT)")
        val sqlString =
            "INSERT INTO birthdays (surname, name, year, month, day, source, daysleft) VALUES (?, ?, ?, ?, ?, ?, ?)"
        val statement = myDB.compileStatement(sqlString)
        statement.bindString(1, surname)
        statement.bindString(2, name)
        statement.bindLong(3, date.get(java.util.Calendar.YEAR).toLong())
        statement.bindLong(4, date.get(java.util.Calendar.MONTH).toLong())
        statement.bindLong(5, date.get(java.util.Calendar.DAY_OF_MONTH).toLong())
        statement.bindString(6, from)
        statement.bindLong(7, daysLeft)

        statement.execute()
        myDB.close()
        if (from == "local") {
            Toast.makeText(setter, "Successfully Added 1 Reminder", Toast.LENGTH_LONG).show()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(setter, "An error occurred", Toast.LENGTH_LONG).show()
    }
}

fun deleteOne(setter: AppCompatActivity, surname:String?, name:String?, month:Int?, day:Int?){
    val myDB = setter.openOrCreateDatabase("BirthdayDB", Context.MODE_PRIVATE, null)
    myDB.execSQL("CREATE TABLE IF NOT EXISTS birthdays (surname VARCHAR, name VARCHAR, day INT, month INT, year INT, source VARCHAR, daysleft INT)")
    val sqlString =
        "DELETE FROM birthdays WHERE surname = ? AND name = ? and day = ? AND month = ?"
    val statement = myDB.compileStatement(sqlString)
    statement.bindString(1, surname)
    statement.bindString(2, name)
    statement.bindLong(3, day!!.toLong())
    statement.bindLong(4, month!!.toLong())
    statement.execute()
    myDB.close()
}

fun editDB(setter: AppCompatActivity, toEdit: Model, edited:Model){
    val result = updateData(edited.date)
    val daysLeft = result[1].toLong()
    val myDB = setter.openOrCreateDatabase("BirthdayDB", Context.MODE_PRIVATE, null)
    myDB.execSQL("CREATE TABLE IF NOT EXISTS birthdays (surname VARCHAR, name VARCHAR, day INT, month INT, year INT, source VARCHAR, daysleft INT)")
    val sqlString =
        "UPDATE birthdays SET surname = ?, name = ?, day = ?,month = ?, year = ?, daysleft = ? WHERE surname = ? AND name = ? and day = ? AND month = ?"
    val statement = myDB.compileStatement(sqlString)
    statement.bindString(1, edited.surname)
    statement.bindString(2, edited.name)
    statement.bindLong(3, edited.date.get(java.util.Calendar.DAY_OF_MONTH).toLong())
    statement.bindLong(4, edited.date.get(java.util.Calendar.MONTH).toLong())
    statement.bindLong(5, edited.date.get(java.util.Calendar.YEAR).toLong())
    statement.bindLong(6, daysLeft)
    statement.bindString(7, toEdit.surname)
    statement.bindString(8, toEdit.name)
    statement.bindLong(9, toEdit.date.get(java.util.Calendar.DAY_OF_MONTH).toLong())
    statement.bindLong(10, toEdit.date.get(java.util.Calendar.MONTH).toLong())
    statement.execute()
    myDB.close()
}


