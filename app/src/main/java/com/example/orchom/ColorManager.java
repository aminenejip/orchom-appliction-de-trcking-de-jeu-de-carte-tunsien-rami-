package com.example.orchom;

import android.content.Context;
import androidx.core.content.ContextCompat;

/**
 * Utility class to manage app colors and avoid hardcoded strings.
 */
public class ColorManager {

    public static int getPlayerColor(Context context, int index) {
        int colorRes;
        switch (index % 5) {
            case 0: colorRes = R.color.player_red; break;
            case 1: colorRes = R.color.player_green; break;
            case 2: colorRes = R.color.player_orange; break;
            case 3: colorRes = R.color.player_purple; break;
            case 4: colorRes = R.color.player_blue; break;
            default: colorRes = R.color.black; break;
        }
        return ContextCompat.getColor(context, colorRes);
    }

    public static String getPlayerColorHex(int index) {
        switch (index % 5) {
            case 0: return "#C41E3A"; // player_red
            case 1: return "#2E7D32"; // player_green
            case 2: return "#F57C00"; // player_orange
            case 3: return "#512DA8"; // player_purple
            case 4: return "#1976D2"; // player_blue
            default: return "#000000";
        }
    }
}
