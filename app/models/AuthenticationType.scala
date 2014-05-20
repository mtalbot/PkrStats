package models

import data.helpers.MappedColumnEnumeration

object AuthenticationType extends MappedColumnEnumeration {
	type AuthenticationType = Value
	val GooglePlus = Value
}