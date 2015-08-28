package com.entradahealth.entrada.android.app.personal.audio;

import android.util.Log;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TODO: Document this!
 *
 * @author edwards
 * @since 12/27/12
 */
public class AudioDB implements Iterable<AudioSegment> {
    public static final String DB_FILE_NAME = "audio.db";
    public static final String LOG_TITLE = "Entrada-AudioDB";

    private static final ObjectMapper mapper = new ObjectMapper();
    static
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private ArrayList<AudioSegment> segments;
    private File basepath;

    public AudioDB(File path) {
        segments = new ArrayList<AudioSegment>();
        this.basepath = path;
        Log.i(LOG_TITLE, "New AudioDB created.");
    }

    @JsonCreator
    public AudioDB(@JsonProperty("path") File path,
                   @JsonProperty("segments")ArrayList<AudioSegment> segments) {
        this.segments = segments;
        this.basepath = path;
        Log.i(LOG_TITLE, "AudioDB loaded from file.");
    }

    @JsonProperty("path")
    public File getBasePath() {
        return this.basepath;
    }

    @JsonProperty("segments")
    public ArrayList<AudioSegment> getSegments() {
        return this.segments;
    }

    public static AudioDB load(File path) throws IOException {
        InputStream is = new FileInputStream(new File(path, DB_FILE_NAME));
        try {
            is = new CipherInputStream(is, AndroidState.getInstance().getUserState().getUserData().getCipherForUser(Cipher.DECRYPT_MODE));
            AudioDB db = mapper.readValue(is, AudioDB.class);
            return db;
        }
        finally {
            is.close();
        }
    }

    public void save() throws IOException {
        File dbpath = new File(this.basepath, DB_FILE_NAME);
        Log.d(LOG_TITLE, String.format("AudioManager saved to %s with %d segments.", dbpath.toString(), size()));

        OutputStream os = new FileOutputStream(dbpath);
        try {
            os = new CipherOutputStream(os, AndroidState.getInstance().getUserState().getUserData().getCipherForUser(Cipher.ENCRYPT_MODE));
            os = new BufferedOutputStream(os);
            mapper.writeValue(os, this);
            os.close();
        }
        catch (IOException ex) {
            throw ex;
        }
        finally {
        }
    }

    public void delete() throws IOException {
        for (AudioSegment seg : segments) {
            File segPath = new File(this.basepath, seg.getFileName());
            assert segPath.delete();
        }
        File dbPath = new File(this.basepath, DB_FILE_NAME);
        dbPath.delete();
        assert this.basepath.delete();
    }

    public void addSegment(AudioSegment segment, long start_sample) {
        start_sample = Math.min(start_sample, this.getTotalLength());

        this.split(start_sample);

        Log.i(LOG_TITLE, "Adding segment at sample " + Long.toString(start_sample));

        long cumulativeLength = 0;
        int idx = 0;
        for (AudioSegment seg : segments) {
            if (cumulativeLength == start_sample) break;
            cumulativeLength += seg.getLength();
            ++idx;
        }
        segments.add(idx, segment);
        Log.i(LOG_TITLE, "Add result: " + this.toString());
    }

    public void split(long sample) {
        sample = Math.min(sample, this.getTotalLength());

        // Do nothing on an empty DB, or if we're trying to split at the beginning or end.
        if (segments.size() == 0 || sample == 0 ||sample == this.getTotalLength()) return;

        Log.i(LOG_TITLE, "Splitting segment at sample " + Long.toString(sample));

        long cumulativeLength = 0;
        int idx = 0;
        for (AudioSegment seg : segments) {
            cumulativeLength += seg.getLength();
            if (cumulativeLength == sample)
                return; // There's already a split here. We're done.
            else if (cumulativeLength > sample)
                break;
            ++idx;
        }

        assert idx != segments.size();

        AudioSegment oldSeg = segments.get(idx);
        AudioSegment newSeg = oldSeg.duplicate();

        newSeg.trimFront(sample - (cumulativeLength - oldSeg.getLength()));
        oldSeg.trimBack(cumulativeLength - sample);

        segments.add(idx + 1, newSeg);

        Log.i(LOG_TITLE, "Split result: " + this.toString());
    }

