package com.app.deying.taskmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.deying.taskmanager.data.repository.RoutineRepository

class RoutineViewModelFactory(private val routineRepository: RoutineRepository)  : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RoutineViewModel(routineRepository) as T
    }

}