package models

import java.util.Date

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Format, Json, JsonConfiguration}

case class AuthResponse(accessToken: String,
                        expiresIn: Date,
                        jti: String,
                        refreshToken: String,
                        scope: String,
                        tokenType: String)

object AuthResponse {
  implicit val config: JsonConfiguration.Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  implicit val authResponseFormat: Format[AuthResponse] = Json.format[AuthResponse]
}