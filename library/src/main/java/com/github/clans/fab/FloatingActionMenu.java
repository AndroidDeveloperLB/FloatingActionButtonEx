package com.github.clans.fab;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

public class FloatingActionMenu extends ViewGroup {

    //private static final int ANIMATION_DURATION = 300;

    private static final int OPEN_UP = 0;
    private static final int OPEN_DOWN = 1;

    private static final int LABELS_POSITION_LEFT = 0;
    private static final int LABELS_POSITION_RIGHT = 1;

    private int mAnimationDuration;
    private int mButtonSpacing = Util.dpToPx(getContext(), 0f);
    private android.support.design.widget.FloatingActionButton mMenuButton;
    private int mMaxButtonWidth;
    private int mLabelsMargin = Util.dpToPx(getContext(), 0f);
    private int mLabelsVerticalOffset = Util.dpToPx(getContext(), 0f);
    private int mButtonsCount;
    private boolean mMenuOpened;
    private boolean mIsMenuOpening;
    private Handler mUiHandler = new Handler();
    private int mLabelsShowAnimation;
    private int mLabelsHideAnimation;
    private int mLabelsPaddingTop = Util.dpToPx(getContext(), 4f);
    private int mLabelsPaddingRight = Util.dpToPx(getContext(), 8f);
    private int mLabelsPaddingBottom = Util.dpToPx(getContext(), 4f);
    private int mLabelsPaddingLeft = Util.dpToPx(getContext(), 8f);
    private ColorStateList mLabelsTextColor;
    private float mLabelsTextSize;
    private int mLabelsCornerRadius = Util.dpToPx(getContext(), 3f);
    private boolean mLabelsShowShadow;
    private int mLabelsColorNormal;
    private int mLabelsColorPressed;
    private int mLabelsColorRipple;
    private int mMenuColorNormal;
    private int mMenuColorPressed;
    private int mMenuColorRipple;
    private Drawable mIcon;
    private int mAnimationDelayPerItem;
    private boolean mIsAnimated = true;
    private boolean mLabelsSingleLine;
    private int mLabelsEllipsize;
    private int mLabelsMaxLines;
    private int mLabelsStyle;
    private boolean mIconAnimated = true;
    private boolean mIsMenuButtonAnimationRunning;
    private boolean mIsSetClosedOnTouchOutside;
    private int mOpenDirection;
    private OnMenuToggleListener mToggleListener;

    private ValueAnimator mShowBackgroundAnimator;
    private ValueAnimator mHideBackgroundAnimator;
    private int mBackgroundColor;

    private int mLabelsPosition;
    private Context mLabelsContext;

    public interface OnMenuToggleListener {
        void onMenuToggle(boolean opened);
    }

