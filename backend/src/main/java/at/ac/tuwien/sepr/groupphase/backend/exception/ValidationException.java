package at.ac.tuwien.sepr.groupphase.backend.exception;

import java.util.List;

/**
 * Exception that signals invalid data from an external source.
 *
 * <p>
 * This exception is thrown when incoming data violates an invariant constraint,
 * meaning the data does not conform to the rules that are always expected,
 * independent of the system's current state. It contains a list of all validation checks that failed.
 * </p>
 */
public class ValidationException extends ErrorListException {

    /**
     * Constructs a {@code ValidationException} with a summary message and a list of validation errors.
     *
     * @param messageSummary a brief description summarizing the validation failure
     * @param errors         a list of specific validation errors that were encountered
     */
    public ValidationException(String messageSummary, List<String> errors) {
        super("Failed validations", messageSummary, errors);
    }
}
