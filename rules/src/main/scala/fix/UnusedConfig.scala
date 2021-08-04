package fix

case class UnusedConfig(
    params: Boolean = true,
    locals: Boolean = true,
    imports: Boolean = true,
    privates: Boolean = true,
    patvars: Boolean = true,
)

object UnusedConfig {
  def default = UnusedConfig()
  implicit val surface =
    metaconfig.generic.deriveSurface[UnusedConfig]
  implicit val decoder =
    metaconfig.generic.deriveDecoder(default)
}
