package com.example.cr.multithreading;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    private EditText editText;
    private Button button;
    private ListView listView;
    private String[] listOfImages;
    ProgressBar progressBar;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.edit_text);
        button = (Button) findViewById(R.id.button_new);
        listView = (ListView) findViewById(R.id.url_list);
        listView.setOnClickListener((View.OnClickListener) this);

        listOfImages = getResources().getStringArray(R.array.imageUrls);
        progressBar = (ProgressBar) findViewById(R.id.download_progress);
        linearLayout = (LinearLayout) findViewById(R.id.load_new_layout);
    }

    private void makeCatsDir()
    {
        String homeDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        File catDirectory = new File(homeDirectory + "/catPics");

        if (!catDirectory.exists()) catDirectory.mkdir();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        editText.setText(listOfImages[position]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public void downloadImage(View view)
    {
        String url = editText.getText().toString();

        Thread thread = new Thread(new DownloadImageUrl(url));
        thread.start();
        makeCatsDir();
    }

    public boolean downloadImageUsingThread(String url)
    {
        boolean successful = false;
        URL downloadURl = null;
        HttpURLConnection connection  = null;
        InputStream inputStream  = null;
        FileOutputStream fileOutputStream = null;
        File file = null;

        try
        {
            downloadURl = new URL(url);
            connection = (HttpURLConnection) downloadURl.openConnection();
            inputStream = connection.getInputStream();

            String homeDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
            file = new File(homeDirectory + "/catPics" +  "/" + Uri.parse(url).getLastPathSegment());
            fileOutputStream = new FileOutputStream(file);

            int read = -1;
            //read 1024 bytes per time
            byte[] buffer = new byte[1024];

            // -1 means that there is no valid value
            while ((read = inputStream.read(buffer)) != -1)
            {
                fileOutputStream.write(buffer, 0, read);

            }
            successful = true;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    linearLayout.setVisibility(View.GONE);
                }
            });

            if(connection !=null)
            {
                connection.disconnect();
            }

            if(inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                if(fileOutputStream != null)
                {
                    try
                    {
                        fileOutputStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return successful;
    }

    private class DownloadImageUrl implements Runnable
    {
        private String url;
        public DownloadImageUrl(String url)
        {
            this.url=url;
        }

        @Override
        public void run()
        {
            MainActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    linearLayout.setVisibility(View.VISIBLE);
                }
            });
            downloadImageUsingThread(url);
        }
    }
}
