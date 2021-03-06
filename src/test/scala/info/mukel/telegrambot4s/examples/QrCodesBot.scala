package info.mukel.telegrambot4s.examples

import java.net.URLEncoder

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.ByteString
import info.mukel.telegrambot4s.Implicits._
import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models._

/**
  * Generates QR codes from text/url.
  */
class QrCodesBot(token: String) extends TestBot(token) with Polling with Commands with ChatActions {
  on("/qr", "QR-encodes arguments (text)") { implicit message => args =>

    val url = "https://api.qrserver.com/v1/create-qr-code/?data=" +
      URLEncoder.encode(args mkString " ", "UTF-8")

    for {
      response <- Http().singleRequest(HttpRequest(uri = Uri(url)))
      if response.status.isSuccess()
      bytes <- Unmarshal(response).to[ByteString]
    } /* do */ {
      val photo = InputFile("qrcode.png", bytes)
      uploadingPhoto // Hint the user
      request(SendPhoto(message.sender, photo))
    }
  }
}