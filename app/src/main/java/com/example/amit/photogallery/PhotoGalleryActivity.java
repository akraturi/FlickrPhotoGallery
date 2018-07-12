package com.example.amit.photogallery;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/** THE ACTIVITY IS A SINGLEFRAGMENT ACTIVITY SUBCLASS AND IT WILL HOST A SINGLE FRAGMENT
 *
 */

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
   protected Fragment createFragment()
    {
        return PhotoGalleryFragment.newInstance();
    }
}