    public void eraseRegion(long sample_start, long sample_length) {
        // We have no segments to erase. Nothing to do.
        if (this.size() == 0) return;

        Log.i(LOG_TITLE, String.format("Erasing [%d->%d] from %s", sample_start, sample_length, this.toString()));

        long total_len = this.getTotalLength();
        // Trim the requested region to an area that's actually within our range.
        sample_start = Math.min(sample_start, total_len);
        sample_length = Math.min(sample_length, total_len - sample_start);

        // Trim nothing. All done. Goodbye.
        if (sample_length == 0) return;

        // Ensure that there are split locations at both indexes.
        // This eliminates the sub-segment cases.
        this.split(sample_start);
        this.split(sample_start + sample_length);

        long cumulativeLength = 0;
        int idx = 0;

        do {
            // Find the index of the segment that marks the start of the deletion.
            if (cumulativeLength == sample_start) break;
            cumulativeLength += segments.get(idx).getLength();
            ++idx;
        } while (cumulativeLength <= sample_start && idx < segments.size());

        if (idx < segments.size()) {
            do {
                // Delete segments while we haven't reached the end segment.
                cumulativeLength += this.segments.get(idx).getLength();
                this.segments.remove(idx);
            } while (cumulativeLength < sample_start + sample_length && idx < segments.size());
        }

        Log.i(LOG_TITLE, "Erase result: " + this.toString());
    }

    public long getTotalLength() {
        long len = 0;
        for (AudioSegment seg : new ArrayList<AudioSegment>(segments)) {
            len += seg.getLength();
        }
        return len;
    }

    public long getDurationInMS() {
        return this.getSamplesInMS(this.getTotalLength());
    }

    public long getDurationInSecs() {
        return this.getDurationInMS() / 1000;
    }


    public long getSamplesInMS(long sample) {
        long len = (sample / (AudioManager.SAMPLE_RATE_HZ / 1000));
        return len;
    }

    public void truncate(long position) {
        this.eraseRegion(position, getTotalLength() - position);
    }

    public boolean isEmpty() {
        return this.segments.isEmpty();
    }

    public int size() {
        return this.segments.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DB: ");
        for (AudioSegment seg : segments) {
            builder.append(seg.toString());
        }
        builder.append(";");
        return builder.toString();
    }

    @Override
    public Iterator<AudioSegment> iterator() {
        return this.segments.iterator();
    }

    private void writeLittleEndianInt(DataOutputStream os, int val) throws IOException {
        os.writeByte((val >> 0) & 0xFF);
        os.writeByte((val >> 8) & 0xFF);
        os.writeByte((val >> 16) & 0xFF);
        os.writeByte((val >> 24) & 0xFF);
    }

    private void writeLittleEndianShort(DataOutputStream os, int val) throws IOException {
        os.writeByte((val >> 0) & 0xFF);
        os.writeByte((val >> 8) & 0xFF);
    }

    public void writeWAVFile(OutputStream os) throws IOException {
        DataOutputStream writer = new DataOutputStream(os);
        writeWAVFile(writer);
    }
    public void writeWAVFile(DataOutputStream writer) throws IOException {
        writeWAVHeader(writer);
        writeWAVData(writer);
    }

    public void writeWAVHeader(OutputStream os) throws IOException {
        DataOutputStream writer = new DataOutputStream(os);
        writeWAVHeader(writer);
    }

    public void writeWAVHeader(DataOutputStream writer) throws IOException {

        int numChannels = 1;
        int bitsPerSample = 16;
        int subChunk1Size = 16;
        int dataSize = (int)(getTotalLength());
        int subChunk2Size = dataSize * numChannels * bitsPerSample / 8;


        // WAV format from: https://ccrma.stanford.edu/courses/422/projects/WaveFormat/

        // Offset  Size  Name             Description

        /*******************************************************************************
         * The canonical WAVE format starts with the RIFF header:
         *
         * 0         4   ChunkID          Contains the letters "RIFF" in ASCII form
         *                                (0x52494646 big-endian form).
         * The default byte ordering assumed for WAVE data files is little-endian.
         * Files written using the big-endian byte ordering scheme have the identifier RIFX instead of RIFF. */
        writer.writeBytes("RIFF");
        /* 4         4   ChunkSize        36 + SubChunk2Size, or more precisely:
         *                                4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
         *                                This is the size of the rest of the chunk
         *                                following this number.  This is the size of the
         *                                entire file in bytes minus 8 bytes for the
         *                                two fields not included in this count:
         *                                ChunkID and ChunkSize. */
        writeLittleEndianInt(writer, 4 + (8 + subChunk1Size) + (8 + subChunk2Size));
        /* 8         4   Format           Contains the letters "WAVE"
         *                                (0x57415645 big-endian form). */
        writer.writeBytes("WAVE");


        /***************************************************************************
         * The "WAVE" format consists of two subchunks: "fmt " and "data":
         * The "fmt " subchunk describes the sound data's format:
         *
         * 12        4   Subchunk1ID      Contains the letters "fmt "
         *                                (0x666d7420 big-endian form).*/
        writer.writeBytes("fmt ");
        /* 16        4   Subchunk1Size    16 for PCM.  This is the size of the
         *                                rest of the Subchunk which follows this number. */
        writeLittleEndianInt(writer, subChunk1Size);
        /* 20        2   AudioFormat      PCM = 1 (i.e. Linear quantization)
         *                                Values other than 1 indicate some
         *                                form of compression. */
        writeLittleEndianShort(writer, 1);
        /* 22        2   NumChannels      Mono = 1, Stereo = 2, etc. */
        writeLittleEndianShort(writer, (short)numChannels);
        /* 24        4   SampleRate       8000, 44100, etc. */
        writeLittleEndianInt(writer, AudioManager.SAMPLE_RATE_HZ);
        /* 28        4   ByteRate         == SampleRate * NumChannels * BitsPerSample/8 */
        writeLittleEndianInt(writer, AudioManager.SAMPLE_RATE_HZ * numChannels * bitsPerSample / 8);
        /* 32        2   BlockAlign       == NumChannels * BitsPerSample/8
                                       The number of bytes for one sample including
                                       all channels. I wonder what happens when
                                       this number isn't an integer? */
        writeLittleEndianShort(writer, numChannels * bitsPerSample / 8);
        /* 34        2   BitsPerSample    8 bits = 8, 16 bits = 16, etc.*/
        writeLittleEndianShort(writer, bitsPerSample);
        /* 36        2   ExtraParamSize   if PCM, then doesn't exist
         *           X   ExtraParams      space for extra parameters*/
        //writer.writeShort(0);


        /************************************************************************
         * The "data" subchunk contains the size of the data and the actual sound:
         *
         * 36        4   Subchunk2ID      Contains the letters "data"
         *                                (0x64617461 big-endian form). */
        writer.writeBytes("data");
        /* 40        4   Subchunk2Size    == NumSamples * NumChannels * BitsPerSample/8
         *                                This is the number of bytes in the data.
         *                                You can also think of this as the size
         *                                of the read of the subchunk following this
         *                                number. */
        writeLittleEndianInt(writer, subChunk2Size);
    }
    
