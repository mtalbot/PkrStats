package models

trait Model[idType] {
	def id: idType;
}