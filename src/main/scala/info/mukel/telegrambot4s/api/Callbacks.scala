package info.mukel.telegrambot4s.api

import info.mukel.telegrambot4s.methods.AnswerCallbackQuery
import info.mukel.telegrambot4s.models.CallbackQuery

import scala.collection.mutable
import scala.concurrent.Future

/**
  * Abstraction for callbacks, allowing to filter events by tag.
  */
trait Callbacks extends BotBase {

  private type Filter = CallbackQuery => Boolean
  private type Action = CallbackQuery => Unit

  private val handlers = mutable.Map[Filter, Action]()

  /** Filters callbacks based on a tag (to avoid collision).
    * Process callbacks starting with 'tag'.
    *
    * @param tag Tag
    * @param action Method to process the filtered callback query.
    */
  def onCallbackWithTag(tag: String)(action: Action): Unit = {
    onCallback(_.data.map(_.startsWith(tag)).getOrElse(false))(action)
  }

  /** Generic callbacks filtering (to avoid collision).
    *
    * @param filter A filter should not have side effects, and should be fast (no DB requests).
    * @param action Method to process the filtered callback query.
    */
  def onCallback(filter: Filter)(action: Action): Unit = {
    handlers += ((filter, action))
  }

  abstract override def onCallbackQuery(callbackQuery: CallbackQuery): Unit = {
    for {
      (filter, action) <- handlers
      if filter(callbackQuery)
    } yield
      action(callbackQuery)

    super.onCallbackQuery(callbackQuery)
  }

  def ackCallback(text: Option[String] = None,
          showAlert: Option[Boolean] = None,
          url: Option[String] = None,
          cacheTime: Option[Int] = None)
         (implicit callbackQuery: CallbackQuery): Future[Boolean] = {
    request(AnswerCallbackQuery(callbackQuery.id, text, showAlert, url, cacheTime))
  }
}
