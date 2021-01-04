package com.sten1ee.olli;

import java.io.IOException;
import java.io.Reader;

/**
 * A wrapper around java.io.Reader to provide a CharSequence view on it (so that regexps can be used on it)
 */
class ReaderCharSequence implements CharSequence {

    static class Kernel {
        Reader rdr;
        StringBuilder sb = new StringBuilder();
        int baseIdx = 0;

        Kernel(Reader rdr) {
            this.rdr = rdr;
        }

        int length() {
            return Integer.MAX_VALUE;
        }

        char charAt(int rqIdx) {
            if (rqIdx < baseIdx) {
                throw new IndexOutOfBoundsException("Requested " + rqIdx + " when baseIdx is " + baseIdx);
            }
            int adjIdx = rqIdx - baseIdx;
            if (adjIdx >= sb.length())
                loadNext(1 + adjIdx - sb.length());
            adjIdx = rqIdx - baseIdx;
            assert 0 <= adjIdx && adjIdx < sb.length() : adjIdx;
            return sb.charAt(adjIdx);
        }

        private void  loadNext(int nchars) {
            char[] buf = new char[4096];
            while (0 < nchars) {
                int nr = 0;
                try {
                    nr = rdr.read(buf, 0, Math.min(buf.length, nchars));
                }
                catch (IOException exn) {
                    throw new RuntimeException(exn);
                }
                if (nr > 0) {
                    nchars -= nr;
                    sb.append(buf, 0, nr);
                }
                else
                    break;
            }
        }
    }

    final Kernel  kernel;
    final int start;
    final int end;

    ReaderCharSequence(Reader rdr) {
        kernel = new Kernel(rdr);
        start = 0;
        end = Integer.MAX_VALUE;
    }

    protected ReaderCharSequence(Kernel kernel, int start, int end) {
        this.kernel = kernel;
        this.start = start;
        this.end = end;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        assert start <= index && index < end;
        return kernel.charAt(start + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        assert 0 <= start && start <= end && end <= length();
        return new ReaderCharSequence(kernel, start + this.start, end + this.start);
    }

    @Override
    public String  toString() {
        return kernel.sb.substring(start - kernel.baseIdx, end - kernel.baseIdx);
    }
}