    //////////////////////////////////////////////
    private void WriteWaveFileHeader(
    		ByteArrayOutputStream out) throws IOException {
    	
    	int numChannels = 1;
        int bitsPerSample = 16;
        int subChunk1Size = 16;
        int totalDataLen = (int)(getTotalLength());
        int subChunk2Size = totalDataLen * numChannels * bitsPerSample / 8;
     
    byte[] header = new byte[44];
     
    header[0] = 'R';  // RIFF/WAVE header
    header[1] = 'I';
    header[2] = 'F';
    header[3] = 'F';
    header[4] = (byte) (totalDataLen & 0xff);
    header[5] = (byte) ((totalDataLen >> 8) & 0xff);
    header[6] = (byte) ((totalDataLen >> 16) & 0xff);
    header[7] = (byte) ((totalDataLen >> 24) & 0xff);
    header[8] = 'W';
    header[9] = 'A';
    header[10] = 'V';
    header[11] = 'E';
    header[12] = 'f';  // 'fmt ' chunk
    header[13] = 'm';
    header[14] = 't';
    header[15] = ' ';
    header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
    header[17] = 0;
    header[18] = 0;
    header[19] = 0;
    header[20] = 1;  // format = 1
    header[21] = 0;
    header[22] = (byte) numChannels;
    header[23] = 0;
    header[24] = (byte) (AudioManager.SAMPLE_RATE_HZ & 0xff);
    header[25] = (byte) ((AudioManager.SAMPLE_RATE_HZ >> 8) & 0xff);
    header[26] = (byte) ((AudioManager.SAMPLE_RATE_HZ >> 16) & 0xff);
    header[27] = (byte) ((AudioManager.SAMPLE_RATE_HZ >> 24) & 0xff);
    header[28] = (byte) (bitsPerSample & 0xff);
    header[29] = (byte) ((bitsPerSample >> 8) & 0xff);
    header[30] = (byte) ((bitsPerSample >> 16) & 0xff);
    header[31] = (byte) ((bitsPerSample >> 24) & 0xff);
    header[32] = (byte) (2 * 16 / 8);  // block align
    header[33] = 0;
    header[34] = (byte)(numChannels * bitsPerSample / 8);  // bits per sample
    header[35] = 0;
    header[36] = 'd';
    header[37] = 'a';
    header[38] = 't';
    header[39] = 'a';
    header[40] = (byte) (totalDataLen & 0xff);
    header[41] = (byte) ((totalDataLen >> 8) & 0xff);
    header[42] = (byte) ((totalDataLen >> 16) & 0xff);
    header[43] = (byte) ((totalDataLen >> 24) & 0xff);

    out.write(header, 0, 44);
}


