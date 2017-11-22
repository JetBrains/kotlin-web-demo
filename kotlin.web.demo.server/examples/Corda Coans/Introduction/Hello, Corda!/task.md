## Adding fields to a state

Take a look at [something](http://kotlinlang.org/docs/reference/basic-syntax.html#defining-functions)
and add fields to this class to represent an IOU between two parties.

You will need to add the following fields: Lender, Borrower and Amount.

Party objects (i.e. "other actors") in Corda are represented by the [Party] type.
For the Amount field, use the Corda type Amount templated by the Java type \<Currency\>.

Replace the code highlighted.