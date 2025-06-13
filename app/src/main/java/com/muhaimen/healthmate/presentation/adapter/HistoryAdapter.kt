package com.muhaimen.healthmate.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.healthmate.data.dataClass.Vitals
import com.muhaimen.healthmate.databinding.HistoryItemBinding

class HistoryAdapter(private var vitalsList: List<Vitals>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(vitals: Vitals) {
            binding.apply {
                // Top date text
                date.text = vitals.date

                // Left column
                steps.text = vitals.steps.toString()


                sleepHours.text = vitals.sleepHours.toString()


                mood.text = vitals.mood

                // Right column

                waterIntake.text = vitals.waterIntake.toString()

                weight.text = vitals.weight.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(vitalsList[position])
    }

    override fun getItemCount(): Int = vitalsList.size

    fun updateList(newList: List<Vitals>) {
        vitalsList = newList
        notifyDataSetChanged()
    }
}
