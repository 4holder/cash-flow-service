package domain.exceptions

class InvalidMonetaryOperationException(message: String) extends RuntimeException(message)

object InvalidMonetaryOperationException {
  def apply(message: String): InvalidMonetaryOperationException = new InvalidMonetaryOperationException(message)
}
