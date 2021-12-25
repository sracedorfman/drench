# Drench
This is a simple Android app I made of the popular web game [Drench](http://flashbynight.com/drench/). Drench is a super simple game where the user is tasked with covering the board all in one color within a certain number of moves by starting in the top left corner and selecting one of six colors to change to.
## Implementation
The app utilizes two custom views for the game board and the game controls. The app also uses Firebase for user authorization, and Firestore is used to store high scores. A FirestoreRecyclerView is used to display a leaderboard.