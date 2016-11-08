package com.samsung.smcl.vr.widgets;

import java.util.List;

import org.joml.Vector3f;

import com.samsung.smcl.utility.Log;
/**
 * A Layout that arranges its children in a single column or a single row. The direction of the row can be
 * set by calling setOrientation(). The default orientation is horizontal. The alignment of all items can
 * be specified by calling {@link LinearLayout#setGravity()} or specify that the children fill up any remaining
 * space in the layout by setting gravity to FILL. The default gravity is {@link Gravity#CENTER}.
 *
 * The size of the layout determines the viewport size (virtual area used by the list rendering engine) if the
 * flag {@link LinearLayout#mApplyViewport} is set. Otherwise all items are rendered in the list even if they
 * occupy larger space  than the container size is. The unlimited size can be specified for the layout. For
 * layout with unlimited size only {@link Gravity#CENTER} can be applied.
 */
public class LinearLayout extends Layout {

    /**
     * Orientation specifies if the items lay out along its local x-axis (
     * {@link Orientation#HORIZONTAL}) or it's local y-axis (
     * {@link Orientation#VERTICAL}) or it's local z-axis (
     * {@link Orientation#STACK}).
     * The default orientation is horizontal.
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL, STACK
    }

    /**
     * Gravity specifies how an layout should position its content along orientation axe, within its own bounds.
     * {@link Gravity#CENTER} is applied by default.
     * The gravity makes sense only if the layout content is not scrollable and all items can be fitted
     * into the container. If the container is defined with unlimited size along the orientation axis, only
     * {@link Gravity#CENTER} is supported.
     *
     * {@link Gravity#CENTER} Place the items in the center of the container in the vertical axis for
     * {@link Orientation#VERTICAL} and horizontal axis for {@link Orientation#HORIZONTAL}

     * {@link Gravity#LEFT} Push the items to the left of the container for {@link Orientation#HORIZONTAL}
     * It is not supported for {@link Orientation#VERTICAL}
     *
     * {@link Gravity#RIGHT} Push the items to the right of the container for {@link Orientation#HORIZONTAL}
     * It is not supported for {@link Orientation#VERTICAL}
     *
     * {@link Gravity#TOP} Push the items to the top of the container for {@link Orientation#VERTICAL}
     * It is not supported for {@link Orientation#HORIZONTAL}
     *
     * {@link Gravity#BOTTOM} Push the items to the bottom of the container for {@link Orientation#VERTICAL}
     * It is not supported for {@link Orientation#HORIZONTAL}
     *
     * {@link Gravity#FRONT} Push the items to the front of the container for {@link Orientation#STACK}
     *
     * {@link Gravity#BACK} Push the items to the back of the container for {@link Orientation#STACK}
     *
     * {@link Gravity#FILL} Calculate the divider amount, so the items completely fill the container. The
     * items size will not be changed.
     */
    public enum Gravity {
        LEFT,
        RIGHT,
        CENTER,
        TOP,
        BOTTOM,
        FRONT,
        BACK,
        FILL
    }

    private static final String pattern = "\nLL attributes====== orientation = %s gravity = %s divider_padding = %s uniformSize = %b size [%s]";

    /**
     * Return the string representation of the LinearLayout
     */
    public String toString() {
        return super.toString() + String.format(pattern, mOrientation, mGravity, mDividerPadding, mUniformSize, mViewPort);
    }

    public LinearLayout() {
        super();
        initCache();
    }

    /**
     * When set to true, all items in layout will be considered having the size of the largest child. If false, all items are
     * measured normally. Disabled by default.
     * @param enable  true to measure children using the size of the largest child, false - otherwise.
     */
    public void enableUniformSize(final boolean enable) {
        if (mUniformSize != enable) {
            mUniformSize = enable;
            invalidate();
        }
    }

    /**
     * @return {@link Orientation} of the layout.
     */
    public Orientation getOrientation() {
        return mOrientation;
    }

    /**
     * @return {@link Gravity} of the layout.
     */
    public Gravity getGravity() {
        return mGravity;
    }

    /**
     * Set the {@link Orientation} of the layout. The new orientation can be rejected if it is in conflict with the
     * currently applied Gravity
     *
     * @param orientation
     *            One of the {@link Orientation} constants.
     */
    public void setOrientation(final Orientation orientation) {
        if (orientation != mOrientation && isValidLayout(mGravity, orientation)) {
            mOrientation = orientation;
        }
        invalidate();
    }

