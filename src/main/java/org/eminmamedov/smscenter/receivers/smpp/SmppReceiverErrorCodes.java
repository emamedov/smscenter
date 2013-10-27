package org.eminmamedov.smscenter.receivers.smpp;

public interface SmppReceiverErrorCodes {

    /** Invalid connection status */
    int ESME_RINVBNDSTS = 0x00000004;
    /** Connection is already in bind state */
    int ESME_RALYBND = 0x00000005;
    /** System error on server side */
    int ESME_RSYSERR = 0x00000008;
    /** Invalid source address */
    int ESME_RINVSRCADR = 0x0000000A;
    /** Invalid destination address */
    int ESME_RINVDSTADR = 0x0000000B;
    /** Invalid message_id */
    int ESME_RINVMSGID = 0x0000000C;
    /** Invalid password */
    int ESME_RINVPASWD = 0x0000000E;
    /** Invalid optional parameters */
    int ESME_RINVOPTPARAMVAL = 0x000000C4;

}
