package i_introduction._5_Nullable_Types

fun sendMessageToClient(
        client: Client?, message: String?, mailer: Mailer
){
    throw Exception("Not implemented")
}

class Client (val personalInfo: PersonalInfo?)
class PersonalInfo (val email: String?)
interface Mailer {
    fun sendMessage(email: String, message: String)
}