    /**
     * Set the {@link Gravity} of the layout.
     * The new gravity can be rejected if it is in conflict with the currently applied Orientation
     *
     * @param gravity
     *            One of the {@link Gravity} constants.
     */
    public void setGravity(final Gravity gravity) {
        if (gravity != mGravity && isValidLayout(gravity, mOrientation)) {
            mGravity = gravity;
            invalidate();
        }
    }

    public void setDividerPadding(float padding, final Axis axis) {
        if (axis == getOrientationAxis()) {
            super.setDividerPadding(padding, axis);
        } else {
            Log.w(TAG, "Cannot apply divider padding for wrong axis [%s], orientation = %s",
                  axis, mOrientation);
        }
    }

    /**
     * Check if the layout is unlimited along the orientation axe
     */
    protected boolean isUnlimitedSize() {
        return mViewPort != null && mViewPort.get(getOrientationAxis()) == Float.POSITIVE_INFINITY;
    }

    /**
     * Check if the gravity and orientation are not in conflict one with other.
     * @param gravity
     * @param orientation
     * @return true if orientation and gravity can be applied together, false - otherwise
     */
    protected boolean isValidLayout(Gravity gravity, Orientation orientation) {
        boolean isValid = true;

        switch (gravity) {
            case TOP:
            case BOTTOM:
                isValid = (!isUnlimitedSize() && orientation == Orientation.VERTICAL);
                break;
            case LEFT:
            case RIGHT:
                isValid = (!isUnlimitedSize() && orientation == Orientation.HORIZONTAL);
                break;
            case FRONT:
            case BACK:
                isValid = (!isUnlimitedSize() && orientation == Orientation.STACK);
                break;
            case FILL:
                isValid = !isUnlimitedSize();
                break;
            case CENTER:
                break;
            default:
                isValid = false;
                break;
        }
        if (!isValid) {
            Log.w(TAG, "Cannot set the gravity %s and orientation %s - " +
                    "due to unlimited bounds or incompatibility", gravity, orientation);
        }
        return isValid;
    }

    /**
     * Calculate the layout offset
     * @param  axis {@link LayoutStrategy#Axis}
     */
    protected float getLayoutOffset() {
        final int offsetSign = getOffsetSign();
        final float axisSize = getAxisSize(getOrientationAxis());
        float layoutOffset = -offsetSign * axisSize / 2;
        if (LOGGING_VERBOSE) {
            Log.d(TAG, "getLayoutOffset(): dimension: %5.2f, layoutOffset: %5.2f",
              axisSize, layoutOffset);
        }
        return layoutOffset;
    }

    /**
     * Calculate the starting content offset based on the layout orientation and Gravity
     * @param totalSize total size occupied by the content
     */
    protected float getStartingOffset(final float totalSize) {
        final int offsetSign = getOffsetSign();
        final float axisSize = getAxisSize(getOrientationAxis());
        float startingOffset = 0;

        if (axisSize > totalSize || !mApplyViewPort) {
            switch (mGravity) {
                case LEFT:
                case TOP:
                case FRONT:
                case FILL:
                    startingOffset = -offsetSign * axisSize / 2;
                    break;
                case RIGHT:
                case BOTTOM:
                case BACK:
                    startingOffset = offsetSign * (axisSize / 2 - totalSize);
                    break;
                case CENTER:
                    startingOffset = -offsetSign * totalSize / 2;
                    break;
                default:
                    Log.w(TAG, "Cannot calculate starting offset: " +
                            "gravity %s is not supported!", mGravity);
                    break;
            }
        } else {
            // if the content occupies more space than the viewport size is, content is aligned to left
            startingOffset = -offsetSign * axisSize / 2;
        }

        Log.d(TAG, "getStartingOffset(): totalSize: %5.2f, dimension: %5.2f, startingOffset: %5.2f",
              totalSize, axisSize, startingOffset);

        return startingOffset;
    }

    /**
     * Get the axis along the orientation
     * @return
     */
    protected Axis getOrientationAxis() {
        final Axis axis;
        switch(mOrientation) {
            case HORIZONTAL:
                axis = Axis.X;
                break;
            case VERTICAL:
                axis = Axis.Y;
                break;
            case STACK:
                axis = Axis.Z;
                break;
            default:
                Log.w(TAG, "Unsupported orientation %s", mOrientation);
                axis = Axis.X;
                break;
        }
        return axis;
    }

