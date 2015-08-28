package com.entradahealth.entrada.android.app.personal.activities.schedule;

import java.util.Collections;
import java.util.List;

import com.entradahealth.entrada.android.app.personal.activities.schedule.model.SectionListItemInfo;

import android.util.Pair;

/**
 * Class to prepare a section and its child's under the section.
 *
 */
public class SideMenuSection extends Pair<String, List<SectionListItemInfo>> {
    public final String title;
    public final List<SectionListItemInfo> menuItems;

    /**
     * Constructor for a SideMenuSection.
     *
     * @param sectionTitle the title of the section
     * @param menuItemList list of SideMenuItemInfo for this section
     */
    public SideMenuSection(String sectionTitle, List<SectionListItemInfo> menuItemList) {
        super(sectionTitle, menuItemList);
        title = sectionTitle;
        if (menuItemList == null) {
            menuItems = Collections.emptyList();
        } else {
            menuItems = menuItemList;
        }
    }
    /**
     * Returns the header sections count. 
     * @return
     */
    public int size() {
        return menuItems.size();
    }
}