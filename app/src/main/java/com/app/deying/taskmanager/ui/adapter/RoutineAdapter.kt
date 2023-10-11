package com.app.deying.taskmanager.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.deying.taskmanager.R
import com.app.deying.taskmanager.data.model.RoutineModel
import com.app.deying.taskmanager.ui.clickListener.OnTaskItemClicked
import com.app.deying.taskmanager.ui.viewholder.TaskViewHolder


class RoutineAdapter(
    val context: Context,
    val routineList: MutableList<RoutineModel>,
    val listener: OnTaskItemClicked
) : RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(context)
        val view1: View = inflater.inflate(R.layout.routine_item_row, parent, false)
        return TaskViewHolder(view1)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val routineModel = routineList[position]
        holder.setData(routineModel, listener, context)
    }

    override fun getItemCount(): Int {
        return routineList.size
    }
}