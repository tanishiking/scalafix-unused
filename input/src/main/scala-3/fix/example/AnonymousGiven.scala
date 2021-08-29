/*
rule = Unused
*/
package fix.example
package angiven

trait Foo

def bar(using Foo) = 42 /* assert: Unused
              ^^^
Parameter value '' in method 'bar' is never used
*/
