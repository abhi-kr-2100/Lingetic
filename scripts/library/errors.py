"""Custom exceptions for the Lingetic application."""

class InvalidWordIDError(Exception):
    """Exception raised when an invalid word ID is encountered.
    
    Attributes:
        word_id: The invalid word ID that was provided
        valid_word_ids: Set of valid word IDs that could have been used
    """
    def __init__(self, word_id: int, valid_word_ids: set[int]):
        self.word_id = word_id
        self.valid_word_ids = valid_word_ids
        super().__init__(f"Invalid word ID: {word_id}. Valid IDs are: {sorted(valid_word_ids)}")
