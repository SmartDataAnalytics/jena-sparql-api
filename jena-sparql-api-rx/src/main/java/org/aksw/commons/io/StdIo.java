package org.aksw.commons.io;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.CloseShieldOutputStream;

/**
 * A small util class to open close-shielded output streams to stdout and stderr
 *
 * @author raven
 *
 */
public class StdIo {

    public static OutputStream openStdout() {
        return new CloseShieldOutputStream(new FileOutputStream(FileDescriptor.out));
    }

    public static OutputStream openStderr() {
        return new CloseShieldOutputStream(new FileOutputStream(FileDescriptor.err));
    }

//    public static OutputStream openStdin() {
//        return new CloseShieldInputStream(new FileInputStream(FileDescriptor.in));
//    }

}
