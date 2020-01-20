package com.yixin.tinode.ui.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.yixin.tinode.tinode.util.UiUtils;


/**
 * Fragment for expanded display of an inline image.
 */

public class ImageViewFragment extends Fragment {

    public static ImageViewFragment newInstance(Bundle bundle) {
        ImageViewFragment fragment = new ImageViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_image, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        Bundle bundle = getArguments();
        if (activity == null || bundle == null) {
            return;
        }

        byte[] bits = bundle.getByteArray("image");
        if (bits != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(bits, 0, bits.length);
            String size = bmp.getWidth() + " \u00D7 " + bmp.getHeight() + "; ";
            ((ImageView) activity.findViewById(R.id.image)).setImageDrawable(new BitmapDrawable(getResources(), bmp));
            ((TextView) activity.findViewById(R.id.content_type)).setText(bundle.getString("mime"));
            ((TextView) activity.findViewById(R.id.file_name)).setText(bundle.getString("name"));
            ((TextView) activity.findViewById(R.id.image_size)).setText(size + UiUtils.bytesToHumanSize(bits.length));
        }
    }
}