    public void writeWAVData(OutputStream os) throws IOException {
        DataOutputStream writer = new DataOutputStream(os);
        writeWAVData(writer);
    }
    public void writeWAVData(DataOutputStream writer) throws IOException {
        /* 44        *   Data             The actual sound data. */
    	writeData(writer);
    }
    
    public OutputStream writeData(OutputStream writer) throws IOException{
        DBReadIterator iter = this.dbIterator();
        do{
            //Log.d("Entrada-AudioDB", "writing chunk");
        	if(iter != null) {
        		ByteBuffer buf = iter.next();
        		writer.write(buf.array());
        	}
        } while (iter.hasNext());
        return writer;
    }

    public DBReadIterator dbIterator() {
        return new DBReadIterator(this.segments.iterator());
    }

    public WavStream wavStream() throws IOException {
        WavStream stream = new WavStream(this.segments.iterator());
        return stream;
    }

    public class DBReadIterator extends InputStream implements Iterator<ByteBuffer> {

        Iterator<AudioSegment> segmentIterator;
        InputStream iStream = null;
        ByteBuffer buffer;
        int totalRead = 0;
        AudioSegment currentSegment = null;

        private DBReadIterator(Iterator<AudioSegment> segIter) {
            this.segmentIterator = segIter;
            buffer = ByteBuffer.allocate(AudioManager.BUFFER_SIZE);
            incrementSegment();
        }

        public void skipSamples(long count) throws IOException {
            if (isSegmentOpen() && count < (currentSegment.getLength() - totalRead)) {
                skip(count * 2);
                return;
            }

            count -= currentSegment.getLength() - totalRead;
            closeSegment();

            while (count >= 0) {
                incrementSegment();
                count -= currentSegment.getLength();
            }

            openSegment();
            skip((currentSegment.getLength() + count) * 2);
        }

        @Override
        public boolean hasNext() {
            if (!isSegmentOpen()) return segmentIterator.hasNext();
            return totalRead < currentSegment.getLength() || segmentIterator.hasNext();
        }

        /**
         * Iterates over the AudioDB in segment order, returning each ByteBuffer in order.
         * A ByteBuffer returned by this method is no longer valid once the iterator moves.
         * The iterator reuses the same ByteBuffer to avoid lots (no, seriously, lots) of allocations.
         *
         * @return The next buffer
         */
        @Override
        public ByteBuffer next() {
            try {
                if (!isSegmentOpen()) {
                    openSegment();
                }

                if (totalRead >= currentSegment.getLength()) {
                    closeSegment();
                    if (this.hasNext()) {
                        incrementSegment();
                        return this.next();
                    }
                }

                buffer.rewind();
                int read = iStream.read(buffer.array());
                assert read >= 0;
                totalRead += read / 2;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return buffer;
        }

        @Override
        public int read() throws IOException {
            if (!isSegmentOpen())
                openSegment();

            if (getCurrentBuffer().hasRemaining() == false) {
                if (hasNext() == false)
                    return -1;
                next();
            }
            byte val = getCurrentBuffer().get();
            return val & 0xFF; // http://stackoverflow.com/a/7401635/53315
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("DBIterator does not support remove()");
        }

        public ByteBuffer getCurrentBuffer() {
            return this.buffer;
        }



        private void incrementSegment() {
            do {
                currentSegment = segmentIterator.next();
                // Make sure we're not gonna try to work on an empty segment.
                // Shouldn't be possible, but just in case...
            } while (currentSegment != null && currentSegment.getLength() == 0);

            if (currentSegment == null) throw new NoSuchElementException("AudioDB.DBReadIterator iterator out of range.");
        }

        private void openSegment() throws IOException {
            File iStreamPath = new File(AudioDB.this.getBasePath(), currentSegment.getFileName());
            iStream = new FileInputStream(iStreamPath);
            iStream = new BufferedInputStream(iStream);
            iStream = new CipherInputStream(iStream, AndroidState.getInstance().getUserState().getUserData().getCipherForUser(Cipher.DECRYPT_MODE));

            iStream.skip((int)currentSegment.getFileOffset() * 2);
        }

        private void closeSegment() {
            IOUtils.closeQuietly(iStream);
            totalRead = 0;
            iStream = null;
        }

        private boolean isSegmentOpen() {
            return iStream != null;
        }
    }

    public class WavStream extends DBReadIterator {
        ByteArrayInputStream headerStream = null;

        public WavStream(Iterator<AudioSegment> segIter) throws IOException {
            super(segIter);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            AudioDB.this.writeWAVHeader(out);
            //AudioDB.this.WriteWaveFileHeader(out);
            byte[] header = out.toByteArray();
            headerStream = new ByteArrayInputStream(header);
        }

        @Override
        public int read() throws IOException {
            int currentByte = headerStream.read();
            if (currentByte == -1)
                currentByte = super.read();
            return currentByte;
        }
    }
}
