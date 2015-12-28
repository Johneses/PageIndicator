package com.mobator.pageindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.BaseInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 12/28/15.
 * Please add lines to this archive with all the love in the universe!
 * If you have problems with the code written right over here, please mail me: joao@ideianoar.com.br
 */
public class PageIndicator extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private static final int ANIMATION_DURATION = 700;
    private static final int ID_MULTIPLIER = 881;

    private static final String[] STATES = new String[]{ "SCROLL_STATE_IDLE", "SCROLL_STATE_DRAGGING", "SCROLL_STATE_SETTLING" };

    private int mNumberOfIndicators = 0;
    private int mSelectedIndicator = 0;
    private int mIndicatorSize = 0;
    private int mIndicatorMarginLeft = 0;
    private int mAnimationDuration = ANIMATION_DURATION;
    @DrawableRes private int mBackgroundEmptyIndicator = 0;
    @DrawableRes private int mBackgroundSelectedIndicator = 0;
    private BaseInterpolator mAnimationInterpolator = new DecelerateInterpolator();

    private List<View> mIndicatorList = new ArrayList<>();
    private View mCurrentIndicator = null;

    private ViewPager mViewPager;

    public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initiateBeatifulIndicator(context, attrs);
    }

    public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initiateBeatifulIndicator(context, attrs);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initiateBeatifulIndicator(context, attrs);
    }

    public PageIndicator(Context context) {
        super(context);
        initiateBeatifulIndicator(context, null);
    }

    public void initiateBeatifulIndicator(@NonNull  Context context, AttributeSet attributeSet) {
        if(attributeSet != null) {
            TypedArray attrs = context.getTheme().obtainStyledAttributes(
                    attributeSet,
                    R.styleable.PageIndicator,
                    0, 0
            );

            try {
                mNumberOfIndicators = attrs.getInt(R.styleable.PageIndicator_number_of_indicators, 0);
                mSelectedIndicator = attrs.getInt(R.styleable.PageIndicator_selected_indicator, 0);
                mIndicatorSize = attrs.getDimensionPixelSize(R.styleable.PageIndicator_indicator_size, getContext().getResources().getDimensionPixelSize(R.dimen.normal_indicator_size));
                mIndicatorMarginLeft = attrs.getDimensionPixelSize(R.styleable.PageIndicator_indicator_margin_left, getContext().getResources().getDimensionPixelSize(R.dimen.normal_margin_left));
                mBackgroundEmptyIndicator = attrs.getResourceId(R.styleable.PageIndicator_background_empty_indicator, R.drawable.empty_indicator);
                mBackgroundSelectedIndicator = attrs.getResourceId(R.styleable.PageIndicator_background_selected_indicator, R.drawable.current_indicator);
                mAnimationDuration = attrs.getInt(R.styleable.PageIndicator_animation_duration, ANIMATION_DURATION);
            } catch (Exception e) {
                mNumberOfIndicators = 0;
                mSelectedIndicator = 0;
                mIndicatorSize = 0;
                mIndicatorMarginLeft = 0;
                mBackgroundEmptyIndicator = 0;
                mBackgroundSelectedIndicator = 0;
                mAnimationDuration = 0;
            }
        }

        buildIndicatorView();
    }

    private void buildIndicatorView() {
        Context context = getContext();

        mIndicatorList.clear();
        removeAllViews();

        for(int i = 0; i < mNumberOfIndicators; i++) {
            View view = new View(context);

            LayoutParams layoutParams = new LayoutParams(mIndicatorSize, mIndicatorSize);
            if(mIndicatorList.size() == 0) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            } else {
                layoutParams.addRule(RelativeLayout.RIGHT_OF, mIndicatorList.get(mIndicatorList.size() - 1).getId());
                layoutParams.setMargins(mIndicatorMarginLeft, 0, 0, 0);
            }
            view.setLayoutParams(layoutParams);
            view.setId((i + 1) * ID_MULTIPLIER);
            view.setBackgroundResource(mBackgroundEmptyIndicator);

            addView(view);

            mIndicatorList.add(view);
        }

        mCurrentIndicator = new View(context);
        LayoutParams layoutParams = new LayoutParams(mIndicatorSize, mIndicatorSize);
        if(mSelectedIndicator == 0) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        } else {
            layoutParams.addRule(RelativeLayout.RIGHT_OF, mIndicatorList.get(mSelectedIndicator - 1).getId());
            layoutParams.setMargins(mIndicatorMarginLeft, 0, 0, 0);
        }
        mCurrentIndicator.setLayoutParams(layoutParams);
        mCurrentIndicator.setBackgroundResource(mBackgroundSelectedIndicator);

        addView(mCurrentIndicator, getChildCount() - 1);
        mCurrentIndicator.bringToFront();
    }

    public void setUpWithViewPager(@NonNull ViewPager viewPager) {
        mViewPager = viewPager;
        setCurrentPageOnViewPager(mSelectedIndicator);
        viewPager.addOnPageChangeListener(this);
    }

    public void setCurrentPosition(int position) {
        if(!isPositionValid(position)) {
            throw new InvalidParameterException("position must be > 0 && < " + (mIndicatorList.size() - 1));
        }
        animateCurrentIndicator(position);
        setCurrentPageOnViewPager(position);
    }

    public void setEmptyIndicatorBackground(@DrawableRes int backgroundResource) {
        mBackgroundEmptyIndicator = backgroundResource;
        buildIndicatorView();
    }

    public void setCurrentIndicatorBackground(@DrawableRes int backgroundResource) {
        mBackgroundSelectedIndicator = backgroundResource;
        buildIndicatorView();
    }

    public void setNumberOfIndicators(int numberOfIndicators) {
        setNumberOfIndicators(numberOfIndicators, 0);
    }

    public void setNumberOfIndicators(int numberOfIndicators, int selectedIndicator) {
        if(numberOfIndicators <= 0) {
            throw new InvalidParameterException("numberOfIndicators must be > 0");
        } else if(selectedIndicator < 0 && selectedIndicator > numberOfIndicators - 1) {
            throw new InvalidParameterException("position must be > 0 && < " + (numberOfIndicators - 1));
        }
        mNumberOfIndicators = numberOfIndicators;
        mSelectedIndicator = selectedIndicator;
        buildIndicatorView();

        setCurrentPageOnViewPager(selectedIndicator);
    }

    public void setIndicatorSize(int pixelSize) {
        if(pixelSize <= 0) {
            throw new InvalidParameterException("pixelSize must be > 0");
        }
        mIndicatorSize = pixelSize;
        buildIndicatorView();
    }

    public void setAnimationDuration(int animationDuration) {
        if(animationDuration <= 0) {
            throw new InvalidParameterException("animationDuration must be > 0");
        }
        mAnimationDuration = animationDuration;
    }

    public void setIndicatorMarginLeft(int marginLeftPixels) {
        if(marginLeftPixels < 0) {
            throw new InvalidParameterException("marginLeftPixels must be > 0");
        }
        mIndicatorMarginLeft = marginLeftPixels;
        buildIndicatorView();
    }

    public void setAnimationInterpolator(@NonNull  BaseInterpolator animationInterpolator) {
        mAnimationInterpolator = animationInterpolator;
    }

    private void setCurrentPageOnViewPager(int position) {
        if(mViewPager != null) {
            mViewPager.setCurrentItem(position, true);
        }
    }

    private boolean isPositionValid(int position) {
        return position <= mIndicatorList.size() - 1 && position > 0;
    }

    private void animateCurrentIndicator(int position) {
        View whereToGo = mIndicatorList.get(position);

        mCurrentIndicator.animate()
                .x(whereToGo.getX())
                .setInterpolator(mAnimationInterpolator)
                .setDuration(mAnimationDuration)
                .start();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.i("BeatifulIndicator", "Current viewPager position: " + position);
        Log.i("BeatifulIndicator", "Current viewPager positionOffset: " + positionOffset);
        Log.i("BeatifulIndicator", "Current viewPager positionOffsetPixels: " + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        Log.i("BeatifulIndicator", "Page selected: " + position);
        animateCurrentIndicator(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.i("BeatifulIndicator", "State of viewPager: " + STATES[state]);
    }
}
