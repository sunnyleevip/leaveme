package com.sunny.leaveme.services.monitor;

import android.util.Log;

/**
 * Created by Sunny Li on 2016/10/15.
 * Controller for states
 */

class MonitorState {
    private final static String TAG = "MonitorState";

    enum State {
        STATE_NONE,
        STATE_SCHEDULE_ON,
        STATE_SCHEDULE_OFF,
        STATE_SURROUNDING_LIGHT_DARK_ON,
        STATE_SURROUNDING_LIGHT_DARK_OFF,
        STATE_LONG_TIME_USE_BLOCKER_ON,
        STATE_LONG_TIME_USE_BLOCKER_OFF,
        STATE_LAY_DOWN_BLOCKER_ON,
        STATE_LAY_DOWN_BLOCKER_OFF,
        STATE_WALKING_BLOCKER_ON,
        STATE_WALKING_BLOCKER_OFF
    }

    private State mState = State.STATE_NONE;
    private OnStateChanger mOnStateChanger;

    MonitorState(OnStateChanger onStateChanger) {
        mOnStateChanger = onStateChanger;
    }

    void setState(State state) {
        State oldState = mState;

        switch (state) {
            case STATE_SCHEDULE_ON:
                mState = State.STATE_SCHEDULE_ON;
                break;
            case STATE_SCHEDULE_OFF:
                if (mState == State.STATE_SCHEDULE_ON) {
                    mState = State.STATE_SCHEDULE_OFF;
                }
                break;
            case STATE_SURROUNDING_LIGHT_DARK_ON:
                if ((mState != State.STATE_SCHEDULE_ON)
                        && (mState != State.STATE_LONG_TIME_USE_BLOCKER_ON)
                        && (mState != State.STATE_LAY_DOWN_BLOCKER_ON)
                        && (mState != State.STATE_WALKING_BLOCKER_ON)) {
                    mState = State.STATE_SURROUNDING_LIGHT_DARK_ON;
                }
                break;
            case STATE_SURROUNDING_LIGHT_DARK_OFF:
                if (mState == State.STATE_SURROUNDING_LIGHT_DARK_ON) {
                    mState = State.STATE_SURROUNDING_LIGHT_DARK_OFF;
                }
                break;
            case STATE_LONG_TIME_USE_BLOCKER_ON:
                if (mState != State.STATE_SCHEDULE_ON) {
                    mState = State.STATE_LONG_TIME_USE_BLOCKER_ON;
                }
                break;
            case STATE_LONG_TIME_USE_BLOCKER_OFF:
                if (mState == State.STATE_LONG_TIME_USE_BLOCKER_ON) {
                    mState = State.STATE_LONG_TIME_USE_BLOCKER_OFF;
                }
                break;
            case STATE_LAY_DOWN_BLOCKER_ON:
                if ((mState != State.STATE_SCHEDULE_ON)
                        && (mState != State.STATE_LONG_TIME_USE_BLOCKER_ON)
                        && (mState != State.STATE_SURROUNDING_LIGHT_DARK_ON)
                        && (mState != State.STATE_WALKING_BLOCKER_ON)) {
                    mState = State.STATE_LAY_DOWN_BLOCKER_ON;
                }
                break;
            case STATE_LAY_DOWN_BLOCKER_OFF:
                if (mState == State.STATE_LAY_DOWN_BLOCKER_ON) {
                    mState = State.STATE_LAY_DOWN_BLOCKER_OFF;
                }
                break;
            case STATE_WALKING_BLOCKER_ON:
                if ((mState != State.STATE_SCHEDULE_ON)
                        && (mState != State.STATE_LONG_TIME_USE_BLOCKER_ON)
                        && (mState != State.STATE_SURROUNDING_LIGHT_DARK_ON)
                        && (mState != State.STATE_LAY_DOWN_BLOCKER_ON)) {
                    mState = State.STATE_WALKING_BLOCKER_ON;
                }
                break;
            case STATE_WALKING_BLOCKER_OFF:
                if (mState == State.STATE_WALKING_BLOCKER_ON) {
                    mState = State.STATE_WALKING_BLOCKER_OFF;
                }
                break;
            default:
                Log.e(TAG, "Wrong input state");
                break;
        }

        if (mState == state) {
            Log.i(TAG, "State changed from " + oldState + " to " + mState);
        } else {
            Log.d(TAG, "State no change, now is " + mState);
        }

        switch (mState) {
            case STATE_SCHEDULE_OFF:
            case STATE_SURROUNDING_LIGHT_DARK_OFF:
            case STATE_LONG_TIME_USE_BLOCKER_OFF:
            case STATE_LAY_DOWN_BLOCKER_OFF:
            case STATE_WALKING_BLOCKER_OFF:
                mState = State.STATE_NONE;
                Log.i(TAG, "State changed to " + mState);
        }

        mOnStateChanger.OnStateChange(oldState, mState);
    }

    State getState() {
        return mState;
    }

    interface OnStateChanger {
        void OnStateChange(State oldState, State newState);
    }
}
