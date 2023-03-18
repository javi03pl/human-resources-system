package nl.tudelft.sem.sem26b.message.domain;

/**
 * Enum for the different types of messages.
 */
public enum MessageType {

    /**
     * (HR) -> (Employee) or (Employee) -> (HR).
     * Propose a new contract.
     * Propose a contract modification.
     * Counter-propose a contract.
     */
    CONTRACT_PROPOSE,

    /**
     * (HR) -> (Employee) or (Employee) -> (HR).
     * Accept a contract proposal.
     */
    CONTRACT_APPROVE,

    /**
     * (HR) -> (Employee)
     * Terminate a contract.
     */
    CONTRACT_TERMINATE,

    /**
     * (Employee) -> (HR).
     * Request for a contract termination.
     */
    CONTRACT_TERMINATE_REQUEST,

    /**
     * (Employee) -> (HR)
     * Request copy of a document from HR.
     */
    DOCUMENT_REQUEST,

    /**
     * (Employee) -> (HR)
     * Request sick leave.
     */
    LEAVE_REQUEST,

    /**
     * (HR) -> (Employee)
     * Approve sick leave.
     */
    LEAVE_APPROVE,

    /**
     * (Any party) -> (Any party) (except to self).
     * Standard message.
     */
    OTHER,
}