    /**
     * Get the total number of records in the cache data set(s)
     * @return
     */
    protected int getCacheCount() {
        return mCache.count();
    }

    protected float getDataOffset(final int dataIndex) {
        return mCache.getDataOffset(dataIndex);
    }


    @Override
    protected boolean isInvalidated() {
        return !mContainer.isDynamic() ? mCache.count() != mContainer.size() :
            false;
    }

    protected Vector3f getFactor() {
        Vector3f factor = new Vector3f();
        Axis axis = getOrientationAxis();
        switch(axis) {
            case X:
                factor.x = 1;
                break;
            case Y:
                factor.y = -1;
                break;
            case Z:
                factor.z = 1;
                break;
        }
        return factor;
    }

    protected void dumpCaches() {
        if (mCache != null) {
            mCache.dump();
        }
    }

    @Override
    protected void layoutChildren() {
        if (LOGGING_VERBOSE) {
            dumpCaches();
        }
        super.layoutChildren();
    }

    @Override
    protected float getMeasuredChildSizeWithPadding(final int dataIndex, final Axis axis) {
        return axis != getOrientationAxis() ? Float.NaN :
            getMeasuredChildSizeWithPadding(dataIndex, mCache);
    }

    protected float getMeasuredChildSizeWithPadding(final int dataIndex, CacheDataSet cache) {
        return cache.getSizeWithPadding(dataIndex);
    }

    @Override
    protected Widget measureChild(final int dataIndex) {
        return measureChild(dataIndex, mCache);
    }

    @Override
    protected int getCenterChild() {
        return getCenterChild(mCache);
    }

    @Override
    protected Direction getDirectionToChild(final int dataIndex, final Axis axis) {
        Direction direction = Direction.NONE;
        int centerId = getCenterChild();
        if (axis == getOrientationAxis() && centerId != dataIndex &&
                dataIndex >= 0 && dataIndex < mContainer.size()) {
            direction = dataIndex > centerId ? Direction.FORWARD :
                Direction.BACKWARD;
        }
        return direction;
    }

    @Override
    protected float getDistanceToChild(int dataIndex, Axis axis) {
        return getDistanceToChild(dataIndex, axis, mCache);
    }

    protected int getNextDataId(final Axis axis, final Direction direction) {
        int dataIndex = -1;

        if (axis == getOrientationAxis()) {
            switch(direction) {
                case BACKWARD: {
                    dataIndex = getCacheCount() == 0 ? 0 :
                        getFirstDataIndex() - 1;
                    if (dataIndex >= 0) {
                        //
                    }
                    break;
                }
                case FORWARD: {
                    dataIndex = getCacheCount() == 0 ? 0 :
                        getLastDataIndex() + 1;
                    if (dataIndex >= mContainer.size()) {
                        dataIndex = -1;
                        //
                    }
                    break;
                }
                case NONE:
                    break;
            }
            if (LOGGING_VERBOSE) {
                Log.d(TAG, "dataIndex = %d mCache.count() = %d",
                      dataIndex, getCacheCount());
            }
        }
        return dataIndex;
    }


    protected int getFirstDataIndex() {
        return mCache.getId(0);
    }

    protected int getLastDataIndex() {
        return mCache.getId(getCacheCount() - 1);
    }

    @Override
    protected float preMeasureNext(final List<Widget> measuredChildren,
            final Axis axis, final Direction direction) {
        float totalSize = Float.NaN;
        int dataIndex = getNextDataId(axis, direction);
        Widget widget = null;

        if (dataIndex >= 0) {
            widget = measureChild(dataIndex);
            totalSize = (direction == Direction.BACKWARD ? 1 : -1) *
                    getMeasuredChildSizeWithPadding(dataIndex, axis);
            if (widget != null && measuredChildren != null) {
                measuredChildren.add(widget);
            }
        }
        return totalSize;
    }

    @Override
    protected boolean postMeasurement() {
        return postMeasurement(mCache);
    }

    protected float getDistanceToChild(int dataIndex, Axis axis, CacheDataSet cache) {
        float distance = Float.NaN;
        int centerIndex = getCenterChild();
        if (centerIndex > 0 && axis == getOrientationAxis() && cache.contains(dataIndex)) {
           distance = cache.getDataOffset(centerIndex) -
                   cache.getDataOffset(dataIndex);
        }
        return distance;
    }

