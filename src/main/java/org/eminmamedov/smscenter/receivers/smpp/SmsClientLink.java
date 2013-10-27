package org.eminmamedov.smscenter.receivers.smpp;

import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.util.SMPPIO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

public class SmsClientLink {

    private static final Logger log = Logger.getLogger(SmsClientLink.class);

    private static final String END_OF_STREAM_ERR = "EOS reached. No data available";
    private static final String LINK_NOT_UP_ERR = "Link not established.";
    private static final int MAX_FIELD_SIZE = 16;
    private static final int INT_TYPE_SIZE = 4;

    /** The buffered input of the link. */
    private BufferedInputStream in;
    /** The buffered output of the link. */
    private BufferedOutputStream out;
    /** Object to use to lock reading. */
    private Lock lock = new ReentrantLock();
    private Condition dataAvailable = lock.newCondition();
    /**
     * Set to automatically flush the output stream after every packet. Default
     * is true.
     */
    private boolean autoFlush;
    private Socket clientSocket;

    /**
     * Create a new unconnected SmscLink.
     * 
     * @throws IOException
     */
    public SmsClientLink(Socket clientSocket) throws IOException {
        this.autoFlush = true;
        this.clientSocket = clientSocket;
        this.in = new BufferedInputStream(this.clientSocket.getInputStream());
        this.out = new BufferedOutputStream(this.clientSocket.getOutputStream());
    }

    /**
     * Close the connection to the SMSC. Calling this method will close the
     * network link to the remote SMSC system. Applications should be unbound
     * from the SMPP link (using {@link ie.omk.smpp.Connection#unbind}) before
     * closing the underlying network link. The connection may be reestablished
     * using {@link #open}.
     * 
     * @throws java.io.IOException
     *             If an exception occurs while closing the connection.
     */
    public final void close() throws IOException {
        out = null;
        in = null;
        clientSocket.close();
    }

    /**
     * Send a packet to the SMSC.
     * 
     * @param pak
     *            the SMPP packet to send.
     * @param withOptional
     *            true to send the optional parameters over the link too, false
     *            to only send the mandatory parameters.
     * @throws java.io.IOException
     *             if an exception occurs during writing or if the connection is
     *             not open.
     */
    public void write(SMPPPacket pak, boolean withOptional) throws IOException {
        if (out == null) {
            throw new IOException(LINK_NOT_UP_ERR);
        }

        lock.lock();
        try {
            pak.writeTo(out, withOptional);
            if (autoFlush) {
                flush();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Flush the output stream of the SMSC link.
     * 
     * @throws java.io.IOException
     *             If an exception occurs while flushing the output stream.
     */
    public void flush() throws IOException {
        if (out != null) {
            out.flush();
        }
    }

    /**
     * Read the next SMPP packet from the SMSC. This method will block until a
     * full packet can be read from the SMSC. The caller should pass in a byte
     * array to read the packet into. If the passed in byte array is too small,
     * a new one will be allocated and returned to the caller.
     * 
     * @param array
     *            a byte array buffer to read the packet into.
     * @return the handle to the passed in buffer or the reallocated one.
     * @throws java.io.EOFException
     *             If the end of stream is reached before a full packet can be
     *             read.
     * @throws java.io.IOException
     *             If an exception occurs when reading the packet from the input
     *             stream.
     */
    public byte[] read(final byte[] array) throws IOException {
        if (in == null) {
            throw new IOException(LINK_NOT_UP_ERR);
        }

        lock.lock();
        byte[] buf = array;
        int count = 0;
        try {
            while (in.available() == 0) {
                dataAvailable.await(1, TimeUnit.SECONDS);
            }
            count = readBytes(buf, 0, INT_TYPE_SIZE, MAX_FIELD_SIZE);
            int cmdLen = SMPPIO.bytesToInt(buf, 0, INT_TYPE_SIZE);
            if (cmdLen > buf.length) {
                byte[] newbuf = new byte[cmdLen];
                System.arraycopy(buf, 0, newbuf, 0, count);
                buf = newbuf;
            }
            int remaining = cmdLen - count;
            readBytes(buf, count, remaining, remaining);
            return buf;
        } catch (InterruptedException e) {
            log.warn(e, e);
            throw new IOException("InterruptedException ocurred", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempt to read the bytes for an SMPP packet from the inbound stream.
     * 
     * @param buf
     *            The buffer to read bytes in to.
     * @param offset
     *            The offset into buffer to begin writing bytes from.
     * @param maxLen
     *            The maximum number of bytes to read in.
     * @param minimum
     *            The minimum number of bytes to read before returning. Once
     *            this method has read at least this number of bytes, it will
     *            return.
     * @return The number of bytes read by this method.
     * @throws IOException
     */
    private int readBytes(byte[] buf, int offset, int minimum, int maxLen) throws IOException {
        int ptr = in.read(buf, offset, maxLen);
        if (ptr < minimum) {
            if (ptr == -1) {
                throw new EOFException(END_OF_STREAM_ERR);
            }
            while (ptr < minimum) {
                int count = in.read(buf, offset + ptr, maxLen - ptr);
                if (count < 0) {
                    throw new EOFException(END_OF_STREAM_ERR);
                }
                ptr += count;
            }
        }
        return ptr;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public String getClientHost() {
        if (clientSocket == null || clientSocket.getInetAddress() == null) {
            return null;
        }
        return clientSocket.getInetAddress().getHostAddress();
    }

}
