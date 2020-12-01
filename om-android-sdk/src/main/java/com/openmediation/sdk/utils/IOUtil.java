// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class IOUtil {

    private IOUtil() {
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (Exception e) {
                DeveloperLog.LogD("IOUtil", e);
                CrashUtil.getSingleton().saveException(e);
            }
    }

    public static void flushQuietly(Flushable flushable) {
        if (flushable != null)
            try {
                flushable.flush();
            } catch (Exception e) {
                DeveloperLog.LogD("IOUtil", e);
                CrashUtil.getSingleton().saveException(e);
            }
    }

    public static BufferedInputStream toBufferedInputStream(InputStream inputStream) {
        return inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
    }

    public static BufferedOutputStream toBufferedOutputStream(OutputStream outputStream) {
        return outputStream instanceof BufferedOutputStream ? (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream);
    }

    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static BufferedWriter toBufferedWriter(Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    public static InputStream toInputStream(CharSequence input) {
        return new ByteArrayInputStream(input.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }

    public static InputStream toInputStream(CharSequence input, String charset) {
        return toInputStream(input, Charset.forName(charset));
    }

    public static InputStream toInputStream(CharSequence input, Charset charset) {
        byte[] bytes = input.toString().getBytes(charset);
        return new ByteArrayInputStream(bytes);
    }

    public static InputStreamReader toInputStreamReader(InputStream stream) {
        return new InputStreamReader(stream, Charset.forName(CommonConstants.CHARTSET_UTF8));
    }

    public static InputStreamReader toInputStreamReader(InputStream stream, String chartSet) throws UnsupportedEncodingException {
        return new InputStreamReader(stream, chartSet);
    }

    public static String toString(InputStream input) throws IOException {
        return new String(toByteArray(input), Charset.forName(CommonConstants.CHARTSET_UTF8));
    }

    public static String toString(InputStream input, String charset) throws IOException {
        return new String(toByteArray(input), charset);
    }

    public static String toString(InputStream input, Charset charset) throws IOException {
        return new String(toByteArray(input), charset);
    }

    public static String toString(Reader input) throws IOException {
        return new String(toByteArray(input), Charset.forName(CommonConstants.CHARTSET_UTF8));
    }

    public static String toString(Reader input, String charset) throws IOException {
        return new String(toByteArray(input), charset);
    }

    public static String toString(Reader input, Charset charset) throws IOException {
        return new String(toByteArray(input), charset);
    }

    public static String toString(byte[] byteArray) {
        return new String(byteArray, Charset.forName(CommonConstants.CHARTSET_UTF8));
    }

    public static String toString(byte[] byteArray, String charset) {
        return toString(byteArray, Charset.forName(charset));
    }

    public static String toString(byte[] byteArray, Charset charset) {
        return new String(byteArray, charset);
    }

    public static byte[] toByteArray(CharSequence input) {
        if (input == null)
            return new byte[0];
        return input.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8));
    }

    public static byte[] toByteArray(CharSequence input, String charset) {
        return toByteArray(input, Charset.forName(charset));
    }

    public static byte[] toByteArray(CharSequence input, Charset charset) {
        if (input == null)
            return new byte[0];
        else
            return input.toString().getBytes(charset);
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        output.close();
        input.close();
        return output.toByteArray();
    }

    public static byte[] toByteArray(InputStream input, int size) throws IOException {
        if (size < 0)
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);

        if (size == 0) return new byte[0];

        byte[] data = new byte[size];
        int offset = 0;
        int byteCount;
        while ((offset < size) && (byteCount = input.read(data, offset, size - offset)) != -1)
            offset += byteCount;

        if (offset != size)
            throw new IOException("Unexpected byte count size. current: " + offset + ", excepted: " + size);
        return data;
    }

    public static byte[] toByteArray(Reader input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        output.close();
        return output.toByteArray();
    }

    public static byte[] toByteArray(Reader input, String charset) throws IOException {
        return toByteArray(input, Charset.forName(charset));
    }

    public static byte[] toByteArray(Reader input, Charset charset) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output, charset);
        output.close();
        return output.toByteArray();
    }

    public static char[] toCharArray(CharSequence input) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        write(output, input);
        return output.toCharArray();
    }

    public static char[] toCharArray(InputStream input) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        write(input, output);
        return output.toCharArray();
    }

    public static char[] toCharArray(InputStream input, String charset) throws IOException {
        return toCharArray(input, Charset.forName(charset));
    }

    public static char[] toCharArray(InputStream input, Charset charset) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        write(input, output, charset);
        return output.toCharArray();
    }

    public static char[] toCharArray(Reader input) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        write(input, output);
        return output.toCharArray();
    }

    public static List<String> readLines(InputStream input, String charset) throws IOException {
        return readLines(input, Charset.forName(charset));
    }

    public static List<String> readLines(InputStream input, Charset charset) throws IOException {
        Reader reader = new InputStreamReader(input, charset);
        return readLines(reader);
    }

    public static List<String> readLines(InputStream input) throws IOException {
        Reader reader = new InputStreamReader(input, Charset.forName(CommonConstants.CHARTSET_UTF8));
        return readLines(reader);
    }

    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    public static void write(OutputStream output, byte[] data) throws IOException {
        if (data != null) {
            output.write(data);
            output.flush();
        }
    }

    public static void write(Writer output, byte[] data) throws IOException {
        if (data != null) {
            output.write(new String(data, Charset.forName(CommonConstants.CHARTSET_UTF8)));
            output.flush();
        }
    }

    public static void write(Writer output, byte[] data, String charset) throws IOException {
        write(output, data, Charset.forName(charset));
    }

    public static void write(Writer output, byte[] data, Charset charset) throws IOException {
        if (data != null) {
            output.write(new String(data, charset));
            output.flush();
        }
    }

    public static void write(Writer output, char[] data) throws IOException {
        if (data != null) {
            output.write(data);
            output.flush();
        }
    }

    public static void write(OutputStream output, char[] data) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
            output.flush();
        }
    }

    public static void write(OutputStream output, char[] data, String charset) throws IOException {
        write(output, data, Charset.forName(charset));
    }

    public static void write(OutputStream output, char[] data, Charset charset) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes(charset));
            output.flush();
        }
    }

    public static void write(Writer output, CharSequence data) throws IOException {
        if (data != null) {
            output.write(data.toString());
            output.flush();
        }
    }

    public static void write(OutputStream output, CharSequence data) throws IOException {
        if (data != null) {
            output.write(data.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
            output.flush();
        }
    }

    public static void write(OutputStream output, CharSequence data, String charset) throws IOException {
        write(output, data, Charset.forName(charset));
    }

    public static void write(OutputStream output, CharSequence data, Charset charset) throws IOException {
        if (data != null) {
            output.write(data.toString().getBytes(charset));
            output.flush();
        }
    }

    public static void write(Reader input, OutputStream output) throws IOException {
        Writer out = new OutputStreamWriter(output, Charset.forName(CommonConstants.CHARTSET_UTF8));
        write(input, out);
    }

    public static void write(InputStream input, OutputStream output) throws IOException {
        int len;
        byte[] buffer = new byte[4096];
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
            output.flush();
        }
    }

    public static void write(InputStream input, Writer output) throws IOException {
        Reader in = new InputStreamReader(input, Charset.forName(CommonConstants.CHARTSET_UTF8));
        write(in, output);
    }

    public static void write(Reader input, OutputStream output, String charset) throws IOException {
        write(input, output, Charset.forName(charset));
    }

    public static void write(Reader input, OutputStream output, Charset charset) throws IOException {
        Writer out = new OutputStreamWriter(output, charset);
        write(input, out);
    }

    public static void write(InputStream input, OutputStream output, String charset) throws IOException {
        write(input, output, Charset.forName(charset));
    }

    public static void write(InputStream input, OutputStream output, Charset charset) throws IOException {
        Reader in = new InputStreamReader(input, charset);
        write(in, output);
    }

    public static void write(InputStream input, Writer output, String charset) throws IOException {
        write(input, output, Charset.forName(charset));
    }

    public static void write(InputStream input, Writer output, Charset charset) throws IOException {
        Reader in = new InputStreamReader(input, charset);
        write(in, output);
    }

    public static void write(Reader input, Writer output) throws IOException {
        int len;
        char[] buffer = new char[4096];
        while (-1 != (len = input.read(buffer))) {
            output.write(buffer, 0, len);
            output.flush();
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        int size;
        byte[] buf = new byte[4096];
        while ((size = in.read(buf)) != -1) {
            out.write(buf, 0, size);
        }
        out.flush();
    }

    public static FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            return null;
        }
        return new FileInputStream(file);
    }

    public static void writeToFile(InputStream in, File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        copy(in, out);
        closeQuietly(out);
    }

    public static void writeToFile(byte[] bytes, File file) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(bytes);
        } finally {
            if (out != null) {
                closeQuietly(out);
            }
        }
    }
}
