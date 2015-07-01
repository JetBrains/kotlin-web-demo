package i_introduction._4_String_Templates

val month = "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)"

fun getPattern() = """(\d{2}) ${month} (\d{4})"""

