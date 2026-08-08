fun main() {
    val regex = Regex("\u001B\\[[;\\d]*[ -/]*[@-~]")
    val s = "\u001b[01m\u001b[K<source>:\u001b[m\u001b[K In function '\u001b[01m\u001b[Kmain\u001b[m\u001b[K':"
    println(s.replace(regex, ""))
}
