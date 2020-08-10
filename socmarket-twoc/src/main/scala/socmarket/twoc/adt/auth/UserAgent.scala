package socmarket.twoc.adt.auth

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class UserAgent(
  deviceClass: String = "",
  deviceName: String = "",
  deviceBrand: String = "",

  osClass: String = "",
  osName: String = "",
  osVersion: String = "",
  osNameVersion: String = "",
  osVersionBuild: String = "",

  layoutEngineClass: String = "",
  layoutEngineName: String = "",
  layoutEngineVersion: String = "",
  layoutEngineVersionMajor: String = "",
  layoutEngineNameVersion: String = "",
  layoutEngineNameVersionMajor: String = "",

  agentClass: String = "",
  agentName: String = "",
  agentVersion: String = "",
  agentVersionMajor: String = "",
  agentNameVersion: String = "",
  agentNameVersionMajor: String = "",
)

object UserAgent {
  implicit val codec: Codec[UserAgent] = deriveCodec
}