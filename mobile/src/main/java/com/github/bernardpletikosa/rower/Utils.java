package com.github.bernardpletikosa.rower;

/** Created by bp on 20/11/2016. */

public class Utils {

    public static class UpdateWorkoutEvent {
        private String event;

        public UpdateWorkoutEvent(String event) {
            this.event = event;
        }

        public String getEvent() {
            return event;
        }
    }
}
