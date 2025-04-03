package com.example.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.model.User

class UnverifiedUsersAdapter(
    private val users: List<User>,
    private val onVerifyClick: (Int) -> Unit
) : RecyclerView.Adapter<UnverifiedUsersAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textEmail: TextView = view.findViewById(R.id.textViewUserEmail)
        val textRole: TextView = view.findViewById(R.id.textViewUserRole)
        val verifyButton: Button = view.findViewById(R.id.buttonVerifyUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unverified_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.textEmail.text = user.email
        holder.textRole.text = "Роль: ${user.role}"
        
        holder.verifyButton.setOnClickListener {
            onVerifyClick(user.id)
        }
    }

    override fun getItemCount() = users.size
}