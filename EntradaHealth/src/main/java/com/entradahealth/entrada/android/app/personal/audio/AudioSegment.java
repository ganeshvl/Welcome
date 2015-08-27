package com.entradahealth.entrada.android.app.personal.audio;

import android.util.Log;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * TODO: Document this!
 *
 * @author edwards
 * @since 12/27/12
 */
public class AudioSegment {
    String filename;
    long sample_length;
    long file_offset;

    public AudioSegment() {
        this.sample_length = 0;
        this.file_offset = 0;
        this.filename = UUID.randomUUID().toString();
    }

    @JsonCreator
    public AudioSegment(@JsonProperty("filename") String filename,
                        @JsonProperty("length") long sample_length,
                        @JsonProperty("offset") long file_offset) {
        this.filename = filename;
        this.sample_length = sample_length;
        this.file_offset = file_offset;
    }

    @JsonProperty("filename")
    public String getFileName() {
        return this.filename;
    }

    @JsonProperty("offset")
    public long getFileOffset() {
        return this.file_offset;
    }

    @JsonProperty("length")
    public long getLength() {
        return this.sample_length;
    }

    AudioSegment duplicate() {
        AudioSegment ret = new AudioSegment();
        ret.filename = this.filename;
        ret.sample_length = this.sample_length;
        ret.file_offset = this.file_offset;
        return ret;
    }

    public void addLength(long len) {
        this.sample_length += len;
    }

    public void setLength(long len) {
        this.sample_length = len;
    }

    public void trimFront(long samples) {
        assert samples < sample_length;

        String oldSegStr = this.toString();
        file_offset += samples;
        sample_length -= samples;
        Log.i(AudioDB.LOG_TITLE, String.format("Trimmed %d from front of AudioSegment. %s => %s",
                samples, oldSegStr, this.toString()));
    }

    public void trimBack(long samples) {
        assert samples < sample_length;

        String oldSegStr = this.toString();
        sample_length -= samples;
        Log.i(AudioDB.LOG_TITLE, String.format("Trimmed %d from back of AudioSegment. %s => %s",
                samples, oldSegStr, this.toString()));
    }

    @Override
    public String toString() {
        return String.format("[%s->%s]", file_offset, sample_length);
    }
}