    protected int getCenterChild(CacheDataSet cache) {
        int id = cache.getId(0);
        if (LOGGING_VERBOSE) {
            Log.d(TAG, "getCenterChild = %d ", id);
        }
        return id;
    }

    protected Widget measureChild(final int dataIndex, CacheDataSet cache) {
        // measure and setup size for new item
        if (isChildMeasured(dataIndex)) {
            Log.w(TAG, "Item [%d] has been already measured!", dataIndex);
        } else {
            float size = getChildSize(dataIndex, getOrientationAxis());

            // add at the end by default
            int pos = cache.count();
            int firstIndex = getFirstDataIndex();
            if (firstIndex >= 0 && dataIndex < firstIndex) {
                pos = 0;
            } else {
            // pos in the middle  TODO: figure out why it does not work
            // pos = cache.searchPos(dataIndex);
            }

            if (LOGGING_VERBOSE) {
                Log.d(TAG, "measureChild [%d] has been added at pos [%d]! cache.count() = %d",
                  dataIndex, pos, cache.count());
            }
            cache.addData(dataIndex, pos, size, getDivider() / 2, getDivider() / 2);
        }
        computeOffset(dataIndex, cache);
        return super.measureChild(dataIndex);
    }

    protected boolean postMeasurement(CacheDataSet cache) {
        // if uniform size feature is enabled - uniform size for all items in the cache
        // uniform size is supported for static dataset only.
        if (!mContainer.isDynamic() && mUniformSize) {
            cache.uniformSize();
        }

        if (mGravity == Gravity.FILL) {
            if (mApplyViewPort && cache.getTotalSize() >= getAxisSize(getOrientationAxis())) {
                // reset padding for all items if size of the data exceeds the view port
                cache.uniformPadding(0);
            } else {
                // if uniform padding feature is enabled - uniform padding for all items in the cache
                cache.uniformPadding(computeUniformPadding(cache));
            }
        }
        return computeOffset(cache);
    }

    @Override
    public void shiftBy(final float offset, final Axis axis) {
        if (!Float.isNaN(offset) && axis == getOrientationAxis()) {
            mCache.shiftBy(offset);
        }
    }

    /**
     * Compute the offset for the item in the layout cache
     * @return true if the item fits the container, false otherwise
     */
    protected boolean computeOffset(final int dataIndex, CacheDataSet cache) {
        // offset computation: update offset for all items in the cache
        float layoutOffset = getLayoutOffset();
        float sign = getOffsetSign();

        int pos = cache.getPos(dataIndex);
        float startDataOffset = Float.NaN;
        float endDataOffset = Float.NaN;
        if (pos > 0) {
            int id = cache.getId(pos - 1);
            if (id != -1) {
                startDataOffset = cache.getEndDataOffset(id);
                if (!Float.isNaN(startDataOffset)) {
                    endDataOffset = cache.setDataOffsetAfter(dataIndex, startDataOffset, sign);
                }
            }
        } else if (pos == 0) {
            int id = cache.getId(pos + 1);
            if (id != -1) {
                endDataOffset = cache.getStartDataOffset(id);
                if (!Float.isNaN(endDataOffset)) {
                    startDataOffset = cache.setDataOffsetBefore(dataIndex, endDataOffset, sign);
                }
            } else {
                startDataOffset = layoutOffset;
                endDataOffset = cache.setDataOffsetAfter(dataIndex, startDataOffset, sign);
            }
        }

        if (Layout.LOGGING_VERBOSE) {
            Log.d(TAG, "computeOffset [%d, %d]: startDataOffset = %f endDataOffset = %f",
                  dataIndex, pos, startDataOffset, endDataOffset);
        }
        boolean inBounds = !Float.isNaN(cache.getDataOffset(dataIndex)) &&
                Math.abs(endDataOffset) <= Math.abs(layoutOffset) &&
                Math.abs(startDataOffset) <= Math.abs(layoutOffset);

        return inBounds;
    }

