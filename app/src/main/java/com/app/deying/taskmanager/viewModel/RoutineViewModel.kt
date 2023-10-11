package com.app.deying.taskmanager.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.deying.taskmanager.data.model.RoutineModel
import com.app.deying.taskmanager.data.model.UserModel
import com.app.deying.taskmanager.data.repository.RoutineRepository

class RoutineViewModel(private val routineRepository: RoutineRepository) : ViewModel() {

    fun addRoutineData(routineModel: RoutineModel) {
        routineRepository.addRoutineToRoom(routineModel)

    }

    fun getRoutines(): LiveData<List<RoutineModel>> {
        return routineRepository.getAllRoutines()
    }

    fun getSearchRoutine(search: String): LiveData<List<RoutineModel>> {
        return routineRepository.getSearchData(search)
    }

    fun updateRoutineData(routineModel: RoutineModel) {
        routineRepository.updateRoutineData(routineModel)
    }

    fun deleteRoutineData(routineModel: RoutineModel) {
        routineRepository.deleteRoutine(routineModel)
    }

    fun checkUserData(email: String, passwd: String) {
        routineRepository.checkUserData(email, passwd)
    }

    fun addNewUserData(userModel: UserModel) {
        routineRepository.addNewUser(userModel)

    }

    fun deleteAllRoutines(){
        routineRepository.deleteAllRoutine()
    }
}