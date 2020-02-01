package club.dreamccc.gamecenter.view.controller

import club.dreamccc.gamecenter.model.GameTask
import club.dreamccc.gamecenter.service.GameTaskService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/gameTask")
class GameTaskController(var gameTaskService: GameTaskService) {


    @RequestMapping("/test")
    fun test(playerId: String?): List<GameTask>? {


        return if (playerId == null) gameTaskService.getGameTaskListAll()
        else gameTaskService.getGameTaskListByPlayerId(playerId)
    }

}


