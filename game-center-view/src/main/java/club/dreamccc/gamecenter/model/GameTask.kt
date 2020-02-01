package club.dreamccc.gamecenter.model

class GameTask(){


    var taskId: String? = null

    var taskName: String? = null

    var playerId: String? = null

    var playerName: String? = null

    var status: Double = 0.0

    var itemList: List<GameTaskItem> = emptyList()

}

class GameTaskItem(){

}