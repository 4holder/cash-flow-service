package domain

case object Currency extends Enumeration {
  val BRL: Value = Value("BRL")
  val USD: Value = Value("USD")
}
