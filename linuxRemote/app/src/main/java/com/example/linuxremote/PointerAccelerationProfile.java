package com.example.linuxremote;

public abstract class PointerAccelerationProfile {

    static class MouseDelta {
        public float x, y;

        MouseDelta() {
            this(0,0);
        }

        MouseDelta(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public abstract void touchMoved(float deltaX, float deltaY, long eventTime);

    public abstract MouseDelta commitAcceleratedMouseDelta(MouseDelta reusedObject);
}
