package com.entradahealth.entrada.android.app.personal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * A base class for progress-dialog-based asynchronous tasks. When
 * instantiated, creates a dialog box with a spinner.
 */
public abstract class DialogTask<Result> extends AsyncTask<Void, DialogTask.ProgressUpdate, Result>
{
    protected ProgressDialog dialog;

    protected final Activity activity;

    protected DialogTask(Activity activity, CharSequence title, CharSequence defaultText, boolean isCancelable)
    {
        this.activity = activity;
        this.dialog = new ProgressDialog(activity);

        this.dialog.setTitle(title);
        this.dialog.setMessage(defaultText);
        this.dialog.setIndeterminate(true);
        this.dialog.setCancelable(isCancelable);
        this.dialog.setCanceledOnTouchOutside(false);

        this.dialog.show();
    }

    public ProgressDialog getDialog()
    {
        return dialog;
    }

    protected void progress(ProgressUpdateType type, CharSequence content)
    {
        publishProgress(new ProgressUpdate(type, content));
    }
    protected void progress(CharSequence content)
    {
        publishProgress(new ProgressUpdate(ProgressUpdateType.DIALOG, content));
    }

    @Override
    protected void onProgressUpdate(ProgressUpdate... values)
    {
        super.onProgressUpdate(values);
        if (values.length > 0)
        {
            //Log.e("Entrada-DialogTask", "Received " + values.length + " progress updates at once.");
        }

        ProgressUpdate update = values[0];
        if (ProgressUpdateType.TOAST.equals(update.type))
        {
            Toast.makeText(activity, update.content, 4000).show();
        }
        else
        {
            this.dialog.setMessage(update.content);
        }
    }

    @Override
    protected void onPostExecute(Result result)
    {
        super.onPostExecute(result);
        this.dialog.hide();
    }

    public static class ProgressUpdate
    {
        public final ProgressUpdateType type;
        public final CharSequence content;

        public ProgressUpdate(ProgressUpdateType type, CharSequence content)
        {
            this.type = type;
            this.content = content;
        }
    }
    public enum ProgressUpdateType
    {
        DIALOG,
        TOAST
    }
}
