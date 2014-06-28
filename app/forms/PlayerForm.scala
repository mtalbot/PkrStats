package forms

object PlayerForm {
  case class PlayerViewModel(id: Long, name: String, nicknames: Seq[String])
  
  
}