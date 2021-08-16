/*
rule = Unused
 */

package fix.example

class EtaExpansion {
  Some(1).map(identity)
  List(1).foldLeft("")(_ + _)
}
