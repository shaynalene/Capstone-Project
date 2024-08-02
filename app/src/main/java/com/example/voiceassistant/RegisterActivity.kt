package com.example.voiceassistant

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etContactNumber: EditText
    private lateinit var etEmailAddress: EditText
    private lateinit var btnRegister: Button

    private val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
    }

    @Serializable
    data class RegisterUser(
        @SerialName("id") val id: String,
        @SerialName("username") val userName: String,
        @SerialName("password") val password: String,
        @SerialName("firstname") val firstName: String,
        @SerialName("middlename") val middleName: String,
        @SerialName("lastname") val lastName: String,
        @SerialName("birthday") val birthDate: String,
        @SerialName("contact_number") val contactNumber: String,
        @SerialName("email") val emailAddress: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etLastName = findViewById(R.id.etLastName)
        etBirthDate = findViewById(R.id.editTextDate)
        etContactNumber = findViewById(R.id.editTextPhone)
        etEmailAddress = findViewById(R.id.editTextEmailAddress)
        btnRegister = findViewById(R.id.btnRegister)

        etBirthDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val middleName = etMiddleName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val birthDate = etBirthDate.text.toString().trim()
            val contactNumber = etContactNumber.text.toString().trim()
            val emailAddress = etEmailAddress.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty() && emailAddress.isNotEmpty()) {
                lifecycleScope.launch {
                    registerUser(
                        RegisterUser(
                            id = UUID.randomUUID().toString(),
                            userName = username,
                            password = password,
                            firstName = firstName,
                            middleName = middleName,
                            lastName = lastName,
                            birthDate = birthDate,
                            contactNumber = contactNumber,
                            emailAddress = emailAddress
                        )
                    )
                }
            } else {
                Toast.makeText(this, "Please fill in the required fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = Calendar.getInstance()
                date.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                etBirthDate.setText(sdf.format(date.time))
            },
            year, month, day
        )
        datePickerDialog.show()
    }



    private suspend fun registerUser(user: RegisterUser) {
        try {
            val response = supabase.postgrest.from("accounts_list").insert(user)

            if (response.data != null) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                // You can redirect the user to the login page or another activity here
                finish()
            } else {
                Toast.makeText(this, "Error: Unable to register user.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}
