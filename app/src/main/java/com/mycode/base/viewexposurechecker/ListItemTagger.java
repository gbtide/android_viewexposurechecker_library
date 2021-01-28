package com.mycode.base.viewexposurechecker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kyunghoon on 2019-07-29
 */
class ListItemTagger {

    private Map<Integer, Object> mTaggedListItems = new HashMap<>();

    boolean isAlreadyTagged(int position, Object uniqueId) {
        if (isInvalidValues(position, uniqueId)) {
            return false;
        }

        if (mTaggedListItems.containsKey(position) && mTaggedListItems.get(position).equals(uniqueId)) {
            return true;
        }
        return false;
    }

    void setTag(int position, Object uniqueId) {
        if (isInvalidValues(position, uniqueId)) {
            return;
        }
        mTaggedListItems.put(position, uniqueId);
    }

    private boolean isInvalidValues(int position, Object uniqueId) {
        return (position < 0 || uniqueId == null);
    }

}
