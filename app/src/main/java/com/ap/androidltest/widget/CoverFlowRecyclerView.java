package com.ap.androidltest.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.ap.androidltest.R;
import com.ap.androidltest.widget.decoration.InsetDecoration;

/**
 * Created by AP on 01/12/14.
 * This view helps to use RecyclerView as Gallery. By default it uses InsetDecoration as an item
 * decoration with offset equals to 50.
 */
@SuppressWarnings("UnusedDeclaration")
public class CoverFlowRecyclerView extends RecyclerView {

    private static final String TAG = CoverFlowRecyclerView.class.getSimpleName();
    private InsetDecoration mDefaultDecoration;
    private CoverFlowLayoutManager mLayoutManager;
    private RecyclerView.OnScrollListener mScrollListener;
    private OnItemClickListener mItemClickListener;
    private int mCenteredPosition = 0;
    private OnCenteredPositionChangedListener mCenteredPositionChangedListener;

    public CoverFlowRecyclerView(Context context) {
        this(context, null);
    }

    public CoverFlowRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverFlowRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Initialize all required components
     */
    private void init(Context context, AttributeSet attrs) {
        // use a linear layout manager
        mLayoutManager = new CoverFlowLayoutManager();
        setLayoutManager(mLayoutManager);
        mDefaultDecoration = new InsetDecoration(0, 50);
        addItemDecoration(mDefaultDecoration);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GalleryRecyclerView, 0, 0);
        try {
            float minScale = ta.getFloat(R.styleable.GalleryRecyclerView_minScale, -1);
            float minAlpha = ta.getFloat(R.styleable.GalleryRecyclerView_minAlpha, -1);
            float maxZ = ta.getFloat(R.styleable.GalleryRecyclerView_maxZ, -1);
            if (minScale != -1) setMinimumScale(minScale);
            if (minAlpha != -1) setMinimumAlpha(minAlpha);
            if (maxZ != -1) setMaxZ(maxZ);
        } finally {
            ta.recycle();
        }
    }

    /**
     * Set a new adapter to provide child views on demand.
     * <p/>
     * When adapter is changed, all existing views are recycled back to the pool. If the pool has
     * only one adapter, it will be cleared.
     *
     * @param adapter The new adapter to set, or null to set no adapter.
     * @see #swapAdapter(android.support.v7.widget.RecyclerView.Adapter, boolean)
     */
    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        setCenteredPosition(0);
    }

    /**
     * Set the minimum scale for the elements which are not centered.
     *
     * @param minScale minimum scale. Should be in a range [0, 1]
     */
    public void setMinimumScale(float minScale) {
        mLayoutManager.setMinimumScale(minScale);
    }

    /**
     * Set the minimum alpha for the elements which are not centered.
     *
     * @param minAlpha minimum alpha. Should be in a range [0, 1]
     */
    public void setMinimumAlpha(float minAlpha) {
        mLayoutManager.setMinimumAlpha(minAlpha);
    }

    /**
     * Set the maximum translation z which will be used to show floating effect for centered item.
     * NB! Currently only works on Android 5.0 and newer version
     *
     * @param maxZ maximum translation z. Should be bigger than or equal to 0
     */
    public void setMaxZ(float maxZ) {
        mLayoutManager.setMaxZ(maxZ);
    }

    /**
     * Returns currently used layout manager in Gallery
     *
     * @return LayoutManager
     */
    @Override
    public LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    /**
     * Set new layout manager which extends CoverFlowLayoutManager
     *
     * @param layout CoverFlowLayoutManager
     */
    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (layout instanceof CoverFlowLayoutManager) {
            mLayoutManager = (CoverFlowLayoutManager) layout;
        } else {
            Log.e(TAG, "You can set only CoverFlowLayoutManager or class which extends that");
        }
        super.setLayoutManager(layout);
    }

    /**
     * Return default decoration used in GalleryRecyclerView
     *
     * @return Default decoration
     */
    public InsetDecoration getDefaultDecoration() {
        return mDefaultDecoration;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        super.setOnScrollListener(new OnScrollListener());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        super.setOnScrollListener(null);
    }

    /**
     * Set the scroll listener
     *
     * @param listener RecyclerView.OnScrollListener
     */
    @Override
    public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
        mScrollListener = listener;
    }

    /**
     * Scroll to position (which will be centered) with animation
     *
     * @param position new position which will be centered
     */
    @Override
    public void smoothScrollToPosition(int position) {
        smoothScrollBy(mLayoutManager.getOffsetToItem(position), 0);
    }

    /**
     * Scroll to position (which will be centered) without animation
     *
     * @param position new position which will be centered
     */
    @Override
    public void scrollToPosition(int position) {
        scrollBy(mLayoutManager.getOffsetToItem(position), 0);
    }

    /**
     * Called from GalleryViewHolder to modify centered position or notify about already centered
     * item click
     *
     * @param view        View which was clicked
     * @param position    Position of the new centered item
     * @param isLongClick was it long click or usual click
     * @return in case of long click this should return that it was handled or not
     */
    boolean onItemClick(View view, int position, boolean isLongClick) {
        int centeredPosition = mLayoutManager.getCurrentCenteredPosition();
        if (centeredPosition == position) {
            if (mItemClickListener != null) {
                if (isLongClick) return mItemClickListener.onItemLongClick(view, position);
                else mItemClickListener.onItemClick(view, position);
            }
        } else {
            smoothScrollToPosition(position);
        }
        return false;
    }

    /**
     * Set the item click and long click listener
     *
     * @param listener OnItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Set the listener to track centered position change
     *
     * @param listener OnCenteredPositionChangedListener
     */
    public void setOnCenteredPositionChangedListener(OnCenteredPositionChangedListener listener) {
        mCenteredPositionChangedListener = listener;
    }

    /**
     * Returns current centered position
     *
     * @return centered position index
     */
    public int getCenteredPosition() {
        return mCenteredPosition;
    }

    /**
     * Set current centered position and notify listener about position change
     *
     * @param newCenteredPosition position of the new centered item
     */
    private void setCenteredPosition(int newCenteredPosition) {
        if (newCenteredPosition == mCenteredPosition) return;
        mCenteredPosition = newCenteredPosition;
        if (mCenteredPositionChangedListener != null)
            mCenteredPositionChangedListener.onCenteredPositionChanged(mCenteredPosition);
    }

    /**
     * Interface fot the listener to track item click and long click
     */
    public static interface OnItemClickListener {
        public void onItemClick(View view, int position);

        public boolean onItemLongClick(View view, int position);
    }

    /**
     * Interface fot the listener to track centered position change
     */
    public static interface OnCenteredPositionChangedListener {
        public void onCenteredPositionChanged(int newCenteredPosition);
    }

    /**
     * Gallery RecyclerView scroll listener
     */
    private class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mScrollListener != null) mScrollListener.onScrolled(recyclerView, dx, dy);
            setCenteredPosition(mLayoutManager.getCurrentCenteredPosition());
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (mScrollListener != null)
                mScrollListener.onScrollStateChanged(recyclerView, newState);
            if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                int newPosition = mLayoutManager.getCurrentCenteredPosition();
                smoothScrollBy(mLayoutManager.getOffsetToItem(newPosition), 0);
                setCenteredPosition(newPosition);
            }
        }
    }
}