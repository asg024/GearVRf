package com.samsung.smcl.vr.widgets.widget;

import org.gearvrf.GVRTransform;

import static com.samsung.smcl.vr.widgets.main.Utility.equal;

public class TransformCache {
    public TransformCache() {

    }

    public TransformCache(final GVRTransform transform) {
        save(transform);
    }

    public TransformCache(final Widget widget) {
        save(widget);
    }

    public void save(GVRTransform transform) {
        save(transform, false);
    }

    public boolean save(GVRTransform transform, boolean notify) {
        if (notify) {
            return setPosX(transform.getPositionX())
                    | setPosY(transform.getPositionY())
                    | setPosZ(transform.getPositionZ())
                    | setRotW(transform.getRotationW())
                    | setRotX(transform.getRotationX())
                    | setRotY(transform.getRotationY())
                    | setRotZ(transform.getRotationZ())
                    | setScaleX(transform.getScaleX())
                    | setScaleY(transform.getScaleY())
                    | setScaleZ(transform.getScaleZ());
        } else {
            posX = transform.getPositionX();
            posY = transform.getPositionY();
            posZ = transform.getPositionZ();
            rotW = transform.getRotationW();
            rotX = transform.getRotationX();
            rotY = transform.getRotationY();
            rotZ = transform.getRotationZ();
            scaleX = transform.getScaleX();
            scaleY = transform.getScaleY();
            scaleZ = transform.getScaleZ();
            return false;
        }
    }

    public void restore(GVRTransform transform) {
        transform.setScale(scaleX, scaleY, scaleZ);
        transform.setPosition(posX, posY, posZ);
        transform.setRotation(rotW, rotX, rotY, rotZ);
    }

    public boolean changed(final GVRTransform transform) {
        return !(equal(posX, transform.getPositionX())
                && equal(posY, transform.getPositionY())
                && equal(posZ, transform.getPositionZ())
                && equal(rotW, transform.getRotationW())
                && equal(rotX, transform.getRotationX())
                && equal(rotY, transform.getRotationY())
                && equal(rotZ, transform.getRotationZ())
                && equal(scaleX, transform.getScaleX())
                && equal(scaleY, transform.getScaleY())
                && equal(scaleZ, transform.getScaleZ()));
    }

    public void save(final Widget widget) {
        save(widget.getTransform(), false);
    }

    public boolean save(final Widget widget, boolean notify) {
        return save(widget.getTransform(), notify);
    }

    public void restore(final Widget widget) {
        widget.setScale(scaleX, scaleY, scaleZ);
        widget.setPosition(posX, posY, posZ);
        widget.setRotation(rotW, rotX, rotY, rotZ);
    }

    public boolean changed(final Widget widget) {
        return changed(widget.getTransform());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Pos: {").append(posX).append(',')
                .append(posY).append(',').append(posZ).append("}, Rot: {")
                .append(rotW).append(',').append(rotX).append(',').append(rotY)
                .append(',').append(rotZ).append("}, Scale: {").append(scaleX)
                .append(',').append(scaleY).append(',').append(scaleZ)
                .append('}');
        return b.toString();
    }

    public boolean setPosition(float posX, float posY, float posZ) {
        return setPosX(posX) | setPosY(posY) | setPosZ(posZ);
    }

    public boolean setPosX(float posX) {
        boolean changed = !equal(this.posX, posX);
        this.posX = posX;
        return changed;
    }

    public boolean setPosY(float posY) {
        boolean changed = !equal(this.posY, posY);
        this.posY = posY;
        return changed;
    }

    public boolean setPosZ(float posZ) {
        boolean changed = !equal(this.posZ, posZ);
        this.posZ = posZ;
        return changed;
    }

    public boolean setRotation(float rotW, float rotX, float rotY, float rotZ) {
        return setRotW(rotW) | setRotX(rotX) | setRotY(rotY) | setRotZ(rotZ);
    }

    public boolean setRotW(float rotW) {
        boolean changed = !equal(this.rotW, rotW);
        this.rotW = rotW;
        return changed;
    }

    public boolean setRotX(float rotX) {
        boolean changed = !equal(this.rotX, rotX);
        this.rotX = rotX;
        return changed;
    }

    public boolean setRotY(float rotY) {
        boolean changed = !equal(this.rotY, rotY);
        this.rotY = rotY;
        return changed;
    }

    public boolean setRotZ(float rotZ) {
        boolean changed = !equal(this.rotZ, rotZ);
        this.rotZ = rotZ;
        return changed;
    }

    public boolean setScale(float scaleX, float scaleY, float scaleZ) {
        return setScaleX(scaleX) | setScaleY(scaleY) | setScaleZ(scaleZ);
    }

    public boolean setScaleX(float scaleX) {
        boolean changed = !equal(this.scaleX, scaleX);
        this.scaleX = scaleX;
        return changed;
    }

    public boolean setScaleY(float scaleY) {
        boolean changed = !equal(this.scaleY, scaleY);
        this.scaleY = scaleY;
        return changed;
    }

    public boolean setScaleZ(float scaleZ) {
        boolean changed = !equal(this.scaleZ, scaleZ);
        this.scaleZ = scaleZ;
        return changed;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public float getRotW() {
        return rotW;
    }

    public float getRotX() {
        return rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    private float posX;
    private float posY;
    private float posZ;
    private float rotW;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float scaleX;
    private float scaleY;
    private float scaleZ;
}