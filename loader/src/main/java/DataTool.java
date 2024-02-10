import java.io.*;
import java.util.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class DataTool {

    private static final byte[] HEADER = {'H', '2', 'A', '1'};


    public static void extract(String fromFile, String toDir,String fileName,String targetName) throws IOException {

        //System.out.println(111);
        long size = new File(fromFile).length();
        InputStream in =
                new BufferedInputStream(
                        new FileInputStream(fromFile), 1024 * 1024);
        String temp = fromFile + ".temp";
        Inflater inflater = new Inflater();
        in = new InflaterInputStream(in, inflater, 1024 * 1024);
        OutputStream out = getDirectoryOutputStream(toDir,fileName,targetName);
        combine(in, out, temp);
        inflater.end();
        in.close();
        out.close();
    }



    private static OutputStream getDirectoryOutputStream(final String dir,String fileName,String targetName) {
        new File(dir).mkdirs();
        return new OutputStream() {

            private ByteArrayOutputStream meta = new ByteArrayOutputStream();
            private OutputStream fileOut;
            private File file;
            private long remaining = 4;
            private long modified;
            private boolean readOnly;

            @Override
            public void write(byte[] buff, int offset, int length) throws IOException {
                while (length > 0) {
                    if (fileOut == null || remaining <= 1) {
                        write(buff[offset] & 255);
                        offset++;
                        length--;
                    } else {
                        int l = (int) Math.min(length, remaining - 1);
                        if (fileName==null || file.getName().equals(fileName))
                        fileOut.write(buff, offset, l);
                        remaining -= l;
                        offset += l;
                        length -= l;
                    }
                }
            }

            @Override
            public void write(int b) throws IOException {
                if (fileOut != null) {
                    fileOut.write(b);
                    if (--remaining > 0) {
                        return;
                    }
                    // this can be slow, but I don't know a way to avoid it
                    fileOut.close();
                    fileOut = null;
                    file.setLastModified(modified);
                    if (readOnly) {
                        file.setReadOnly();
                    }
                    remaining = 4;
                    return;
                }
                meta.write(b);
                if (--remaining > 0) {
                    return;
                }
                DataInputStream in = new DataInputStream(
                        new ByteArrayInputStream(meta.toByteArray()));
                if (meta.size() == 4) {
                    // metadata is next
                    remaining = in.readInt() - 4;
                    if (remaining > 16 * 1024) {
                        throw new IOException("Illegal directory stream");
                    }
                    return;
                }
                // read and ignore the length
                in.readInt();
                boolean isFile = in.read() == 1;
                readOnly = in.read() == 1;
                modified = readVarLong(in);
                if (isFile) {
                    remaining = readVarLong(in);
                } else {
                    remaining = 4;
                }
                String indexFileName = in.readUTF();
                String name = dir + "/" + indexFileName;
                file = new File(name);
                if (isFile) {
                        if (fileName == null || indexFileName.equals(fileName)) {

                            if (targetName != null )
                                name =dir + "/" +targetName;
                            if (remaining == 0) {
                                new File(name).createNewFile();
                                remaining = 4;
                            } else {
                                fileOut = new BufferedOutputStream(
                                        new FileOutputStream(name), 1024 * 1024);
                            }
                        }else
                        {
                            fileOut = new ByteArrayOutputStream();
                        }

                } else {
                    file.mkdirs();
                    file.setLastModified(modified);
                    if (readOnly) {
                        file.setReadOnly();
                    }
                }
                meta.reset();
            }
        };
    }


    private static long openSegments(List<Long> segmentStart, TreeSet<ChunkStream> segmentIn,
                                     String tempFileName, boolean readKey) throws IOException {
        long inPos = 0;
        int bufferTotal = 64 * 1024 * 1024;
        int bufferPerStream = bufferTotal / segmentStart.size();
        // FileChannel fc = new RandomAccessFile(tempFileName, "r").
        //     getChannel();
        for (int i = 0; i < segmentStart.size(); i++) {
            // long end = i < segmentStart.size() - 1 ?
            //     segmentStart.get(i+1) : fc.size();
            // InputStream in =
            //     new SharedInputStream(fc, segmentStart.get(i), end);
            InputStream in = new FileInputStream(tempFileName);
            in.skip(segmentStart.get(i));
            ChunkStream s = new ChunkStream(i);
            s.readKey = readKey;
            s.in = new DataInputStream(new BufferedInputStream(in, bufferPerStream));
            inPos += s.readNext();
            if (s.current != null) {
                segmentIn.add(s);
            }
        }
        return inPos;
    }

    private static Iterator<Chunk> merge(final TreeSet<ChunkStream> segmentIn) {
        return new Iterator<Chunk>() {

            @Override
            public boolean hasNext() {
                return !segmentIn.isEmpty();
            }

            @Override
            public Chunk next() {
                ChunkStream s = segmentIn.first();
                segmentIn.remove(s);
                Chunk c = s.current;
                int len = s.readNext();
                if (s.current != null) {
                    segmentIn.add(s);
                }
                return c;
            }

        };
    }



    private static void combine( InputStream in, OutputStream out,
                                String tempFileName) throws IOException {
        int bufferSize = 16 * 1024 * 1024;
        DataOutputStream tempOut =
                new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(tempFileName), 1024 * 1024));

        // File: header length chunk* 0
        // chunk: pos* 0 data
        DataInputStream dataIn = new DataInputStream(in);
        byte[] header = new byte[4];
        dataIn.readFully(header);
        if (!Arrays.equals(header, HEADER)) {
            tempOut.close();
            throw new IOException("Invalid header");
        }
        long size = readVarLong(dataIn);
        long outPos = 0;
        List<Long> segmentStart = new ArrayList<>();
        boolean end = false;

        while (!end) {
            int segmentSize = 0;
            TreeMap<Long, byte[]> map = new TreeMap<>();
            while (segmentSize < bufferSize) {
                Chunk c = Chunk.read(dataIn, false);
                if (c == null) {
                    end = true;
                    break;
                }
                int length = c.value.length;
                segmentSize += length;
                for (long x : c.idList) {
                    map.put(x, c.value);
                }
            }
            if (map.size() == 0) {
                break;
            }
            segmentStart.add(outPos);
            for (Long x : map.keySet()) {
                outPos += writeVarLong(tempOut, x);
                outPos += writeVarLong(tempOut, 0);
                byte[] v = map.get(x);
                outPos += writeVarLong(tempOut, v.length);
                tempOut.write(v);
                outPos += v.length;
            }
            outPos += writeVarLong(tempOut, 0);
        }
        tempOut.close();
        long tempSize = new File(tempFileName).length();
        size = outPos;

        int blockSize = 64;
        boolean merge = false;
        while (segmentStart.size() > blockSize) {
            merge = true;
            ArrayList<Long> segmentStart2 = new ArrayList<>();
            outPos = 0;
            DataOutputStream tempOut2 = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(tempFileName + ".b"), 1024 * 1024));
            while (segmentStart.size() > 0) {
                segmentStart2.add(outPos);
                int s = Math.min(segmentStart.size(), blockSize);
                List<Long> start = segmentStart.subList(0, s);
                TreeSet<ChunkStream> segmentIn = new TreeSet<>();
                long read = openSegments(start, segmentIn, tempFileName, false);

                Iterator<Chunk> it = merge(segmentIn);
                while (it.hasNext()) {
                    Chunk c = it.next();
                    outPos += writeVarLong(tempOut2, c.idList.get(0));
                    outPos += writeVarLong(tempOut2, 0);
                    outPos += writeVarLong(tempOut2, c.value.length);
                    tempOut2.write(c.value);
                    outPos += c.value.length;
                }
                outPos += writeVarLong(tempOut2, 0);

                segmentStart = segmentStart.subList(s, segmentStart.size());
            }
            segmentStart = segmentStart2;
            tempOut2.close();
            tempSize = new File(tempFileName).length();
            new File(tempFileName).delete();
            tempFileName += ".b";
        }

        TreeSet<ChunkStream> segmentIn = new TreeSet<>();
        DataOutputStream dataOut = new DataOutputStream(out);

        long read = openSegments(segmentStart, segmentIn, tempFileName, false);

        Iterator<Chunk> it = merge(segmentIn);
        while (it.hasNext()) {
            dataOut.write(it.next().value);
        }
        new File(tempFileName).delete();
        dataOut.flush();
    }

    static class ChunkStream implements Comparable<ChunkStream> {
        final int id;
        Chunk current;
        DataInputStream in;
        boolean readKey;

        ChunkStream(int id) {
            this.id = id;
        }
        int readNext() {
            current = null;
            current = Chunk.read(in, readKey);
            if (current == null) {
                return 0;
            }
            return current.value.length;
        }

        @Override
        public int compareTo(ChunkStream o) {
            int comp = current.compareTo(o.current);
            if (comp != 0) {
                return comp;
            }
            return Integer.signum(id - o.id);
        }
    }

    static class Chunk implements Comparable<Chunk> {
        ArrayList<Long> idList;
        final byte[] value;
        private final int[] sortKey;

        Chunk(ArrayList<Long> idList, int[] sortKey, byte[] value) {
            this.idList = idList;
            this.sortKey = sortKey;
            this.value = value;
        }

        public static Chunk read(DataInputStream in, boolean readKey) {
            try {
                ArrayList<Long> idList = new ArrayList<>();
                while (true) {
                    long x = readVarLong(in);
                    if (x == 0) {
                        break;
                    }
                    idList.add(x);
                }
                if (idList.isEmpty()) {
                    // eof
                    in.close();
                    return null;
                }
                int[] key = null;
                if (readKey) {
                    key = new int[4];
                    for (int i = 0; i < key.length; i++) {
                        key[i] = in.readInt();
                    }
                }
                int len = (int) readVarLong(in);
                byte[] value = new byte[len];
                in.readFully(value);
                return new Chunk(idList, key, value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int compareTo(Chunk o) {
            if (sortKey == null) {
                // sort by id
                long a = idList.get(0);
                long b = o.idList.get(0);
                if (a < b) {
                    return -1;
                } else if (a > b) {
                    return 1;
                }
                return 0;
            }
            for (int i = 0; i < sortKey.length; i++) {
                if (sortKey[i] < o.sortKey[i]) {
                    return -1;
                } else if (sortKey[i] > o.sortKey[i]) {
                    return 1;
                }
            }
            if (value.length < o.value.length) {
                return -1;
            } else if (value.length > o.value.length) {
                return 1;
            }
            for (int i = 0; i < value.length; i++) {
                int a = value[i] & 255;
                int b = o.value[i] & 255;
                if (a < b) {
                    return -1;
                } else if (a > b) {
                    return 1;
                }
            }
            return 0;
        }
    }


    static int writeVarLong(OutputStream out, long x)
            throws IOException {
        int len = 0;
        while ((x & ~0x7f) != 0) {
            out.write((byte) (0x80 | (x & 0x7f)));
            x >>>= 7;
            len++;
        }
        out.write((byte) x);
        return ++len;
    }

    static long readVarLong(InputStream in) throws IOException {
        long x = in.read();
        if (x < 0) {
            throw new EOFException();
        }
        x = (byte) x;
        if (x >= 0) {
            return x;
        }
        x &= 0x7f;
        for (int s = 7; s < 64; s += 7) {
            long b = in.read();
            if (b < 0) {
                throw new EOFException();
            }
            b = (byte) b;
            x |= (b & 0x7f) << s;
            if (b >= 0) {
                break;
            }
        }
        return x;
    }

}
