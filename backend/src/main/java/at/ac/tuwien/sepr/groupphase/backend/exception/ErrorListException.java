package at.ac.tuwien.sepr.groupphase.backend.exception;

import java.util.Collections;
import java.util.List;

/**
 * Common superclass for exceptions that report a list of errors back to the user
 * when the provided data fails certain checks.
 *
 * <p>
 * This abstract class is designed to hold a list of error messages related to validation
 * or other checks on the provided data. Subclasses can use this structure to
 * report multiple errors at once.
 * </p>
 */
public abstract class ErrorListException extends Exception {

    /**
     * List of errors.
     */
    private final List<String> errors;

    /**
     * Summary of the message.
     */
    private final String messageSummary;

    /**
     * Describes the error list.
     */
    private final String errorListDescriptor;

    /**
     * Constructor to create a new {@code ErrorListException}.
     *
     * @param errorListDescriptor a descriptor that describes the type or context of the errors
     * @param messageSummary      a brief summary of the overall error message
     * @param errors              a list of specific errors related to the validation or checks
     */
    public ErrorListException(String errorListDescriptor, String messageSummary, List<String> errors) {
        super(messageSummary);
        this.errorListDescriptor = errorListDescriptor;
        this.messageSummary = messageSummary;
        this.errors = errors;
    }

    /**
     * Returns a formatted message containing the error summary and the list of specific errors.
     *
     * <p>
     * The message is generated from the {@code messageSummary}, the {@code errorListDescriptor}, and
     * the list of {@code errors}.
     * </p>
     *
     * @return the detailed error message
     * @see Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return "%s. %s: %s."
            .formatted(messageSummary, errorListDescriptor, String.join(", ", errors));
    }

    /**
     * Returns the summary of the error message.
     *
     * @return the summary message
     */
    public String summary() {
        return messageSummary;
    }

    /**
     * Returns an unmodifiable list of errors that occurred.
     *
     * @return a list of errors
     */
    public List<String> errors() {
        return Collections.unmodifiableList(errors);
    }
}