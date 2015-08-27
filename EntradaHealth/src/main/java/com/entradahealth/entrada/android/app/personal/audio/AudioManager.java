package com.entradahealth.entrada.android.app.personal.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.entradahealth.entrada.android.app.personal.AndroidState;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * TODO: Document this!
 *
 * @author edwards
 * @since 12/5/12
 */
public class AudioManager {
    private static final String LOG_TITLE = "Entrada-AudioManager";

    public static final int CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    //public static final int CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    public static final int SAMPLE_RATE_HZ = 44100;
    //public static final int SAMPLE_RATE_HZ = 16000;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	public static int BUFFER_SIZE = AudioRecord.getMinBufferSize(
			SAMPLE_RATE_HZ, CHANNELS, ENCODING);

    // One "unit" = one tick of the playback scrubber on the job list view.
    public static final int BYTES_PER_UNIT = BUFFER_SIZE;
    public static final int SAMPLES_PER_UNIT = BYTES_PER_UNIT / 2;

    public static int MAX_SEGMENT_LENGTH = SAMPLE_RATE_HZ * 15;

    private boolean stream_mode = true;
    
    private AudioState state = AudioState.IDLE;
    private AudioProcessor currentProcessor = null;
    MediaRecorder mRec = null;
    private AudioDB data;
    RatingBar rating;
    private boolean isSpeakerOn = true;
    private boolean hasChanges = false;

    static{
		String currentapiVersion = android.os.Build.VERSION.RELEASE;
		Log.e("", "currentapiversion--"+currentapiVersion);
		if (Integer.parseInt(currentapiVersion.split("\\.")[0]) < 5){
			BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ, CHANNELS, ENCODING);
		} else{
			BUFFER_SIZE = (int) (AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ, CHANNELS, ENCODING)/2);
		}
    }
	
    public enum AudioState {
        IDLE,
        PLAYING,
        RECORDING
    }

    public interface AudioProgressMonitor {
        void setSampleCursor(long samples);
        void stopped();
    }
    
    

    public abstract class AudioProcessor extends AsyncTask<Void, Long, Void> {
        protected long offset;
        protected AudioProgressMonitor monitor;

        public void setMonitor(AudioProgressMonitor m) {
            this.monitor = m;
        }

        @Override
        protected void onProgressUpdate(Long... progress) {
            if (this.monitor != null) this.monitor.setSampleCursor(progress[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            AudioManager.this.stop();
            if (this.monitor != null) this.monitor.stopped();
        }

        @Override
        protected void onCancelled(Void result) {
            onPostExecute(result);
        }
    }

    public class AudioRecorder extends AudioProcessor {
        boolean overwrite;
        private AudioRecord recorder = null;
        AudioRecorder(long offset, boolean overwrite) {
            this.offset = offset;
            this.overwrite = overwrite;
            
        }

        int i = 1;
        @Override
        protected Void doInBackground(Void... args) {
            //byte[] buffer = new byte[AudioManager.BUFFER_SIZE];
        	byte[] buffer = new byte[BUFFER_SIZE];
            OutputStream writer = null;
            Log.i(LOG_TITLE, String.format("Recording started. Buffer size = %d.", AudioManager.BUFFER_SIZE));
            try {

                recorder = new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        AudioManager.SAMPLE_RATE_HZ,
                        AudioManager.CHANNELS,
                        AudioManager.ENCODING,
                        AudioManager.BUFFER_SIZE);
                
                

                assert recorder.getState() == AudioRecord.STATE_INITIALIZED;
                
                recorder.startRecording();
                while (!this.isCancelled()) { // isCanceled() handles the stop button.

                    // Create a new segment and open its file.
                    AudioSegment segment = new AudioSegment();

                    File writerPath = new File(AudioManager.this.data.getBasePath(), segment.getFileName());
                    writer = new FileOutputStream(writerPath, true);
                    writer = new BufferedOutputStream(writer);
                    writer = new CipherOutputStream(writer, AndroidState.getInstance().getUserState().getUserData().getCipherForUser(Cipher.ENCRYPT_MODE));

                    // Add the segment to the DB. offset is the start of the current segment.
                    AudioManager.this.data.addSegment(segment, offset);

                    // While we haven't stopped recording, and the current segment is less than
                    // the max segment length...
                    String currentapiVersion = android.os.Build.VERSION.RELEASE;
                    if (Integer.parseInt(currentapiVersion.split("\\.")[0]) < 5){
            			MAX_SEGMENT_LENGTH = MAX_SEGMENT_LENGTH * 2;
            		} else{
                    	MAX_SEGMENT_LENGTH = MAX_SEGMENT_LENGTH;
            		}
                    
                    for (int totalBytesRead = 0; totalBytesRead < MAX_SEGMENT_LENGTH  && !this.isCancelled(); ) {

                        // Read audio data into buffer until we fill it up to AudioManager.BUFFER_SIZE.
                        int bytesRead = 0;
                        double sum = 0;
              		  	double amplitude = 0.0;
              		  	
                        do {
                        	//amplitude = 0.0;
                        	 if (Integer.parseInt(currentapiVersion.split("\\.")[0]) < 5){
                        		 bytesRead = recorder.read(buffer, bytesRead, AudioManager.BUFFER_SIZE - bytesRead);                        		 
                        	 } else {
                        		 bytesRead = recorder.read(buffer, 0, (AudioManager.BUFFER_SIZE));
                        	 }
                            sum += buffer [i] * buffer [i]  * buffer [i] ;
                            amplitude = sum / bytesRead;
                            rating.setRating((float)amplitude*5);
                            
                    		//i++;
                            
                    		
                        } while (bytesRead < buffer.length);

                        // Write the buffer out to the segment file.
                        writer.write(buffer, 0, bytesRead);
                        
                        

                        // Extend the segment by the number of SAMPLES (not bytes) we just read
                        segment.addLength(bytesRead / 2);

                        // Account for the total number of bytes read into the current segment
                        totalBytesRead += bytesRead;
                        
                        // Publish our progress to update the seekbar and time labels
                        //publishProgress(Long.valueOf((offset + segment.getLength()) / SAMPLES_PER_UNIT));
                        
                    }
                    
                    


                    Log.d(LOG_TITLE, "Finished segment.");

                    // If we're in overwrite mode, delete from the end of this segment for the length of the segment
                    // AKA all of the data that would have been overwritten.
                    if (this.overwrite)
                        AudioManager.this.data.eraseRegion(offset + segment.getLength(), segment.getLength());

                    // Increment the offset so that the next segment records at the end of the one we just finalized
                    offset += segment.getLength();

                    // Close the segment file.
                    writer.close();
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
            }
            Log.i(LOG_TITLE, "Recording complete.");

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
        }
    }
    
    

    class AudioPlayer extends AudioProcessor {
        AudioPlayer(long offset) {
            this.offset = offset;
        }

        @Override
        protected Void doInBackground(Void... args) {
            if (AudioManager.this.data.isEmpty()) return null;

            AudioTrack player = null;
            byte[] buffer = new byte[AudioManager.BUFFER_SIZE];

            Log.i(LOG_TITLE, "Playback started");
            try {
            	
            	if(isSpeakerOn)
            		player = new AudioTrack(android.media.AudioManager.STREAM_MUSIC,
            				SAMPLE_RATE_HZ,
            				CHANNELS,
            				ENCODING,
            				BUFFER_SIZE,
            				AudioTrack.MODE_STREAM);
            	else
            		player = new AudioTrack(android.media.AudioManager.STREAM_VOICE_CALL,
            				SAMPLE_RATE_HZ,
            				CHANNELS,
            				ENCODING,
            				BUFFER_SIZE,
            				AudioTrack.MODE_STREAM);

                
                player.play();
                int totalWritten = 0;

                AudioDB.DBReadIterator iter = AudioManager.this.data.dbIterator();
                iter.skipSamples(this.offset);
                while (iter.hasNext() && !this.isCancelled()) {
                    ByteBuffer buf = iter.next();
                    // TODO: Decrypt this!
                    totalWritten += player.write(buf.array(), 0, buf.remaining()) / 2;
                    publishProgress(Long.valueOf(offset + totalWritten) / SAMPLES_PER_UNIT);
                }
            } catch (IOException e) {
                e.printStackTrace();  // TODO: Handle this exception type.
            } finally {
                if (player != null) {
                    player.stop();
                    player.release();
                }
            }
            Log.i(LOG_TITLE, "Playback complete.");
            return null;
        }
    }


    public static AudioManager loadOrCreate(File path) throws IOException {
        AudioManager db = null;
        File dbpath = new File(path, AudioDB.DB_FILE_NAME);
        if (dbpath.isFile()) {
            db = AudioManager.load(path);
            Log.d(LOG_TITLE, String.format("AudioManager loaded from %s with %d segments.",
                    path.toString(), db.data.size()));
        } else if (!dbpath.exists()) {
            db = new AudioManager(path);
            Log.d(LOG_TITLE, "New AudioManager created at " + path.toString());
        } else {
            throw new FileNotFoundException("Cannot load AudioManager for account: " +
                    AudioDB.DB_FILE_NAME +
                    " already exists and is not a file.");
        }
        assert db != null;
        return db;
    }

    public void save() throws IOException {
        hasChanges = false;
        this.data.save();
    }

    public static AudioManager load(File path) throws IOException {
        AudioDB db = AudioDB.load(path);
        return new AudioManager(path, db);
    }

    private AudioManager(File path, AudioDB data) {
        this.data = data;
        Log.i(LOG_TITLE, "AudioManager loaded from file.");
    }

    private AudioManager(File path) {
        this.data = new AudioDB(path);
        Log.i(LOG_TITLE, "AudioManager initialized.");
    }

    /**
     * Truncates the recording to the given length.
     *
     * @param startSample The global sample at which to truncate the whole recording.
     */
    public void truncateRecording(long startSample) {
        hasChanges = true;
        data.truncate(startSample * SAMPLES_PER_UNIT);
    }

    /**
     * Begins a recording session.
     *
     * @param startSample Where to begin recording. Range is 0 (beginning) to 1 (end)
     */
    public AudioProcessor beginRecord(long startSample, boolean overwrite, RatingBar rating) {
        if (this.state != AudioState.IDLE) {
            throw new IllegalStateException("Recording cannot begin while in state " + this.state.toString());
        }
        hasChanges = true;
        this.state = AudioState.RECORDING;
        this.currentProcessor = new AudioRecorder(startSample * SAMPLES_PER_UNIT, overwrite);
        this.currentProcessor.execute();
        this.rating = rating;

        return this.currentProcessor;
    }

    /**
     * Begins playback of the whole recording.
     *
     * @param startSample Where to begin playback. Range is 0 (beginning) to 1 (end)
     */
    public AudioProcessor beginPlayback(long startSample, boolean isSpeakerOn) {
        if (this.state != AudioState.IDLE) {
            throw new IllegalStateException("Playback cannot begin while in state " + this.state.toString());
        }
        this.state = AudioState.PLAYING;
        this.currentProcessor = new AudioPlayer(startSample * SAMPLES_PER_UNIT);
        this.currentProcessor.execute();
        this.isSpeakerOn = isSpeakerOn;
        return this.currentProcessor;
    }

    /**
     * Stops recording or playback, if either are running.
     */
    public void stop() {
        if (this.state == AudioState.IDLE) return;
        this.state = AudioState.IDLE;
        assert this.currentProcessor != null;
        this.currentProcessor.cancel(false);
        this.currentProcessor = null;
    }

    public long getRecordingLength() {
        return this.data.getTotalLength() / SAMPLES_PER_UNIT;
    }

    public long getDurationInMS() {
        return this.data.getDurationInMS();
    }

    public long getDurationInSecs() {
        return getDurationInMS() / 1000;
    }

    public long getSamplesInMS(long sample) {
        return this.data.getSamplesInMS(sample * SAMPLES_PER_UNIT);
    }

    public boolean hasUnsavedChanges() {
        return this.hasChanges;
    }

    public AudioState getState() {
        return this.state;
    }

}
