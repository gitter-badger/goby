package com.mindprod.ledatastream;

/**
 * This class is the same as the one distributed by Roedy Green, Canadian Mind* Products
 * EXCEPT I have removed the "final" declaration on the class and methods to make it
 * extendable and I have reformatted the code. I also added Closable. Kevin Dorff, ICB-WMC.
 *
 * LEDataInputStream.java
 * copyright (c) 1998-2009 Roedy Green, Canadian Mind* Products
 * Very similar to DataInputStream except it reads
 * little-endian instead of big-endian binary data. We can't extend
 * DataInputStream directly since it has only final methods, though
 * DataInputStream itself is not final. This forces us implement
 * LEDataInputStream with a DataInputStream object, and use wrapper methods.
 */

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Closeable;

/**
 * reads little endian binary data .
 */
public class LEDataInputStream implements DataInput, Closeable {
    // ------------------------------ FIELDS ------------------------------

    /**
     * undisplayed copyright notice.
     *
     * @noinspection UnusedDeclaration
     */
    private static final String EMBEDDED_COPYRIGHT =
            "copyright (c) 1999-2009 Roedy Green, Canadian Mind Products, http://mindprod.com";

    /**
     * to get at the big-Endian methods of a basic DataInputStream.
     *
     * @noinspection WeakerAccess
     */
    protected final DataInputStream dis;

    /**
     * to get at the a basic readBytes method.
     *
     * @noinspection WeakerAccess
     */
    protected final InputStream is;

    /**
     * work array for buffering input.
     *
     * @noinspection WeakerAccess
     */
    protected final byte[] work;
    // -------------------------- PUBLIC STATIC METHODS --------------------------

    /**
     * Note. This is a STATIC method!
     *
     * @param in stream to read UTF chars from (endian irrelevant)
     * @return string from stream
     * @throws IOException if read fails.
     */
    public static String readUTF(final DataInput in) throws IOException {
        return DataInputStream.readUTF(in);
    }

    // -------------------------- PUBLIC INSTANCE  METHODS --------------------------
    /**
     * constructor.
     *
     * @param in binary inputstream of little-endian data.
     */
    public LEDataInputStream(final InputStream in) {
        this.is = in;
        this.dis = new DataInputStream(in);
        work = new byte[8];
    }

    /**
     * close.
     *
     * @throws IOException if close fails.
     */
    public void close() throws IOException {
        dis.close();
    }

    /**
     * Read bytes. Watch out, read may return fewer bytes than requested.
     *
     * @param ba  where the bytes go.
     * @param off offset in buffer, not offset in file.
     * @param len count of bytes to read.
     * @return how many bytes read.
     * @throws IOException if read fails.
     */
    public int read(final byte[] ba, final int off, final int len) throws IOException {
        // For efficiency, we avoid one layer of wrapper
        return is.read(ba, off, len);
    }

    /**
     * read only a one-byte boolean.
     *
     * @return true or false.
     * @throws IOException if read fails.
     * @see java.io.DataInput#readBoolean()
     */
    public boolean readBoolean() throws IOException {
        return dis.readBoolean();
    }

    /**
     * read byte.
     *
     * @return the byte read.
     * @throws IOException if read fails.
     * @see java.io.DataInput#readByte()
     */
    public byte readByte() throws IOException {
        return dis.readByte();
    }

    /**
     * Read on char. like DataInputStream.readChar except little endian.
     *
     * @return little endian 16-bit unicode char from the stream.
     * @throws IOException if read fails.
     */
    public char readChar() throws IOException {
        dis.readFully(work, 0, 2);
        return (char) ((work[1] & 0xff) << 8 | (work[0] & 0xff));
    }

