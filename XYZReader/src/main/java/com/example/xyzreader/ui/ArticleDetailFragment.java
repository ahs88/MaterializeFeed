package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,NestedScrollView.OnScrollChangeListener,AppBarLayout.OnOffsetChangedListener {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private boolean isVisibleToUser;
    private NestedScrollView bodyContainer;
    private int headerHeight;
    private int minHeaderTranslation;
    private LinearLayout headerView;
    private TextView titleView;
    private TextView bylineView;
    private int toolbarTitleLeftMargin;
    private TextView articleTitleToolBar;
    private AppBarLayout appBar;
    private int headerHeightPx;
    private int minOffsetHeight;
    private Toolbar mToolbar;
    private int mCurrentToolbarColor;
    private int mCurrentToolbarTitleColor;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        //setHasOptionsMenu(true);
        headerHeight = (int)getResources().getDimensionPixelSize(R.dimen.header_height);

// The height of your fully collapsed header view. Actually the Toolbar height (56dp)
        int minHeaderHeight = (int)getResources().getDimensionPixelSize(R.dimen.app_bar_height);
        minOffsetHeight = headerHeight - minHeaderHeight-100;
// The left margin of the Toolbar title (according to specs, 72dp)
        toolbarTitleLeftMargin = getResources().getDimensionPixelSize(R.dimen.toolbar_left_margin);

// Added after edit



    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(getActivity().getResources().getColor(R.color.color_primary));
        }*/
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

                /*((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);*/
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) getActivity()).supportFinishAfterTransition();
            }
        });
        mToolbar.setTitle("");
        //setupToolBar();
        appBar = (AppBarLayout)mRootView.findViewById(R.id.app_bar);
        if(getActivity() instanceof DetailScreenInterface && isVisibleToUser)
            ((DetailScreenInterface)getActivity()).viewLoaded(appBar);

        setHasOptionsMenu(true);

        mStatusBarColorDrawable = new ColorDrawable(0);
        articleTitleToolBar = (TextView)mRootView.findViewById(R.id.article_title_toolbar);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        //updateStatusBar();

        return mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
       // mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            Log.d(TAG,"root View is null");

            return;
        }


        bodyContainer = (NestedScrollView)mRootView.findViewById(R.id.scrollLayout);
        //headerHeight = getResources().getDimensionPixelSize(R.dimen.detail_title_text_size);
        //minHeaderTranslation = -headerHeight +
         //       getResources().getDimensionPixelOffset(R.dimen.app_bar_height);

        headerView = (LinearLayout)mRootView.findViewById(R.id.meta_bar);
        
        bodyContainer.setOnScrollChangeListener(this);

        appBar = (AppBarLayout)mRootView.findViewById(R.id.app_bar);

        /*if(getActivity() instanceof DetailScreenInterface && isVisibleToUser)
            ((DetailScreenInterface)getActivity()).viewLoaded(appBar);*/

        appBar.addOnOffsetChangedListener(this);
        titleView = (TextView) mRootView.findViewById(R.id.article_title);
        bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
      //  bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            articleTitleToolBar.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            articleTitleToolBar.setVisibility(View.GONE);
            Log.d(TAG,"bindViews title:"+mCursor.getString(ArticleLoader.Query.TITLE)+" photo_url:"+mCursor.getString(ArticleLoader.Query.PHOTO_URL)+" isVisibleToUser:"+isVisibleToUser);

            setupToolBar();
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

            //Log.d(TAG,"article string:"+mCursor.getString(ArticleLoader.Query.BODY));


            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));


            //need to use color palette here
            Picasso.with(getActivity()).load(mCursor.getString(ArticleLoader.Query.PHOTO_URL)).into(target);




            /*ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                *//*mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);*//*
                                //updateStatusBar();

                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });*/
        } else {
            mRootView.setVisibility(View.GONE);
            //titleView.setText("N/A");
            //bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }


    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.d(TAG,"onBitmap Loaded");
            mPhotoView.setImageBitmap(bitmap);
            paletteGenerator(bitmap,12);
            if(getActivity() instanceof DetailScreenInterface)
            {
                ((DetailScreenInterface)getActivity()).imageLoaded();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.d(TAG,"onBitmap failed");
            if(getActivity() instanceof DetailScreenInterface)
            {
                ((DetailScreenInterface)getActivity()).imageLoaded();
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d(TAG,"onPrepare Load");
            mPhotoView.setImageDrawable(placeHolderDrawable);
            if(getActivity() instanceof DetailScreenInterface)
            {
                ((DetailScreenInterface)getActivity()).imageLoaded();
            }
        }
    };

    public void paletteGenerator( Bitmap bitmap,int colorCount){
        Palette.from(bitmap).maximumColorCount(colorCount).generate(new Palette.PaletteAsyncListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onGenerated(Palette palette) {

                // Get the "vibrant" color swatch based on the bitmap
                Palette.Swatch vibrant = palette.getVibrantSwatch();

                if (vibrant != null) {
                    // Set the background color of a layout based on the vibrant color
                    mCurrentToolbarColor = vibrant.getRgb();


                    // Update the title TextView with the proper text color
                    mCurrentToolbarTitleColor = vibrant.getTitleTextColor();
                    //Log.d(TAG,"palette onGenerated:mCurrentToolbarTitleColor:"+Integer.toHexString(mCurrentToolbarTitleColor)+" mCurrentToolbarColor:"+Integer.toHexString(mCurrentToolbarColor));
                    if(getActivity()!=null && isVisibleToUser) {
                        getActivity().getWindow().setStatusBarColor(mCurrentToolbarColor);
                    }
                }
            }
        });

    }

    public interface DetailScreenInterface{
        public void viewLoaded(AppBarLayout appBarLayout);

        void imageLoaded();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG,"onLoadFinished");
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(TAG,"onLoaderReset");
        mCursor = null;
        //bindViews();
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser){
            Log.d(TAG,"setUserVisibleHint:");
               // getLoaderManager().initLoader(0, null, this);
                //setupToolBar();
        }

        this.isVisibleToUser = isVisibleToUser;
        if(getActivity()!=null && mCursor!=null) {
            Picasso.with(getActivity()).load(mCursor.getString(ArticleLoader.Query.PHOTO_URL)).into(target);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG,"onHiddenChanged:"+hidden);
    }

    public void setupToolBar(){
        if(isVisibleToUser && mCursor != null) {
            Log.d(TAG,"setting up  toolbar");
            if(collapsingToolbarLayout == null) {
                collapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.toolbar_layout);
            }
            collapsingToolbarLayout.setTitle("");
            collapsingToolbarLayout.setTitleEnabled(false);
        }


    }

    public void setupStatusAndNavigation(){

    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

     /*   headerView.setTranslationY(Math.max(0, scrollY + minHeaderTranslation));

        // Scroll ratio (0 <= ratio <= 1). 
        // The ratio value is 0 when the header is completely expanded, 
        // 1 when it is completely collapsed



        // Now that we have this ratio, we only have to apply translations, scales,
        // alpha, etc. to the header views

        // For instance, this will move the toolbar title & subtitle on the X axis 
        // from its original position when the ListView will be completely scrolled
        // down, to the Toolbar title position when it will be scrolled up.
        titleView.setTranslationX(toolbarTitleLeftMargin * offset);
        bylineView.setTranslationX(toolbarTitleLeftMargin * offset);*/
        float offset = 1 - Math.max(
                (float) (-minHeaderTranslation - scrollY) / -minHeaderTranslation, 0f);
        Log.d(TAG,"onScrollChange offset:"+offset);

    }

    public enum State {
        EXPANDED,
        COLLAPSED,
        TRANSITION
    }

    public State mCurrentState ;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        Log.d(TAG,"verticalOffset:"+verticalOffset+" offsetheight:"+minOffsetHeight);

        if (verticalOffset == 0) {
            if (mCurrentState != State.EXPANDED) {
                articleTitleToolBar.setVisibility(View.GONE);
                mToolbar.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
            }
            mCurrentState = State.EXPANDED;
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                articleTitleToolBar.setVisibility(View.VISIBLE);
                mToolbar.setBackgroundColor(mCurrentToolbarColor);

            }
            mCurrentState = State.COLLAPSED;
        } else {
            if (mCurrentState != State.TRANSITION) {
                articleTitleToolBar.setVisibility(View.GONE);
                mToolbar.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
                //getActivity().getWindow().setStatusBarColor(mCurrentToolbarC);
            }
            mCurrentState = State.TRANSITION;
        }
    }



}
