package club.dreamccc.gamecenter.service

import club.dreamccc.gamecenter.model.GameTask
import org.springframework.stereotype.Service

@Service
class GameTaskService {

    fun getGameTaskListByPlayerId(playerId: String): List<GameTask> {
        val task = GameTask()
        task.taskName = playerId

        return  arrayListOf(task)
    }

    fun getGameTaskListAll(): List<GameTask> {

        return emptyList()
    }
}