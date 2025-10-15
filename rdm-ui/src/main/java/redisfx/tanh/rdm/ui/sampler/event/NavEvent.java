/* SPDX-License-Identifier: MIT */

package redisfx.tanh.rdm.ui.sampler.event;


import redisfx.tanh.rdm.ui.sampler.page.Page;

public final class NavEvent extends Event {

    private final Class<? extends Page> page;

    public NavEvent(Class<? extends Page> page) {
        this.page = page;
    }

    public Class<? extends Page> getPage() {
        return page;
    }

    @Override
    public String toString() {
        return "NavEvent{"
            + "page=" + page
            + "} " + super.toString();
    }
}
