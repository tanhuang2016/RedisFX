/* SPDX-License-Identifier: MIT */

package redisfx.tanh.rdm.ui.sampler.event;

public final class TabPaneEvent extends Event {

    public enum EventType {
        // theme can change both, base font size and colors
        SERVER_TAB_PANE_CHANGE,
        // font size or family only change
        KEY_TAB_PANE_CHANGE,
    }

    private final EventType eventType;

    public TabPaneEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "TabPaneEvent{"
            + "eventType=" + eventType
            + "} " + super.toString();
    }
}
