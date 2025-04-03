package com.example.quiz

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.model.User

class UsersAdapter(
    private val users: List<User>,
    private val onAddPenguins: (Int, Int) -> Unit,
    private val onRemovePenguins: (Int, Int) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textEmail: TextView = view.findViewById(R.id.textViewUserEmail)
        val textPenguins: TextView = view.findViewById(R.id.textViewUserPenguins)
        val addPenguinsButton: Button = view.findViewById(R.id.buttonAddPenguins)
        val removePenguinsButton: Button = view.findViewById(R.id.buttonRemovePenguins)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_penguins, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.textEmail.text = user.email
        holder.textPenguins.text = "Пингвины: ${user.penguins}"
        
        holder.addPenguinsButton.setOnClickListener {
            showAmountDialog(it.context, user.id, true)
        }
        
        holder.removePenguinsButton.setOnClickListener {
            showAmountDialog(it.context, user.id, false)
        }
    }
    
    private fun showAmountDialog(context: android.content.Context, userId: Int, isAdding: Boolean) {
        val editText = EditText(context)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        
        val title = if (isAdding) "Добавить пингвины" else "Забрать пингвины"
        val message = if (isAdding) "Введите количество пингвинов для добавления:" else "Введите количество пингвинов для списания:"
        
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                try {
                    val amount = editText.text.toString().toInt()
                    if (amount <= 0) {
                        Toast.makeText(context, "Введите положительное число", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    if (isAdding) {
                        onAddPenguins(userId, amount)
                    } else {
                        onRemovePenguins(userId, amount)
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "Введите корректное число", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun getItemCount() = users.size
}