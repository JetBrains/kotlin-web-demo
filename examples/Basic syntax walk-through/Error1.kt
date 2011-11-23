namespace demo

fun map<S, T>(source : java.util.List<S> , f : fun (S):T) : java.util.List<T> {
    val answer = java.util.ArrayList<T>()
    for (s in source) answer.add(f(s))
    return answer;
}

fun main(args : Array<String>) {
    val source = java.util.ArrayList<String>();
    source.add("aaa");
    source.add("bbb");

    System.out?.println("Hello, ${map(source, {it.length})}!")
}
