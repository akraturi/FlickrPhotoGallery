package com.example.amit.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//This class object will prepare a looper

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG="ThumbnailDownloader";
    //message associated with the Message object, this will identify the message as download request
    private static final int MESSAGE_DOWNLOAD=0;

    private boolean mHasQuit=false;
    // mRequestHandler will store a reference to the Handler responsible for queueing
    //download requests as messages onto the ThumbnailDownloader background thread. This handler will
    //also be in charge of processing download request messages when they are pulled off the queue.
    private Handler mRequestHandler;
    //Variable to hold the reference of the Handler passed by the main thread
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownload(T target,Bitmap thumbnail);
    }
    public void setmThumbnailDownloadListener(ThumbnailDownloadListener listener)
    {
        mThumbnailDownloadListener=listener;
    }

    // A thread safe version of hash map
    private ConcurrentMap<T,String> mRequestMap=new ConcurrentHashMap<>();
    public ThumbnailDownloader(Handler responseHandler)
    {
        super(TAG);
        mResponseHandler=responseHandler;
    }
    //The method inside the handler decodes the message and put it into own messagequeue
    @Override
    protected void onLooperPrepared()
    {
        mRequestHandler=new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what==MESSAGE_DOWNLOAD)
                {
                    T target=(T)msg.obj;
                    Log.i(TAG,"Got a request for URL="+mRequestMap.get(target));
                    handleRequest(target);

                }

            }
        };
    }
    //Method that signals that the thread has quit
    @Override
    public boolean quit()
    {
        mHasQuit=true;
        return super.quit();
    }
    //Method which clear all the messages
    public void clearQueue()
    {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    public void queueThumbnail(T target,String url)
    {
        Log.i(TAG,"Got an url:-"+url);
        //Here We put the object as key and the url associated with it in the map
        //from handler we get obtain a message which creates a message object with msg,obj and the handler calling to it and it
        //is sent to the the messageQueue
        if(url==null){
            mRequestMap.remove(target);
        }
        else{
            mRequestMap.put(target,url);
            //this message object represents a download request for the specified target object which will be Photoholdr in this
            //case. Message itself here does not include the downloading url but we put it in the map and it will be pulled out
            //of it so that we can get the most recent request so it is also important
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
        }
    }
    //this method downloads the image from given url as Bytes and converts it to Bitmap
    private void handleRequest(final T target)
    {
        try{
            final String url=mRequestMap.get(target);
            if(url==null)
            {
                return;
            }
            byte[] bitmapBytes= new FlickerFetch().getUrlBytes(url);
            final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG,"Bitmap created");
            //Responding back to UI thread
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target)!=url||mHasQuit)
                    {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownload(target,bitmap);
                }
            });

        }catch (IOException ioe)
        {
            Log.e(TAG,"Failed to download image",ioe);
        }
    }

}
