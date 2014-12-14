package me.linkcube.skea.core.excercise;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ervin on 14/11/2.
 */
public class ExerciseController {

    private final int UNIT_TIME = 50;
    private Timer timer;
    private int offset = 0;

    private int activePosition = -1;

    private int nextPosition = 0;

    private List<Bar> list;

    private TimerTask timerTask;

    private boolean active = false;

    private int leftTime;

    private ExerciseScoreCallback callback;

    /**
     * 用来分割计时点
     */
    private int count = 0;

    private int second = 0;

    public ExerciseController(Context context) {
        //TODO get user's level

        list = BarGenerator.getInstance().getBars();

        nextPosition = 0;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                BarGroupManager.getInstance().scrollBy(-BarConst.VIEW.UNIT_SPEED);
                offset = offset + BarConst.VIEW.UNIT_SPEED;
//                Log.i("CXC", "@@@@offset:" + offset);
                checkActivePosition();
            }
        };

    }

    public void init(Context context, LinearLayout frontGroup, LinearLayout behindGroup) {
        BarGroupManager.getInstance().initBarGroup(context, frontGroup, behindGroup);
        //将leftTime的计算从构造函数移到这里，是因为，getBlankHeight中返回的height只有在getHeaderOrFooterView执行过之后才是真正的高度，否则为未赋值的默认值0
        //进行向上取值
        leftTime=(int)Math.ceil (BarGroupManager.getInstance().getBarGroupHeight(context, true) / (double)BarConst.VIEW.SPEED);

        Log.i("CXC","leftTime----"+leftTime);
    }

    public void prepare(Context context, ScrollView frontScrollView, ScrollView behindScrollView) {
        BarGroupManager.getInstance().prepare(context, frontScrollView, behindScrollView);
    }


    /**
     * 开始计算分数
     */
    public void shrink() {

    }

    public void relax() {

    }

    public void checkActivePosition() {
        //确定激活的Bar
        count++;
        second++;
        if (activePosition < list.size() && 0 <= nextPosition && nextPosition < list.size()) {
            Bar bar = list.get(nextPosition);
            if (bar.getBeginActiveOffset() <= offset && offset <= bar.getRealEndActiveOffset()) {
                if (!active) {
                    activePosition = nextPosition;
                    nextPosition = activePosition + 2;

                    if (callback != null) {
//                        callback.startCoolScore(list.get(activePosition));
//                        callback.startPerfectScore(list.get(activePosition));
                        callback.startScore(list.get(activePosition));

                        count = 0;
                    }
                }
            }else if (activePosition != -1 && list.get(activePosition).getBeginActiveOffset() <= offset && offset < list.get(activePosition).getBeginActiveOffset()+12) {
                //cheat
                active = true;

//                Log.d("checkActivePosition", "activePosition = " + activePosition);
            }
            else if (activePosition != -1 && list.get(activePosition).getBeginActiveOffset()+12 <= offset && offset < list.get(activePosition).getBeginActiveOffset()+32) {
                //Cool
                active = true;
                callback.tickCoolScore();
//                Log.d("checkActivePosition", "activePosition = " + activePosition);
            }
            else if (activePosition != -1 && list.get(activePosition).getRealBeginActiveOffset()+32 <= offset && offset < list.get(activePosition).getRealBeginActiveOffset()) {
                //Perfect
                active = true;
                callback.tickPerfectScore();
//                Log.d("checkActivePosition", "activePosition = " + activePosition);
            }
            else if (activePosition != -1 && list.get(activePosition).getRealBeginActiveOffset() <= offset && offset <= list.get(activePosition).getRealEndActiveOffset()) {
                active = true;
//                Log.d("checkActivePosition", "activePosition = " + activePosition);
            } else {
                active = false;
                if (callback != null && activePosition != -1)
                    callback.tickScore();
                callback.stopScore();

                activePosition = -1;
            }
        } else {
            //timer.cancel();
        }

        if (count != 0 && count % 10 == 0) {
            count = 0;
            if (callback != null)
                callback.tickScore();
        }

        if (second % 20 == 0) {
            second = 0;
            if (callback != null) {
                leftTime -= 1;
                callback.tickSecond(leftTime);
            }
        }
        //以总时间倒计时来控制整个游戏
        if (leftTime <= 0) {
            timer.cancel();
            //游戏结束之后，显示本次游戏成绩－－跳转到运动记录界面进行显示

            /**
             *
             * 启动运动记录Activity，并传入当前游戏用户的数据，以便显示
             **/

        }
    }


    public void pause() {
        timerTask.cancel();
    }

    public void start() {
        timer.schedule(timerTask, 0, UNIT_TIME);
    }

    public void continueGame(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                BarGroupManager.getInstance().scrollBy(-BarConst.VIEW.UNIT_SPEED);
                offset = offset + BarConst.VIEW.UNIT_SPEED;
//                Log.i("CXC", "@@@@offset:" + offset);
                checkActivePosition();
            }
        };
       timer.schedule(timerTask,0,UNIT_TIME);
    }

    public void resume() {
        timer.schedule(timerTask, 0, UNIT_TIME);
    }

    public void registerShrinkCallback(ExerciseScoreCallback callback) {
        this.callback = callback;
    }

    public interface ExerciseScoreCallback {

        void startScore(Bar bar);
        void startCoolScore(Bar bar);
        void startPerfectScore(Bar bar);

        void tickScore();
        void tickCoolScore();
        void tickPerfectScore();

        void tickSecond(int leftTime);

        void stopScore();

    }

}
