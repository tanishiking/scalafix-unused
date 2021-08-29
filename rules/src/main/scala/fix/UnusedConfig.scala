package fix

case class UnusedConfig(
    params: Boolean = true,
    locals: Boolean = true,
    imports: Boolean = true,
    privates: Boolean = true,
    patvars: Boolean = true,
    disabledImports: List[String] = List(
      "scala.language"
    ),
    /** disable unused params check for the listed methods */
    disabledParamsOfMethods: List[String] = List(
      "main"
    )
)

object UnusedConfig {
  def default = UnusedConfig()
  implicit val surface =
    metaconfig.generic.deriveSurface[UnusedConfig]
  implicit val decoder =
    metaconfig.generic.deriveDecoder(default)
}
