package hu.ait.drench

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import hu.ait.drench.databinding.ActivityMainBinding
import hu.ait.drench.leaderboard.LeaderboardActivity
import hu.ait.drench.leaderboard.data.HighScore
import hu.ait.drench.login.LoginActivity
import hu.ait.drench.model.DrenchModel

class MainActivity : AppCompatActivity() {

    companion object {
        const val COLLECTION_USERS = "users"
        const val FIELD_HIGH_SCORE = "highScore"
    }

    lateinit var binding: ActivityMainBinding
    private lateinit var menu: Menu
    private var changeLoginMenuItemTitle = false
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    lateinit var colors: Array<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        colors = arrayOf(
            ContextCompat.getColor(this, R.color.green),
            ContextCompat.getColor(this, R.color.pink),
            ContextCompat.getColor(this, R.color.blue),
            ContextCompat.getColor(this, R.color.cyan),
            ContextCompat.getColor(this, R.color.red),
            ContextCompat.getColor(this, R.color.yellow)
        )

        binding.tvMovesLeft.text = DrenchModel.movesLeft.toString()
        binding.tvScore.text = DrenchModel.startingMoves.toString()

        binding.btnReset.setOnClickListener {
            DrenchModel.startingMoves = DrenchModel.STARTING_MOVES_INIT
            resetGame()
        }
    }

    public override fun onStart() {
        super.onStart()
        currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvUser.text = if (currentUser!!.isAnonymous) "Guest" else currentUser!!.email
            if (!currentUser!!.isAnonymous) {
                changeLoginMenuItemTitle = true
                val firestoreHighScore = downloadHighScore()
                val sharedPrefHighScore = getSharedPrefHighScore()
                if (firestoreHighScore != null && sharedPrefHighScore > firestoreHighScore) {
                    saveHighScore(firestoreHighScore, true)
                } else {
                    uploadHighScore()
                }
            } else {
                uploadHighScore()
            }
        } else {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG_ANON", "signInAnonymously:success")
                        binding.tvUser.text = getString(R.string.guest)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG_ANON", "signInAnonymously:failure", task.exception)
                        binding.tvUser.text = getString(R.string.guest_offline)
                        Snackbar.make(binding.root, "Authentication failed.",
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (changeLoginMenuItemTitle) {
            menu?.findItem(R.id.menuItemLogin)?.title = "Log out"
        }
        this.menu = menu!!
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuItemLeaderboard) {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        } else if (item.itemId == R.id.menuItemHighScore) {
            Snackbar.make(
                binding.root,
                "Your high score is: ${getSharedPrefHighScore()}",
                Snackbar.LENGTH_LONG
            ).show()
        } else if (item.itemId == R.id.menuItemLogin) {
            if (currentUser == null || currentUser!!.isAnonymous)
                startActivity(Intent(this, LoginActivity::class.java))
            else {
                auth.signOut()
                menu.findItem(R.id.menuItemLogin).title = "Log in"
                auth.signInAnonymously()
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG_ANON", "signInAnonymously:success")
                            binding.tvUser.text = getString(R.string.guest)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG_ANON", "signInAnonymously:failure", task.exception)
                            binding.tvUser.text = getString(R.string.guest_offline)
                            Snackbar.make(binding.root, "Authentication failed.",
                                Snackbar.LENGTH_SHORT).show()
                        }
                    }
                saveHighScore(DrenchModel.STARTING_MOVES_INIT, true)
            }
        }

        return true
    }

    fun resetGame() {
        DrenchModel.resetGame()
        binding.drenchView.invalidate()
        binding.tvMovesLeft.text = DrenchModel.movesLeft.toString()
        binding.tvScore.text = DrenchModel.startingMoves.toString()
    }

    fun saveHighScore(score: Int, overwrite: Boolean) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val currentHighScore = sharedPref.getInt(getString(R.string.saved_high_score_key), DrenchModel.STARTING_MOVES_INIT)
        if (score < currentHighScore || overwrite || currentHighScore <= 0) {
            with(sharedPref.edit()) {
                putInt(getString(R.string.saved_high_score_key), score)
                apply()
            }
        }
    }

    private fun getSharedPrefHighScore() : Int {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return DrenchModel.STARTING_MOVES_INIT
        return sharedPref.getInt(getString(R.string.saved_high_score_key), DrenchModel.STARTING_MOVES_INIT)
    }

    fun uploadHighScore() {
        if (currentUser != null) {
            val data = HighScore(currentUser!!.uid, currentUser!!.email ?: "", getSharedPrefHighScore())
            FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(currentUser!!.uid)
                .set(data)
        }
    }

    private fun downloadHighScore() : Int? {
        val docRef = FirebaseFirestore.getInstance().collection(COLLECTION_USERS).document(currentUser!!.uid)
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