package com.example.notifier

import java.io.Serializable
import java.util.*

class Model(val surname: String, val name: String, val date: Calendar) : Serializable {

    var age: Int
    var daysLeft: Int
    init {
        val result = updateData(date)
        age = result[0]
        daysLeft = result[1]
    }
}
