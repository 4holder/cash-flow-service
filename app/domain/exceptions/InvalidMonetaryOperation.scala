package domain.exceptions

class InvalidMonetaryOperation(message: String) extends RuntimeException(message)

object InvalidMonetaryOperation {
  def apply(message: String): InvalidMonetaryOperation = new InvalidMonetaryOperation(message)
}
