package models.baseModels

trait Model[idType] {
	def id: idType;
}