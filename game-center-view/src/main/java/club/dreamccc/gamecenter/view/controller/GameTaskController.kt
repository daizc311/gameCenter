package club.dreamccc.gamecenter.view.controller

import club.dreamccc.gamecenter.model.GameTask
import club.dreamccc.gamecenter.view.service.GameTaskService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/gameTask")
class GameTaskController(var gameTaskService: GameTaskService) {


    @RequestMapping("/list")
    fun test(playerId: String?): List<GameTask>? {


        return if (playerId == null) gameTaskService.getGameTaskListAll()
        else gameTaskService.getGameTaskListByPlayerId(playerId)
    }

}