    /**
     * Read a double. like DataInputStream.readDouble except little endian.
     *
     * @return little endian IEEE double from the datastream.
     * @throws IOException if read error
     */
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Read one float. Like DataInputStream.readFloat except little endian.
     *
     * @return little endian IEEE float from the datastream.
     * @throws IOException if read fails.
     */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Read bytes until the array is filled.
     *
     * @param ba the buffer into which the data is read.
     * @see java.io.DataInput#readFully(byte[])
     * @throws IOException if read error
     */
    public void readFully(final byte[] ba) throws IOException {
        dis.readFully(ba, 0, ba.length);
    }

    /**
     * Read bytes until the count is satisfied.
     *
     * @param ba the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the number of bytes to read.
     * @throws IOException if read fails.
     * @see java.io.DataInput#readFully(byte[],int,int)
     */
    public void readFully(
            final byte[] ba, final int off, final int len) throws IOException {
        dis.readFully(ba, off, len);
    }

    /**
     * Read an int, 32-bits. Like DataInputStream.readInt except little endian.
     *
     * @return little-endian binary int from the datastream
     * @throws IOException if read fails.
     */
    public int readInt() throws IOException {
        dis.readFully(work, 0, 4);
        return (work[3]) << 24
                | (work[2] & 0xff) << 16
                | (work[1] & 0xff) << 8
                | (work[0] & 0xff);
    }

    /**
     * Read a line.
     *
     * @return a rough approximation of the 8-bit stream as a 16-bit unicode string
     * @throws IOException if read fails
     * @noinspection deprecation
     * @deprecated This method does not properly convert bytes to characters. Use a Reader
     * instead with a little-endian
     *             encoding.
     */
    public String readLine() throws IOException {
        return dis.readLine();
    }

    /**
     * read a long, 64-bits.  Like DataInputStream.readLong except little endian.
     *
     * @return little-endian binary long from the datastream.
     * @throws IOException if read fails
     */
    public long readLong() throws IOException {
        dis.readFully(work, 0, 8);
        return (long) (work[7]) << 56
                |
                /* long cast needed or shift done modulo 32 */
                (long) (work[6] & 0xff) << 48
                | (long) (work[5] & 0xff) << 40
                | (long) (work[4] & 0xff) << 32
                | (long) (work[3] & 0xff) << 24
                | (long) (work[2] & 0xff) << 16
                | (long) (work[1] & 0xff) << 8
                | (long) (work[0] & 0xff);
    }

    /**
     * Read short, 16-bits. Like DataInputStream.readShort except little endian.
     *
     * @return little endian binary short from stream.
     * @throws IOException if read fails.
     */
    public short readShort() throws IOException {
        dis.readFully(work, 0, 2);
        return (short) ((work[1] & 0xff) << 8 | (work[0] & 0xff));
    }

    /**
     * Read UTF counted string.
     *
     * @return String read.
     * @throws IOException if read fails.
     */
    public String readUTF() throws IOException {
        return dis.readUTF();
    }

    /**
     * Read an unsigned byte, 8-bits. Note: returns an int, even though says Byte (non-Javadoc)
     *
     * @return little-endinan unsigned byte from the stream
     * @throws IOException if read fails.
     * @see java.io.DataInput#readUnsignedByte()
     */
    public int readUnsignedByte() throws IOException {
        return dis.readUnsignedByte();
    }

    /**
     * Read an unsigned short, 16 bits. Like DataInputStream.readUnsignedShort except little
     * endian. Note, returns int even though it reads a short.
     *
     * @return little-endian int from the stream.
     * @throws IOException if read fails.
     */
    public int readUnsignedShort() throws IOException {
        dis.readFully(work, 0, 2);
        return ((work[1] & 0xff) << 8 | (work[0] & 0xff));
    }

    /**
     * Skip over bytes in the stream. See the general contract of the <code>skipBytes</code>
     * method of <code>DataInput</code>.
     * <p/>
     * Bytes for this operation are read from the contained input stream.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O error occurs.
     */
    public int skipBytes(final int n) throws IOException {
        return dis.skipBytes(n);
    }
}
