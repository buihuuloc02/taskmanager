package com.app.deying.taskmanager.data.repository

import androidx.lifecycle.LiveData
import com.app.deying.taskmanager.data.model.RoutineModel
import com.app.deying.taskmanager.data.model.UserModel
import com.app.deying.taskmanager.data.roomDB.RoutineDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineRepository(val routineDAO: RoutineDAO) {

    fun addRoutineToRoom(routineModel: RoutineModel) {
        CoroutineScope(Dispatchers.IO).launch {
            routineDAO.addRoutine(routineModel)
        }
    }

    fun getAllRoutines(): LiveData<List<RoutineModel>> {
        return routineDAO.getRoutineData()
    }

    fun getSearchData(search: String): LiveData<List<RoutineModel>> {
        return routineDAO.getSearchData(search)
    }
    fun updateRoutineData(routineModel: RoutineModel) {
        CoroutineScope(Dispatchers.IO).launch {
            routineDAO.updateRoutine(routineModel)
        }
    }

    fun deleteRoutine(routineModel: RoutineModel) {
        CoroutineScope(Dispatchers.IO).launch {
            routineDAO.deleteRoutine(routineModel)
        }
    }

    fun checkUserData(email: String, passwd: String) {
        CoroutineScope(Dispatchers.IO).launch {
            routineDAO.getUserData(email, passwd)
        }
    }

    fun deleteAllRoutine(){
        CoroutineScope(Dispatchers.IO).launch {
            routineDAO.deleteAllRoutine()
        }
    }

    fun addNewUser(userModel: UserModel){
        CoroutineScope(Dispatchers.IO).launch {
            routineDAO.insertUserData(userModel)
        }
    }
}