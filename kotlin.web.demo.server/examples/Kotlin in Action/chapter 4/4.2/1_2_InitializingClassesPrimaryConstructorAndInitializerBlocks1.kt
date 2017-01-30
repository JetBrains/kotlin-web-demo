package ch04.ex2_1_2_InitializingClassesPrimaryConstructorAndInitializerBlocks1

class User constructor(_nickname: String) {
    val nickname: String

    init {
        nickname = _nickname
    }
}