    public FloatingActionMenu(Context context) {
        this(context, null);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mAnimationDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        setClipChildren(false);
        setClipToPadding(false);
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionMenu, 0, 0);
        mButtonSpacing = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_buttonSpacing, mButtonSpacing);
        mLabelsMargin = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_margin, mLabelsMargin);
        mLabelsPosition = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_position, LABELS_POSITION_LEFT);
        mLabelsShowAnimation = attr.getResourceId(R.styleable.FloatingActionMenu_menu_labels_showAnimation,
                mLabelsPosition == LABELS_POSITION_LEFT ? R.anim.fab_slide_in_from_right : R.anim.fab_slide_in_from_left);
        mLabelsHideAnimation = attr.getResourceId(R.styleable.FloatingActionMenu_menu_labels_hideAnimation,
                mLabelsPosition == LABELS_POSITION_LEFT ? R.anim.fab_slide_out_to_right : R.anim.fab_slide_out_to_left);
        mLabelsPaddingTop = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingTop, mLabelsPaddingTop);
        mLabelsPaddingRight = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingRight, mLabelsPaddingRight);
        mLabelsPaddingBottom = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingBottom, mLabelsPaddingBottom);
        mLabelsPaddingLeft = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingLeft, mLabelsPaddingLeft);
        mLabelsTextColor = attr.getColorStateList(R.styleable.FloatingActionMenu_menu_labels_textColor);
        // set default value if null same as for textview
        if (mLabelsTextColor == null) {
            mLabelsTextColor = ColorStateList.valueOf(Color.WHITE);
        }
        mLabelsTextSize = attr.getDimension(R.styleable.FloatingActionMenu_menu_labels_textSize, getResources().getDimension(R.dimen.labels_text_size));
        mLabelsCornerRadius = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_cornerRadius, mLabelsCornerRadius);
        mLabelsShowShadow = attr.getBoolean(R.styleable.FloatingActionMenu_menu_labels_showShadow, true);
        mLabelsColorNormal = attr.getColor(R.styleable.FloatingActionMenu_menu_labels_colorNormal, 0xFF333333);
        mLabelsColorPressed = attr.getColor(R.styleable.FloatingActionMenu_menu_labels_colorPressed, 0xFF444444);
        mLabelsColorRipple = attr.getColor(R.styleable.FloatingActionMenu_menu_labels_colorRipple, 0x66FFFFFF);
        mMenuColorNormal = attr.getColor(R.styleable.FloatingActionMenu_menu_colorNormal, 0xFFDA4336);
        mMenuColorPressed = attr.getColor(R.styleable.FloatingActionMenu_menu_colorPressed, 0xFFE75043);
        mMenuColorRipple = attr.getColor(R.styleable.FloatingActionMenu_menu_colorRipple, 0x99FFFFFF);
        mAnimationDelayPerItem = attr.getInt(R.styleable.FloatingActionMenu_menu_animationDelayPerItem, 50);
        mIcon = attr.getDrawable(R.styleable.FloatingActionMenu_menu_icon);
        if (mIcon == null) {
            mIcon = getResources().getDrawable(R.drawable.fab_add);
        }
        mLabelsSingleLine = attr.getBoolean(R.styleable.FloatingActionMenu_menu_labels_singleLine, false);
        mLabelsEllipsize = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_ellipsize, 0);
        mLabelsMaxLines = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_maxLines, -1);
        mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionMenu_menu_labels_style, 0);
        mOpenDirection = attr.getInt(R.styleable.FloatingActionMenu_menu_openDirection, OPEN_UP);
        mBackgroundColor = attr.getColor(R.styleable.FloatingActionMenu_menu_backgroundColor, Color.TRANSPARENT);

        if (attr.hasValue(R.styleable.FloatingActionMenu_menu_labels_padding)) {
            int padding = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_padding, 0);
            initPadding(padding);
        }

        mLabelsContext = new ContextThemeWrapper(getContext(), mLabelsStyle);

        initBackgroundDimAnimation();
        createMenuButton();

        attr.recycle();
    }

    private void initBackgroundDimAnimation() {
        final int maxAlpha = Color.alpha(mBackgroundColor);
        final int red = Color.red(mBackgroundColor);
        final int green = Color.green(mBackgroundColor);
        final int blue = Color.blue(mBackgroundColor);

        mShowBackgroundAnimator = ValueAnimator.ofInt(0, maxAlpha);
        mShowBackgroundAnimator.setDuration(mAnimationDuration);
        mShowBackgroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer alpha = (Integer) animation.getAnimatedValue();
                setBackgroundColor(Color.argb(alpha, red, green, blue));
            }
        });

        mHideBackgroundAnimator = ValueAnimator.ofInt(maxAlpha, 0);
        mHideBackgroundAnimator.setDuration(mAnimationDuration);
        mHideBackgroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer alpha = (Integer) animation.getAnimatedValue();
                setBackgroundColor(Color.argb(alpha, red, green, blue));
            }
        });
    }

    private boolean isBackgroundEnabled() {
        return mBackgroundColor != Color.TRANSPARENT;
    }

    private void initPadding(int padding) {
        mLabelsPaddingTop = padding;
        mLabelsPaddingRight = padding;
        mLabelsPaddingBottom = padding;
        mLabelsPaddingLeft = padding;
    }

    private void createMenuButton() {
        mMenuButton = new android.support.design.widget.FloatingActionButton(getContext());
        mMenuButton.setRippleColor(mMenuColorRipple);
        mMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(mIsAnimated);
            }
        });
        updateColorStates();

        addView(mMenuButton, super.generateDefaultLayoutParams());
        mMenuButton.setImageDrawable(mIcon);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;
        mMaxButtonWidth = 0;
        int maxLabelWidth = 0;

        for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) continue;

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
        }

        for (int i = 0; i < mButtonsCount; i++) {
            int usedWidth = 0;
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) continue;

            usedWidth += child.getMeasuredWidth();
            height += child.getMeasuredHeight();

            Label label = (Label) child.getTag(R.id.fab_label);
            if (label != null) {
                int labelOffset = (mMaxButtonWidth - child.getMeasuredWidth()) / 2;
                int labelUsedWidth = child.getMeasuredWidth() + label.calculateShadowWidth() + mLabelsMargin + labelOffset;
                measureChildWithMargins(label, widthMeasureSpec, labelUsedWidth, heightMeasureSpec, 0);
                usedWidth += label.getMeasuredWidth();
                maxLabelWidth = Math.max(maxLabelWidth, usedWidth + labelOffset);
            }
        }

        width = Math.max(mMaxButtonWidth, maxLabelWidth + mLabelsMargin) + getPaddingLeft() + getPaddingRight();

        height += mButtonSpacing * (mButtonsCount - 1) + getPaddingTop() + getPaddingBottom();
        height = adjustForOvershoot(height);

        if (getLayoutParams().width == LayoutParams.MATCH_PARENT) {
            width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        }

        if (getLayoutParams().height == LayoutParams.MATCH_PARENT) {
            height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int buttonsHorizontalCenter = mLabelsPosition == LABELS_POSITION_LEFT
                ? r - l - mMaxButtonWidth / 2 - getPaddingRight()
                : mMaxButtonWidth / 2 + getPaddingLeft();
        boolean openUp = mOpenDirection == OPEN_UP;

        int menuButtonTop = openUp
                ? b - t - mMenuButton.getMeasuredHeight() - getPaddingBottom()
                : getPaddingTop();
        int menuButtonLeft = buttonsHorizontalCenter - mMenuButton.getMeasuredWidth() / 2;
        mMenuButton.layout(menuButtonLeft, menuButtonTop, menuButtonLeft + mMenuButton.getMeasuredWidth(),
                menuButtonTop + mMenuButton.getMeasuredHeight());
        int nextY = openUp
                ? menuButtonTop + mMenuButton.getMeasuredHeight() + mButtonSpacing
                : menuButtonTop;

        for (int i = mButtonsCount - 1; i >= 0; i--) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) continue;

            int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
            int childY = openUp ? nextY - child.getMeasuredHeight() - mButtonSpacing : nextY;

            if (child instanceof FloatingActionButton) {
                child.layout(childX, childY, childX + child.getMeasuredWidth(),
                        childY + child.getMeasuredHeight());

                if (!mIsMenuOpening) {
                    ((FloatingActionButton) child).hide(false);
                }
            }

            View label = (View) child.getTag(R.id.fab_label);
            if (label != null) {
                int labelsOffset = (child.getMeasuredWidth() / 2) + mLabelsMargin;
                int labelXNearButton = mLabelsPosition == LABELS_POSITION_LEFT
                        ? buttonsHorizontalCenter - labelsOffset
                        : buttonsHorizontalCenter + labelsOffset;

                int labelXAwayFromButton = mLabelsPosition == LABELS_POSITION_LEFT
                        ? labelXNearButton - label.getMeasuredWidth()
                        : labelXNearButton + label.getMeasuredWidth();

                int labelLeft = mLabelsPosition == LABELS_POSITION_LEFT
                        ? labelXAwayFromButton
                        : labelXNearButton;

                int labelRight = mLabelsPosition == LABELS_POSITION_LEFT
                        ? labelXNearButton
                        : labelXAwayFromButton;

                int labelTop = childY - mLabelsVerticalOffset + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

                label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());

                if (!mIsMenuOpening) {
                    label.setVisibility(INVISIBLE);
                }
            }

            nextY = openUp
                    ? childY - mButtonSpacing
                    : childY + child.getMeasuredHeight() + mButtonSpacing;
        }
    }

    private int adjustForOvershoot(int dimension) {
        return (int) (dimension * 0.03 + dimension);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bringChildToFront(mMenuButton);
        mButtonsCount = getChildCount();
        createLabels();
    }

    private void createLabels() {

        for (int i = 0; i < mButtonsCount; i++) {
            final View child = getChildAt(i);
            if (child.getTag(R.id.fab_label) != null) continue;
            if (child instanceof FloatingActionButton)
                addLabel((FloatingActionButton) child);
        }
    }

    private void addLabel(FloatingActionButton fab) {
        String text = fab.getLabelText();

        if (TextUtils.isEmpty(text)) return;

        final Label label = new Label(mLabelsContext);
        label.setClickable(true);
        label.setFab(fab);
        label.setShowAnimation(AnimationUtils.loadAnimation(getContext(), mLabelsShowAnimation));
        label.setHideAnimation(AnimationUtils.loadAnimation(getContext(), mLabelsHideAnimation));

        if (mLabelsStyle > 0) {
            label.setTextAppearance(getContext(), mLabelsStyle);
            label.setShowShadow(false);
            label.setUsingStyle(true);
        } else {
            label.setColors(mLabelsColorNormal, mLabelsColorPressed, mLabelsColorRipple);
            label.setShowShadow(mLabelsShowShadow);
            label.setCornerRadius(mLabelsCornerRadius);
            if (mLabelsEllipsize > 0) {
                setLabelEllipsize(label);
            }
            label.setMaxLines(mLabelsMaxLines);
            label.updateBackground();

            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLabelsTextSize);
            label.setTextColor(mLabelsTextColor);

            int left = mLabelsPaddingLeft;
            int top = mLabelsPaddingTop;
            if (mLabelsShowShadow) {
                left += fab.getShadowRadius() + Math.abs(fab.getShadowXOffset());
                top += fab.getShadowRadius() + Math.abs(fab.getShadowYOffset());
            }

            label.setPadding(
                    left,
                    top,
                    mLabelsPaddingLeft,
                    mLabelsPaddingTop
            );

            if (mLabelsMaxLines < 0 || mLabelsSingleLine) {
                label.setSingleLine(mLabelsSingleLine);
            }
        }

        label.setText(text);
        label.setOnClickListener(fab.getOnClickListener());

        addView(label);
        fab.setTag(R.id.fab_label, label);
    }

    private void setLabelEllipsize(Label label) {
        switch (mLabelsEllipsize) {
            case 1:
                label.setEllipsize(TextUtils.TruncateAt.START);
                break;
            case 2:
                label.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                break;
            case 3:
                label.setEllipsize(TextUtils.TruncateAt.END);
                break;
            case 4:
                label.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                break;
        }
    }

    @Override
    public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected MarginLayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected MarginLayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT,
                MarginLayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    private void hideMenuButtonWithImage(boolean animate) {
        if (!isMenuButtonHidden()) {
            mMenuButton.animate().cancel();
            if (animate) {
                animate(false);
            } else {
                mMenuButton.animate().cancel();
                mMenuButton.setScaleY(0);
                mMenuButton.setScaleX(0);
                mMenuButton.setVisibility(View.GONE);
            }
            mIsMenuButtonAnimationRunning = false;
        }
    }

    private void animate(boolean open) {
        if (open) {
            mMenuButton.animate().scaleX(1).scaleY(1).setDuration(mAnimationDuration).setListener(new AnimatorListener() {

                @Override
                public void onAnimationEnd(final Animator animation) {
                    super.onAnimationEnd(animation);
                }
            }).start();
        } else {
            mMenuButton.animate().scaleX(0).scaleY(0).setDuration(mAnimationDuration).setListener(new AnimatorListener() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    super.onAnimationEnd(animation);
                    mMenuButton.setVisibility(View.GONE);
                }
            }).start();
        }
    }

    private void showMenuButtonWithImage(boolean animate) {
        if (isMenuButtonHidden()) {
            mMenuButton.animate().cancel();
            if (animate) {
                animate(true);
            } else {
                mMenuButton.setScaleY(1);
                mMenuButton.setScaleX(1);
            }
            mMenuButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsSetClosedOnTouchOutside) {
            return mGestureDetector.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    GestureDetector mGestureDetector = new GestureDetector(getContext(),
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    return mIsSetClosedOnTouchOutside && isOpened();
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    close(mIsAnimated);
                    return true;
                }
            });

    /* ===== API methods ===== */

    public boolean isOpened() {
        return mMenuOpened;
    }

    public void toggle(boolean animate) {
        if (isOpened()) {
            close(animate);
        } else {
            open(animate);
        }
    }

    public void open(final boolean animate) {
        if (!isOpened()) {
            if (isBackgroundEnabled()) {
                mShowBackgroundAnimator.start();
            }

            if (mIconAnimated) {
                mMenuButton.animate().cancel();
                mMenuButton.animate().scaleX(1).scaleY(1).rotation(45).setDuration(mAnimationDuration).start();
            }

            int delay = 0;
            int counter = 0;
            mIsMenuOpening = true;
            showMenuButton(animate);
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child instanceof FloatingActionButton && child.getVisibility() != GONE) {
                    counter++;

                    final FloatingActionButton fab = (FloatingActionButton) child;
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isOpened()) return;
                            fab.show(animate);
                            Label label = (Label) fab.getTag(R.id.fab_label);
                            if (label != null && label.isHandleVisibilityChanges()) {
                                label.show(animate);
                            }
                        }
                    }, delay);
                    delay += mAnimationDelayPerItem;
                }
            }

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMenuOpened = true;

                    if (mToggleListener != null) {
                        mToggleListener.onMenuToggle(true);
                    }
                }
            }, ++counter * mAnimationDelayPerItem);
        }
    }

    public void close(final boolean animate) {
        if (isOpened()||mIsMenuOpening) {
            if (isBackgroundEnabled()) {
                mHideBackgroundAnimator.start();
            }
            if (mIconAnimated) {
                mMenuButton.animate().cancel();
                mMenuButton.animate().scaleX(1).scaleY(1).rotation(0).setDuration(mAnimationDuration).start();
            }
            int delay = 0;
            int counter = 0;
            mIsMenuOpening = false;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof FloatingActionButton && child.getVisibility() != GONE) {
                    counter++;

                    final FloatingActionButton fab = (FloatingActionButton) child;
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isOpened()) return;
                            fab.hide(animate);

                            Label label = (Label) fab.getTag(R.id.fab_label);
                            if (label != null && label.isHandleVisibilityChanges()) {
                                label.hide(animate);
                            }
                        }
                    }, delay);
                    delay += mAnimationDelayPerItem;
                }
            }
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMenuOpened = false;

                    if (mToggleListener != null) {
                        mToggleListener.onMenuToggle(false);
                    }
                }
            }, ++counter * mAnimationDelayPerItem);
        }
    }

    /**
     * Sets whether open and close actions should be animated
     *
     * @param animated if <b>false</b> - menu items will appear/disappear instantly without any animation
     */
    public void setAnimated(boolean animated) {
        mIsAnimated = animated;
    }

    public boolean isAnimated() {
        return mIsAnimated;
    }

    public void setAnimationDelayPerItem(int animationDelayPerItem) {
        mAnimationDelayPerItem = animationDelayPerItem;
    }

    public int getAnimationDelayPerItem() {
        return mAnimationDelayPerItem;
    }

    public void setOnMenuToggleListener(OnMenuToggleListener listener) {
        mToggleListener = listener;
    }

    public void setIconAnimated(boolean animated) {
        mIconAnimated = animated;
    }

    public boolean isIconAnimated() {
        return mIconAnimated;
    }

    public boolean isMenuHidden() {
        return getVisibility() == INVISIBLE;
    }

    public boolean isMenuButtonHidden() {
        return mMenuButton.getVisibility() != View.VISIBLE || mMenuButton.getScaleX() != 1;
    }

    /**
     * Makes the whole {@link #FloatingActionMenu} to appear and sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void showMenu(boolean animate) {
        if (isMenuHidden()) {
            if (animate) {
                animate(true);
            }
            setVisibility(VISIBLE);
        }
    }

    /**
     * Makes the {@link #FloatingActionMenu} to disappear and sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hideMenu(final boolean animate) {
        if (!isMenuHidden() && !mIsMenuButtonAnimationRunning) {
            mIsMenuButtonAnimationRunning = true;
            if (isOpened()) {
                close(animate);
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (animate) {
                            animate(false);
                        }
                        setVisibility(INVISIBLE);
                        mIsMenuButtonAnimationRunning = false;
                    }
                }, mAnimationDelayPerItem * mButtonsCount);
            } else {
                if (animate) {
                    animate(false);
                }
                setVisibility(INVISIBLE);
                mIsMenuButtonAnimationRunning = false;
            }
        }
    }

    public void toggleMenu(boolean animate) {
        if (isMenuHidden()) {
            showMenu(animate);
        } else {
            hideMenu(animate);
        }
    }

    /**
     * Makes the {@link FloatingActionButton} to appear inside the {@link #FloatingActionMenu} and
     * sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void showMenuButton(boolean animate) {
        if (isMenuButtonHidden()) {
            showMenuButtonWithImage(animate);
        }
    }

    /**
     * Makes the {@link FloatingActionButton} to disappear inside the {@link #FloatingActionMenu} and
     * sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hideMenuButton(final boolean animate) {
        if (!isMenuButtonHidden() && !mIsMenuButtonAnimationRunning) {
            mIsMenuButtonAnimationRunning = true;
            if (isOpened()||mIsMenuOpening) {
                close(animate);
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideMenuButtonWithImage(animate);
                    }
                }, mAnimationDelayPerItem * mButtonsCount);
            } else {
                hideMenuButtonWithImage(animate);
            }
        }
    }

    public void toggleMenuButton(boolean animate) {
        if (isMenuButtonHidden()) {
            showMenuButton(animate);
        } else {
            hideMenuButton(animate);
        }
    }

    public void setClosedOnTouchOutside(boolean close) {
        mIsSetClosedOnTouchOutside = close;
    }

    public void setMenuButtonColorNormal(int color) {
        mMenuColorNormal = color;
        updateColorStates();
    }

    private void updateColorStates() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed}, // pressed
                new int[]{}  // default
        };
        int[] colors = new int[]{
                mMenuColorPressed,
                mMenuColorNormal
        };
        mMenuButton.setBackgroundTintList(new ColorStateList(states, colors));
    }

    public void setMenuButtonColorNormalResId(int colorResId) {
        mMenuColorNormal = getResources().getColor(colorResId);
        updateColorStates();
    }

    public int getMenuButtonColorNormal() {
        return mMenuColorNormal;
    }

    public void setMenuButtonColorPressed(int color) {
        mMenuColorPressed = color;
        updateColorStates();
    }

    public void setMenuButtonColorPressedResId(int colorResId) {
        mMenuColorPressed = getResources().getColor(colorResId);
        updateColorStates();
    }

    public int getMenuButtonColorPressed() {
        return mMenuColorPressed;
    }

    public void setMenuButtonColorRipple(int color) {
        mMenuColorRipple = color;
        mMenuButton.setRippleColor(color);
    }

    public void setMenuButtonColorRippleResId(int colorResId) {
        mMenuColorRipple = getResources().getColor(colorResId);
        mMenuButton.setRippleColor(mMenuColorRipple);
    }

    public int getMenuButtonColorRipple() {
        return mMenuColorRipple;
    }

    public void addMenuButton(FloatingActionButton fab) {
        addView(fab, mButtonsCount - 1);
        mButtonsCount++;
        addLabel(fab);
    }

    public void removeMenuButton(FloatingActionButton fab) {
        removeView(fab.getLabelView());
        removeView(fab);
        mButtonsCount--;
    }

    public void addMenuButton(FloatingActionButton fab, int index) {
        int size = mButtonsCount - 1;
        if (index < 0) {
            index = 0;
        } else if (index > size) {
            index = size;
        }

        addView(fab, index);
        mButtonsCount++;
        addLabel(fab);
    }

    public void removeAllMenuButtons() {
        close(true);

        List<FloatingActionButton> viewsToRemove = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v != mMenuButton && v instanceof FloatingActionButton) {
                viewsToRemove.add((FloatingActionButton) v);
            }
        }
        for (FloatingActionButton v : viewsToRemove) {
            removeMenuButton(v);
        }
    }

    public void setOnMenuButtonClickListener(OnClickListener clickListener) {
        mMenuButton.setOnClickListener(clickListener);
    }
}
