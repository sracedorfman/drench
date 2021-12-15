package hu.ait.drench.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.drench.MainActivity
import hu.ait.drench.R
import hu.ait.drench.databinding.ActivityLoginBinding
import hu.ait.drench.leaderboard.data.HighScore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener{
            registerUser()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun registerUser() {
        if (isFormValid()) {
            val oldUserIsAnon = FirebaseAuth.getInstance().currentUser!!.isAnonymous
            val oldUserUid = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                binding.etEmail.text.toString(), binding.etPassword.text.toString()
            ).addOnSuccessListener {
                Snackbar.make(binding.root, getString(R.string.msg_user_created), Snackbar.LENGTH_LONG).show()
                if (oldUserIsAnon) {
                    FirebaseFirestore.getInstance()
                        .collection(MainActivity.COLLECTION_USERS)
                        .document(oldUserUid)
                        .set(HighScore(oldUserUid,"", 0))
                    FirebaseFirestore.getInstance()
                        .collection(MainActivity.COLLECTION_USERS)
                        .document(oldUserUid)
                        .delete()
                } else {
                    val highScore = downloadHighScore()
                    if (highScore != null)
                        saveHighScore(highScore)
                }
            }.addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    "Error: ${it.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loginUser(){
        if (isFormValid()){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                binding.etEmail.text.toString(), binding.etPassword.text.toString()
            ).addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
            }.addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    "Login failed: ${it.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun isFormValid(): Boolean {
        return when {
            binding.etEmail.text.isEmpty() -> {
                binding.etEmail.error = getString(R.string.msg_field_empty)
                false
            }
            binding.etPassword.text.isEmpty() -> {
                binding.etPassword.error = getString(R.string.msg_pwd_empty)
                false
            }
            else -> true
        }
    }

    private fun saveHighScore(score: Int) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt(getString(R.string.saved_high_score_key), score)
            apply()
        }
    }

    private fun downloadHighScore() : Int? {
        val docRef = FirebaseFirestore.getInstance().collection(MainActivity.COLLECTION_USERS).document(FirebaseAuth.getInstance().currentUser!!.uid)
        var highScore: Int? = null
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    highScore = document.toObject(HighScore::class.java)?.highScore
                }
            }
        return highScore
    }
}