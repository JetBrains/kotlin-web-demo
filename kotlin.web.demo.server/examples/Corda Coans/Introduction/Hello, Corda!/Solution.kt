data class IOUState(val amount: Amount<Currency>,
                    val lender: Party,
                    val borrower: Party)

/* The order doesn't matter (yet) unless you order based values for the construction of an instance of this type */