/*
rule = Unused
*/
package fix.example

package inlineconsume

import inlinedefs.FakePredef.assert

class Foo:
  def test = assert(3 > 2)
