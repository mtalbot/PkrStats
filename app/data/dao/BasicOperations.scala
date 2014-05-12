package data.dao

trait Operation[returnType] {
  def getResult(value: AnyRef): returnType = value.asInstanceOf[returnType]
}

class BasicOperations[keyType, classType] {
  case class Insert(obj: classType) extends Operation[keyType]
  case class Update(obj: classType) extends Operation[Option[Long]]
  case class Delete(key: keyType) extends Operation[Boolean]
  case class Get(key: keyType) extends Operation[Option[classType]]
  case class Select(key: List[keyType]) extends Operation[Map[keyType, classType]]
}