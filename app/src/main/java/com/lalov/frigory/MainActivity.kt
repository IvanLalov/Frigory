package com.lalov.frigory

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializamos el motor de Firebase
        mAuth = FirebaseAuth.getInstance()

        // Referencias a los componentes del XML
        val emailEdit = findViewById<EditText>(R.id.emailEditText)
        val passwordEdit = findViewById<EditText>(R.id.passwordEditText)
        val btnLogin = findViewById<Button>(R.id.loginButton)

        btnLogin.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Intento de login en Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "¡Bienvenido a Frigory!", Toast.LENGTH_SHORT).show()
                            // Aquí es donde luego saltaremos a la pantalla de la nevera
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Rellena los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}