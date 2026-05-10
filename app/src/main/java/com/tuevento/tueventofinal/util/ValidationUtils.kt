package com.tuevento.tueventofinal.util

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isUdecEmail(email: String): Boolean {
        return email.endsWith("@ucundinamarca.edu.co", ignoreCase = true)
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }
    
    fun isNotBlank(value: String): Boolean = value.isNotBlank()
}
