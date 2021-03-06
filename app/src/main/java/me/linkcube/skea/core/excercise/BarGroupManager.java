package me.linkcube.skea.core.excercise;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.List;

import custom.android.util.DensityUtils;
import custom.android.util.PreferenceUtils;
import me.linkcube.skea.R;
import me.linkcube.skea.core.KeyConst;
import me.linkcube.skea.view.BarViewWrapper;

/**
 * Created by Ervin on 14/11/5.
 */
public class BarGroupManager {

    private static BarGroupManager instance;
    private List<Bar> list;
    private int frontBlankHeight;
    private int behindBlankHeight;
    private ScrollView frontScrollView;
    private ScrollView behindScrollView;

    private BarGroupManager(Context context) {
        //TODO 获取level
        list = BarGenerator.getInstance(context).getBars();
    }

    public static BarGroupManager getInstance(Context context) {
        if (instance == null) {
            Log.i("CXC","+++++++++++new instance of BarGroupManager+++++++++");
            instance = new BarGroupManager(context);
        }
        return instance;
    }

    public static void reset2null(){
        instance=null;
    }

    public int getBarGroupHeight(Context context, boolean front) {
        //TODO 获取level
        int level = PreferenceUtils.getInt(context, KeyConst.SKEA_EXERCISE_LEVEL_KEY,3);
        int barUnitNum = 0;
        switch (level+1) {
            case BarConst.LEVEL.LEVEL_ONE:
                barUnitNum = BarConst.LEVEL.BAR_UNIT_NUM[BarConst.LEVEL.LEVEL_ONE];
                break;
            case BarConst.LEVEL.LEVEL_TWO:
                barUnitNum = BarConst.LEVEL.BAR_UNIT_NUM[BarConst.LEVEL.LEVEL_TWO];
                break;
            case BarConst.LEVEL.LEVEL_THREE:
                barUnitNum = BarConst.LEVEL.BAR_UNIT_NUM[BarConst.LEVEL.LEVEL_THREE];
                break;
            case BarConst.LEVEL.LEVEL_FOUR:
                barUnitNum = BarConst.LEVEL.BAR_UNIT_NUM[BarConst.LEVEL.LEVEL_FOUR];
                break;
            default:
                barUnitNum = BarConst.LEVEL.BAR_UNIT_NUM[BarConst.LEVEL.LEVEL_ONE];
                break;
        }
        //***锻练的时间
        int exercise = BarConst.VIEW.SPEED * (BarConst.TYPE.BAR_TIME[BarConst.TYPE.SHORT] + BarConst.TYPE.BAR_TIME[BarConst.TYPE.MEDIUM] + BarConst.TYPE.BAR_TIME[BarConst.TYPE.LONG]) * barUnitNum;
        int blank = getBlankHeight(front);
        //****各个间隔休息的总时间，因为一共有3种长度，每个有15个，一共3x15＝45个，所以需要44个间隔
        int slot = 0;
        //Log.i("CXC","*****"+list.get(list.size()-1).getType());
        switch (list.get(list.size() - 1).getType()) {

            case BarConst.TYPE.SHORT:
                slot = BarConst.VIEW.SPEED * ((BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_SHORT] + BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_MEDIUM] + BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_LONG]) * barUnitNum - BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_SHORT]);
                break;
            case BarConst.TYPE.MEDIUM:
                slot = BarConst.VIEW.SPEED * ((BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_SHORT] + BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_MEDIUM] + BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_LONG]) * barUnitNum - BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_MEDIUM]);
                break;
            case BarConst.TYPE.LONG:
                slot = BarConst.VIEW.SPEED * ((BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_SHORT] + BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_MEDIUM] + BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_LONG]) * barUnitNum - BarConst.TYPE.BAR_TIME[BarConst.TYPE.SLOT_LONG]);
                break;
            default:
                break;


        }
        Log.i("CXC", "----exercise:" + exercise);
        Log.i("CXC", "----slot:" + slot);
        Log.i("CXC", "----blank:" + blank);
        Log.i("CXC", "----total:" + (exercise + blank + slot));
        return exercise + blank + slot;

    }

    /**
     * 滑动到指定位置开始游戏
     *
     * @param frontScrollView
     * @param behindScrollView
     */
    public void prepare(Context context, ScrollView frontScrollView, ScrollView behindScrollView) {
        this.frontScrollView = frontScrollView;
        this.behindScrollView = behindScrollView;
        frontScrollView.scrollBy(0, getBarGroupHeight(context, true));
        behindScrollView.scrollBy(0, getBarGroupHeight(context, false));
    }

    public void scrollBy(int y) {
        frontScrollView.scrollBy(0, y);
        behindScrollView.scrollBy(0, y);
    }

    /**
     * 这个方法只执行一次
     *
     * @param context
     * @param frontGroup
     * @param behindGroup
     */
    public void initBarGroup(Context context, LinearLayout frontGroup, LinearLayout behindGroup) {
        ViewGroup frontParent = (ViewGroup) frontGroup.getParent();
        ViewGroup behindParent = (ViewGroup) behindGroup.getParent();

        //清空View,以防用户第一次进入游戏时，未连接蓝牙。退出之后，连接蓝牙，再次进入“位置”错位
        frontGroup.removeAllViews();
        behindGroup.removeAllViews();

        frontGroup.addView(getHeaderOrFooterView(frontParent, true));
        behindGroup.addView(getHeaderOrFooterView(behindParent, false));


        for (int i = list.size() - 1; i >= 0; i--) {
            frontGroup.addView(new BarViewWrapper().getImageView(context, list.get(i).getType(), false));
            behindGroup.addView(new BarViewWrapper().getImageView(context, list.get(i).getType(), true));
//            Log.i("CXC", "i" + i + "--type:" + list.get(i).getType());
        }

        frontGroup.addView(getHeaderOrFooterView(frontParent, true));
        behindGroup.addView(getHeaderOrFooterView(behindParent, false));
        //***在这里设置各个Bar的触发时间与结束时间
        //其中也包括了所有的间隔，
        for (int i = 0; i < list.size(); i++) {
            Bar bar = list.get(i);
            if (i == 0) {
                bar.setBeginActiveOffset(getBlankHeight(true));

//                Log.d("test activeOffset", "" + bar.getBeginActiveOffset());
                bar.setEndActiveOffset(bar.getBeginActiveOffset() + bar.getHeight(context));
            } else {
                bar.setBeginActiveOffset(list.get(i - 1).getEndActiveOffset());
                bar.setEndActiveOffset(bar.getBeginActiveOffset() + bar.getHeight(context));
            }
//            Log.i("CXC", "---" + bar.getBeginActiveOffset() + "---" + bar.getEndActiveOffset());
        }

    }

    private int getBlankHeight(boolean front) {
        if (front)
            return frontBlankHeight;
        else
            return behindBlankHeight;
    }

    private View getHeaderOrFooterView(ViewGroup parent, boolean front) {
        View view = new View(parent.getContext());
        int width = DensityUtils.dip2px(parent.getContext(), 60);
        int height = parent.getHeight();
        //****出现倒计时问题可能就出现在这里。。。
        if (front) {
            frontBlankHeight = height;

//            Log.i("CXC", "****front Height:" + height);
        } else {
            behindBlankHeight = height;
//            Log.i("CXC", "****behind Height:" + height);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        view.setLayoutParams(lp);
        view.setBackgroundColor(parent.getResources().getColor(R.color.transparent));
        return view;
    }

}
