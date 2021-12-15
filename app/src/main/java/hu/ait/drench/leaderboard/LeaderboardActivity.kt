package hu.ait.drench.leaderboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import hu.ait.drench.R
import hu.ait.drench.databinding.ActivityLeaderboardBinding
import hu.ait.drench.leaderboard.adapter.HighScoreAdapter
import hu.ait.drench.leaderboard.data.HighScore
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import hu.ait.drench.MainActivity


class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var adapter: HighScoreAdapter
    private var listenerReg: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = getString(R.string.leaderboard)

        val query = FirebaseFirestore.getInstance()
            .collection(MainActivity.COLLECTION_USERS)
            .orderBy(MainActivity.FIELD_HIGH_SCORE)
            .limit(100)
        val options = FirestoreRecyclerOptions.Builder<HighScore>()
            .setQuery(query, HighScore::class.java)
            .build()

        adapter = HighScoreAdapter(options,this, FirebaseAuth.getInstance().currentUser!!.uid)

        binding.recyclerHighScores.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }
}