    /**
     * Compute the offset for the item in the layout based on the offsets of neighbors
     * in the layout. The other offsets are not patched. If neighbors offsets have not
     * been computed the offset of the item will not be set.
     * @return true if the item fits the container, false otherwise
     */
    protected boolean computeOffset(CacheDataSet cache) {
        // offset computation: update offset for all items in the cache
        float startDataOffset = getStartingOffset((cache.getTotalSizeWithPadding()));
        float layoutOffset = getLayoutOffset();
        float sign = getOffsetSign();

        boolean inBounds = Math.abs(startDataOffset) <= Math.abs(layoutOffset);

        for (int pos = 0; pos < cache.count(); ++pos) {
            int id = cache.getId(pos);
            if (id != -1) {
                float endDataOffset = cache.setDataOffsetAfter(id, startDataOffset, sign);
                inBounds = inBounds &&
                        Math.abs(endDataOffset) <= Math.abs(layoutOffset) &&
                        Math.abs(startDataOffset) <= Math.abs(layoutOffset);
                startDataOffset = endDataOffset;
  //              layoutChild(id);
            }
        }

        return inBounds;
    }

    @Override
    protected boolean inViewPort(final int dataIndex) {
        return inViewPort(dataIndex, mCache);
    }

    protected boolean inViewPort(final int dataIndex, CacheDataSet cache) {
        float startDataOffset = cache.getStartDataOffset(dataIndex);
        float endDataOffset = cache.getEndDataOffset(dataIndex);

        float layoutOffset = getLayoutOffset();
        return (Math.abs(endDataOffset) <= Math.abs(layoutOffset) ||
                Math.abs(startDataOffset) <= Math.abs(layoutOffset));
    }

    /**
     * Compute the proportional padding for all items in the cache
     * @param cache Cache data set
     * @return the uniform padding amount
     */
    protected float computeUniformPadding(final CacheDataSet cache) {
        float axisSize = getAxisSize(getOrientationAxis());
        float totalPadding = axisSize - cache.getTotalSize();
        float uniformPadding = totalPadding > 0 && cache.count() > 1 ?
                totalPadding / (cache.count() - 1)  : 0;
        return uniformPadding;
    }

    /**
     * Calculate the padding between the items in the layout. It depends on the layout settings
     * @return  padding
     */
    protected float getDivider() {
        return mGravity == Gravity.FILL ? 0 :
            getDividerPadding(getOrientationAxis());
    }

    /**
     * @return the offset sign applied to the child positioning
     */
    protected int getOffsetSign() {
        return 1;
    }

    @Override
    public void invalidate() {
        invalidateCache();
        super.invalidate();
    }

    protected void invalidateCache() {
        mCache.invalidate();
    }

    protected void invalidateCache(final int dataIndex) {
        mCache.removeData(dataIndex);
    }

    @Override
    protected void invalidate(final int dataIndex) {
        Log.d(TAG, "invalidate item [%d]", dataIndex);
        invalidateCache(dataIndex);
        super.invalidate(dataIndex);
    }

    @Override
    protected void layoutChild(final int dataIndex) {
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            final Vector3f factor = getFactor();
            final float childOffset = getDataOffset(dataIndex);
            if (LOGGING_VERBOSE) {
                Log.d(TAG, "positionChild [%d] %s : childOffset = [%f] factor: [%s] layout: %s",
                  dataIndex, child.getName(), childOffset, factor, this);
            }
            updateTransform(child, factor, childOffset);
        } else {
            Log.w(TAG, "positionChild: child with dataIndex [%d] was not found in layout: %s",
                  dataIndex, this);
        }

        super.layoutChild(dataIndex);
    }

    protected void updateTransform(Widget child, final Vector3f factor, float childOffset) {
        // keep it in front of the parent to avoid z-fighting
        // TODO: use /setOffsetUnits/setOffsetFactor

        float position = childOffset * factor.x +
                childOffset * factor.y +
                (childOffset + 0.1f) * factor.z;

        if (LOGGING_VERBOSE) {
            Log.d(TAG, "setPosition [%s], position = [%f], factor = [%s]",
                  child.getName(), position, factor);
        }
        if (factor.x != 0) {
            child.setPositionX(position);
        } else if (factor.y != 0) {
            child.setPositionY(position);
        } else if (factor.z != 0) {
            child.setPositionZ(position);
        }

        child.onTransformChanged();
    }

    @Override
    protected void resetChildLayout(final int dataIndex) {
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            child.setPosition(0, 0, 0);
        }
    }

    /**
     * Initialize cache data set
     */
    protected void initCache() {
        mCache = new LinearCacheDataSet();
    }

    protected CacheDataSet mCache;
    protected boolean mUniformSize;
    protected Orientation mOrientation = Orientation.HORIZONTAL;
    protected Gravity mGravity = Gravity.CENTER;
}
