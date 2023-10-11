package com.app.deying.taskmanager.ui.clickListener

import com.app.deying.taskmanager.data.model.RoutineModel


interface OnTaskItemClicked {

    fun onEditClicked(routineModel: RoutineModel)

    fun onDeleteClicked(routineModel: RoutineModel)

    fun onTaskClick(routineModel: RoutineModel)
}