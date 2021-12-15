package hu.ait.drench.leaderboard.adapter

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import hu.ait.drench.R
import hu.ait.drench.databinding.HighScoreRowBinding
import hu.ait.drench.leaderboard.LeaderboardActivity
import hu.ait.drench.leaderboard.data.HighScore

class HighScoreAdapter(
    options: FirestoreRecyclerOptions<HighScore>,
    private var context: Context,
    uid: String
) : FirestoreRecyclerAdapter<HighScore, HighScoreAdapter.ViewHolder>(options) {

    var currentUid: String = uid

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: HighScore) {
        holder.bind(model, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HighScoreRowBinding.inflate(
            (context as LeaderboardActivity).layoutInflater,
            parent, false
        )
        return ViewHolder(binding)
    }

    inner class ViewHolder(private var binding: HighScoreRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(highScore: HighScore, position: Int) {
            binding.tvEmail.text =
                if (highScore.email == "") context.getString(R.string.guest) else formatEmailAsUserName(
                    highScore.email
                )
            binding.tvHighScore.text = highScore.highScore.toString()
            binding.tvRank.text = (position + 1).toString()

            if (highScore.uid == currentUid) {
                binding.root.setCardBackgroundColor(Color.parseColor("#FFE587"))
            }
        }

        private fun formatEmailAsUserName(email: String): CharSequence {
            var s = ""
            for (i in email.indices) {
                if (email[i] == '@')
                    return s
                s += email[i]
            }
            return s
        }
    }
}