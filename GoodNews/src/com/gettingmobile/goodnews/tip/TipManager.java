package com.gettingmobile.goodnews.tip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TipManager {
    public static final int TIP_UNGROUPED = 0;
    public static final int NO_TIPS = -1;
    private final TipStatusStorage storage;
    private final List<Tip> tips = new ArrayList<Tip>();
    private final Map<String, Tip> tipsById = new HashMap<String, Tip>();
    private final Map<Integer, List<Tip>> groups = new HashMap<Integer, List<Tip>>();

    public TipManager(TipStatusStorage storage) {
        this.storage = storage;
    }

    public void addTip(Integer groupId, Tip tip) {
        tips.add(tip);
        tipsById.put(tip.getId(), tip);
        List<Tip> group = groups.get(groupId);
        if (group == null) {
            group = new ArrayList<Tip>();
            groups.put(groupId, group);
        }
        group.add(tip);
    }

    public Tip getTip(String tipId) {
        return tipsById.get(tipId);
    }

    protected List<Tip> filterTips(List<Tip> tips, boolean unshownOnly, int flags) {
        final List<Tip> matchingTips = new ArrayList<Tip>();
        if (unshownOnly) {
            flags |= Tip.FLAG_AUTOMATIC;
        }
        if (tips != null) {
            for (Tip tip : tips) {
                if (tip.hasFlags(flags) && (!unshownOnly || !storage.wasTipShown(tip.getId()))) {
                    matchingTips.add(tip);
                }
            }
        }
        return matchingTips;
    }

    public List<Tip> getTips(int flags) {
        if (flags == 0) {
            return tips;
        } else {
            return filterTips(tips, false, flags);
        }
    }

    public List<Tip> getGroup(Integer groupId, boolean unshownOnly, int flags) {
        return filterTips(groups.get(groupId), unshownOnly, flags);
    }

    public void setTipShown(Tip tip) {
        storage.setTipShown(tip.getId());
    }
}
