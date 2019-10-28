package club.dreamccc.gamecenter.view.controller

import club.dreamccc.gamecenter.model.GameTask
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/gameTask")
class GameTaskController {


    @RequestMapping("/list")
    fun test(username: String?, password: String?): List<GameTask>? {


        return null
    }

}


