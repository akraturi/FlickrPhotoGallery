package com.example.amit.photogallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/** THIS FRAGMENT WILL DISPLAY THE GRID OF PHOTOS FROM RECYCLERVIEW AND WILL BE HOSTED BY PHOTOGALLERYACTIVITY
 *
 */
public class PhotoGalleryFragment extends VisibleFragment {
     private RecyclerView mPhotoRecyclerView;
     private List<GalleryItem> mItems=new ArrayList<>();
     private static final String TAG="photogallerytag";
     private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
     private Activity activity;

    //static method to return an object of this class
    public static PhotoGalleryFragment newInstance()
    {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        activity= getActivity();

        // starting polling for new search results using a background service
//        Intent i = PollService.newIntent(activity);
//        activity.startService(i);

        // turn the alarm manager on to start the service , every 1 minute to poll for new search results
         PollService.setServiceAlarm(activity,true);

        //On rotation if the fragment is distroyed the AsyncTask is not fired again to fetch the data
        //but the old data is retained
        setRetainInstance(true);
        // enable the fragment to recieve the menu callbacks
        setHasOptionsMenu(true);
        //Initiating the background thread using the AsyncTask subclass
        updateView();
        //initiate the image downloading thread
        //this handler will automatically be attached with the looper of the UI thread
      /*  Handler mResponseHandler=new Handler();
        mThumbnailDownloader=new ThumbnailDownloader<>(mResponseHandler);
        mThumbnailDownloader.setmThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownload(PhotoHolder holder, Bitmap thumbnail) {
                Drawable drawable=new BitmapDrawable(getResources(),thumbnail);
                holder.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"Background thread started for downloading ");*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mPhotoRecyclerView=(RecyclerView)view.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        //Initially setup the adapter when fragment view is created
        setUpAdapter();
        return view;
    }
    //Setup a View holder for recycler view
    private class PhotoHolder extends RecyclerView.ViewHolder{
       // private TextView mTitleTextView;
        private ImageView mImageView;
        public PhotoHolder(View itemView)
        {
            super(itemView);

            //mTitleTextView=(TextView)itemView;
            mImageView=(ImageView)itemView.findViewById(R.id.item_image_view);

        }
        /*//This method is called after background process is completed in UI thread so this is the
        //best place to update UI after the background thread is completed
        public  void bindGalleryItem(GalleryItem item)
        {
            mTitleTextView.setText(item.toString());
        }*/
        // This image binds the downloaded image to the viewholder of recyclerview
        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }

    }
    // Adapter to connect recycler view with the fetched dataset
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        private List<GalleryItem> mGalleryItems;
        public PhotoAdapter(List<GalleryItem> galleryItems)
        {
            mGalleryItems=galleryItems;
        }
        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //TextView textView=new TextView(getActivity());
            LayoutInflater inflater= LayoutInflater.from(getActivity());
            View view= inflater.inflate(R.layout.list_item_gallery,parent,false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
           GalleryItem galleryItem=mGalleryItems.get(position);
           /*// holder.bindGalleryItem(galleryItem);
            Drawable placeholder=getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);
            //queue download for current photo holder
            mThumbnailDownloader.queueThumbnail(holder,galleryItem.getmUrl());*/
            /**
             * DOWNLOADING IMAGES AND SETTING THEM WITH PICASO LIBRARY
             */
            // initial place holder
//            Picasso.get().load(galleryItem.getmUrl()).placeholder(R.drawable.bill_up_close).into(holder.mImageView);
            Picasso.get().load(galleryItem.getmUrl()).into(holder.mImageView);

        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

    }
    //This private inner class extends the AsyncTask to create a background thread to do the networking
    private class FetchItemTask extends AsyncTask<Void,Void,List<GalleryItem>>{
        private String mQuery;

        public FetchItemTask(String query)
        {
            mQuery = query;
        }


        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
          /*  try{
                String result=new FlickerFetch().getUrlString("http://www.google.co.in");
                Log.i(TAG,"Fetched content of url"+result);
            }catch (IOException ioe){
                Log.e(TAG,"Failed to fetch the content",ioe);
            }*/

          //String query = "robot"; // sample search query for searching purpose

            if(mQuery == null)// No search query fetching only the recent photos
            {
              return new FlickerFetch().fetchRecentPhotos();
            }
            else // Fetching with a search query
            {
              return new FlickerFetch().searchPhotos(mQuery);
            }

        }
        @Override
        protected void onPostExecute(List<GalleryItem> items)
        {
            mItems=items;
            activity.setProgressBarIndeterminateVisibility(false);
            setUpAdapter();
        }
    }

    /** THE METHOD SETS THE ADAPTER TO THE RECYCLERVIEW
     * IT IS CONFIRMED BEFORE ATTACHING THE ADAPTER THAT FRAGMENT IS ATTACHED TO ACTIVITY TO AVOID NULL RETURNED FROM GETACTIVITY
     * IT IS SO BECAUSE NOW THERE ARE NOT ONLY CALLBACKS FROM FRAMWORK BUT A BACKGROUND THREAD IS ALSO INVOLVED SO WE CANNOT
     * ASSUME THAT THE FRAGMENT IS ALWAYS ATTACHED TO THE ACTIVITY AS THE EARLIER CASE
     */
    private void setUpAdapter()
    {
        if(isAdded())
        {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //Background thread which was downloading image is quit here,if we don't quit it
        //it will keep running in the background zombie
        //mThumbnailDownload.quit();
        //This clears any downloads before destroying the app
//        mThumbnailDownloader.clearQueue();
        Log.i(TAG,"Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_photo_gallery,menu);

        // get the searchview from the toolbar or actionbar and set a listener to it on any query

        MenuItem searchItem =menu.findItem(R.id.menu_item_search);

        final android.support.v7.widget.SearchView searchView =(android.support.v7.widget.SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("text submited:",query);
                // save the query to the preferences
                QueryPreferences.setStoredQuery(getActivity(),query);
                // update view based upon the search query
                updateView();
                return true; // signifies that the search request has been handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("text changed:",newText);
                return false;
            }
        });
        // pre populating the search box with saved query
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             String savedQuery = QueryPreferences.getStoredQuery(getActivity());
             searchView.setQuery(savedQuery,false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.menu_item_search:
                return true;
            case R.id.menu_item_clear:
                // clear the search preferences
                QueryPreferences.setStoredQuery(getActivity(),null);
                // update the view
                updateView();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                // contents of menu has been changed menu is to be redrawn
                // to set the toggling
                activity.invalidateOptionsMenu();
                return true;
             default:
                 return super.onOptionsItemSelected(item);
        }

    }

    // update the view
    private void updateView() {
        String query = QueryPreferences.getStoredQuery(getActivity());
       new FetchItemTask(query).execute();
    }

    public static Intent newIntent(Context context)
    {
        return new Intent(context,PhotoGalleryFragment.class);
    }
}