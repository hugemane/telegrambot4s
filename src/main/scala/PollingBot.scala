import ChatAction.ChatAction

/**
 * PollingBot
 */
abstract class PollingBot(token: String) extends TelegramBot(token) with Polling with Runnable {

  import OptionPimps._

  val pollingCycle = 1000

  lazy val botName = {
    val user = getMe
    user.username.get
  }

  private var running = true

  override def run(): Unit = {
    println("Running: " + botName)

    var updatesOffset = 0

    while (running) {
      for (u <- getUpdates(offset = updatesOffset)) {
        handleUpdate(u)
        updatesOffset = updatesOffset max (u.updateId + 1)
      }

      Thread.sleep(pollingCycle)
    }
  }

  //def start(): Unit = (new Thread(this)).start()
  def stop(): Unit = (running = false)

  def setChatAction(chat_id: Int, chatAction: ChatAction): Unit = sendChatAction(chat_id, chatAction)
}