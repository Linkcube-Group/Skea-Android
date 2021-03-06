package me.linkcube.skea.ui.exercise;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.ervin.bluetooth.EasyBluetooth;
import me.linkcube.skea.R;
import me.linkcube.skea.base.ui.BaseActivity;
import me.linkcube.skea.core.KeyConst;
import me.linkcube.skea.core.excercise.Bar;
import me.linkcube.skea.core.excercise.BarConst;
import me.linkcube.skea.core.excercise.ExerciseController;
import me.linkcube.skea.core.excercise.ExerciseScoreCounter;
import me.linkcube.skea.db.DayRecord;
import me.linkcube.skea.ui.evaluation.RecordActivity;
import me.linkcube.skea.util.TimeUtils;


public class ExerciseActivity extends BaseActivity implements ExerciseController.ExerciseScoreCallback {

    private static final String TAG = "ExerciseActivity";
    private LinearLayout frontGroup;
    private LinearLayout behindGroup;
    private ScrollView frontScrollView;
    private ScrollView behindScrollView;
    private ExerciseController controller;
    private TextView leftTimeTextView;
    private TextView scoreTextView;
    private ImageView perfectCoolImageView;
    private ImageView laser_iv;
    private boolean shrink;

    private Timer testSignalTimer;

    public UpdateImageViewPicHandler updateTextViewTextHandler;

    private InitGameHandler initGameHandler;

    private TestShrinkHandler testShrinkHandler;



    private int previousScore=0;

    /**
     * 用于标记ScorllView 是否可以滑动
     * 此处是不允许滑动的
     */
    private boolean mScrollable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        initViews();

        updateTextViewTextHandler = new UpdateImageViewPicHandler(perfectCoolImageView);
        initGameHandler = new InitGameHandler();
        testShrinkHandler = new TestShrinkHandler(laser_iv);


        testSignalTimer=new Timer();
        testSignalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
//                //使用Handler发送消息，以检测当前是否有挤压
                sendMessage_to_handler(testShrinkHandler,TestShrinkHandler.TEST_SIGNAL,TestShrinkHandler.TEST_SHRINK_YES_OR_NOT);
            }
        }, 1000, 15);//1000ms 以后每隔15ms执行一次

    }



    /**
     * 初始化控件及变量，注册事件
     */
    private void initViews() {
        leftTimeTextView = (TextView) findViewById(R.id.time_left);
        scoreTextView = (TextView) findViewById(R.id.score);
        frontScrollView = (ScrollView) findViewById(R.id.front_scrollView);
        behindScrollView = (ScrollView) findViewById(R.id.behind_scrollView);
        behindGroup = (LinearLayout) findViewById(R.id.behind_group);
        frontGroup = (LinearLayout) findViewById(R.id.exercise_group);
//        shrinkButton = (ToggleButton) findViewById(R.id.shrink_button);

        //显示perfect cool 文字特效
        perfectCoolImageView = (ImageView) findViewById(R.id.perfect_cool_iv);

        laser_iv=(ImageView) findViewById(R.id.laser);


        controller = new ExerciseController(this);
        controller.registerShrinkCallback(this);

        frontScrollView.setOnTouchListener(mScorllViewOnTouchListener);
        behindScrollView.setOnTouchListener(mScorllViewOnTouchListener);

    }

    /**
     * ScorllView 的OnTouchListener事件，
     * 以便禁用其滑动事件
     */
    View.OnTouchListener mScorllViewOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mScrollable)
                return false;
            else {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        }
    };

    @Override
    public int getLayoutResourceId() {
        return R.layout.activity_exercise;
    }

    private int timeCount = 0;

    @Override
    public void onResume() {
        super.onResume();
        //可以将Game的初始化放在ExerciseProgressDialog的onStart(),和onStop()方法中。
        //但是现在放进去会出现“错位”，有空调整之
//        final ExerciseProgressDialog progressDialog = new ExerciseProgressDialog(this,initGameHandler);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.exercise_load_title));
        progressDialog.setMessage(getResources().getString(R.string.exercise_load_message));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.show();


        new Thread(new Runnable() {
            @Override
            public void run() {

                //初始化Bar－－－调用 controller.init(getApplicationContext(), frontGroup, behindGroup);
                sendMessage_to_handler(initGameHandler,InitGameHandler.INIT_GAME_HANDLER_EXERCISE_KEY,InitGameHandler.INIT_EXERCISE);

                //进入游戏后，倒计时5秒钟开始
                while (timeCount < 100) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressDialog.incrementProgressBy(1);
                    timeCount++;
                }
                progressDialog.dismiss();

                //启动游戏－－－调用 controller.prepare(getApplicationContext(), frontScrollView, behindScrollView); 和controller.start();
                sendMessage_to_handler(initGameHandler,InitGameHandler.INIT_GAME_HANDLER_EXERCISE_KEY,InitGameHandler.PREPARE_EXERCISE);

            }
        }).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exercise, menu);
        return true;
    }

    private boolean isPaused = true;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
        switch (item.getItemId()){
            case R.id.action_pause:
                if (isPaused) {
                    controller.pause();
                    isPaused = false;
                    gameAlertDialog();
                    return true;
                }
                break;
            case android.R.id.home:
                stopTheExercise();
                return true;

//                break;
            default:
//                stopTheExercise();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void gameAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.exercise_notice_title));
        builder.setMessage(getString(R.string.exercise_notice_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.exercise_notice_yes), new DialogInterface.OnClickListener() {//退出游戏
                    public void onClick(DialogInterface dialog, int id) {
                        ExerciseActivity.this.stopTheExercise();
                    }
                })
                .setNegativeButton(getString(R.string.exercise_notice_no), new DialogInterface.OnClickListener() {//继续游戏
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        controller.continueGame(getApplicationContext());
                        isPaused = true;
                    }
                });
        //显示Dialog
        builder.create().show();
    }

    @Override
    public void startGameScore(Bar bar) {
//        Log.d("startScore", "bar type = " + bar.getType());
        ExerciseScoreCounter.getInstance(this).startGameScore(bar);
    }

    @Override
    public void startCoolScore(Bar bar) {
        ExerciseScoreCounter.getInstance(this).startCoolScore(bar);
    }

    @Override
    public void startPerfectScore(Bar bar) {
        ExerciseScoreCounter.getInstance(this).startPerfectScore(bar);
    }

    @Override
    public void tickGameScore() {
        if(ExerciseScoreCounter.getInstance(this).tickGameScore()){
            showPerfectCool(R.drawable.text_starts);
        }
    }

    @Override
    public void tickCoolScore() {
        ExerciseScoreCounter.getInstance(this).tickCoolScore();

    }

    @Override
    public void tickPerfectScore() {
        ExerciseScoreCounter.getInstance(this).tickPerfectScore();

    }

//    String temp_time;
    @Override
    public void tickSecond(final int leftTime) {
        Log.d("tickSecond ", "" + leftTime);
        //记录已运动时间
        ExerciseScoreCounter.getInstance(this).tickSecond();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                leftTimeTextView.setText(leftTime + "");
                leftTimeTextView.setText(TimeUtils.formatSec2Min_Sec(leftTime));
            }
        });

        //TODO 更新UI倒计时
    }

    @Override
    public void stopGameScore() {
        final int score = ExerciseScoreCounter.getInstance(this).stopGameScore();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTextView.setText(score + "");
            }
        });
        //TODO 更新UI分数
        previousScore=score;
    }

    @Override
    public void stopCoolScore() {
        final int score = ExerciseScoreCounter.getInstance(this).stopCoolScore();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTextView.setText(score + "");
            }
        });
        if(score==previousScore+BarConst.SCORE.COOL_SCORE){
            previousScore=score;
            showPerfectCool(R.drawable.text_cool);

        }

    }

    @Override
    public void stopPerfectScore() {

        final int score = ExerciseScoreCounter.getInstance(this).stopPerfectScore();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTextView.setText(score + "");
            }
        });
        if(score==previousScore+ BarConst.SCORE.PERFECT_SCORE){
            previousScore=score;
            showPerfectCool(R.drawable.text_perfect);

        }
    }

    @Override
    public void showPerfectCool(int imgID) {
        AlphaAnimation perfect_cool_anim = new AlphaAnimation(1.0f, 0.0f);
        perfect_cool_anim.setDuration(1000);//1000ms


        perfect_cool_anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                perfectCoolImageView.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                perfectCoolImageView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        //使用Handler发送消息，以更新UI
        sendMessage_to_handler(updateTextViewTextHandler,UpdateImageViewPicHandler.PERFECT_COOL_IMAGEVIEW_PIC_MESSAGE_KEY,imgID);
        //注册动画
        perfectCoolImageView.setAnimation(perfect_cool_anim);

    }

    @Override
    public void startMissScore() {

        ExerciseScoreCounter.getInstance(this).startMissScore();

    }

    @Override
    public void tickMissScore() {
        if(ExerciseScoreCounter.getInstance(this).tickMissScore()){
            showPerfectCool(R.drawable.text_miss);
        }

    }

    @Override
    public void stopMissScore() {
        ExerciseScoreCounter.getInstance(this).stopMissScore();

    }

    @Override
    public void showExerciseResult(List<Bar> list) {
        Intent showResultIntent = new Intent(this, RecordActivity.class);

//        //保存Score
//        double[] barScore = new double[(list.size()+1)/2];
//        //保存Type
//        int[] barType = new int[(list.size()+1)/2];
//
//        for (int i = 0; i < (list.size()+1)/2; i++) {//将中间“休息”间隔去除掉，以便数据处理
//            barScore[i] = (double) (list.get(i*2).getScore());
//            barType[i] = list.get(i*2).getType();
//        }
//
//        showResultIntent.putExtra(RecordActivity.EXERCISE_TYPE_KEY, barType);
//        showResultIntent.putExtra(RecordActivity.EXERCISE_SCORE_KEY, barScore);

        startActivity(showResultIntent);
    }

    @Override
    public void stopTheExercise() {


        testSignalTimer.cancel();
        sendMessage_to_handler(initGameHandler, InitGameHandler.INIT_GAME_HANDLER_EXERCISE_KEY, InitGameHandler.STOP_EXERCISE);
        ExerciseScoreCounter.getInstance(this).stopScoreCounter();
        this.finish();
    }


    /**
     * @param handler:发送消息的Handler(或子类)
     * @param key:发送数据和接收数据时用到的key
     * @param value:key所对应的value
     * */
    public void sendMessage_to_handler(Handler handler, String key, int value){
        //使用Handler发送消息
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt(key,value);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }


    public int handlerMessage_from_handler(Message msg ,String key){

        return msg.getData().getInt(key);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){
            //重写物理“返回键”事件，以防止用户通过它退出游戏时，再次进入节奏变快
            Log.i("CXC","*****back----out");
            stopTheExercise();

        }
        return super.onKeyDown(keyCode, event);
    }

    class InitGameHandler extends Handler {
        public static final String INIT_GAME_HANDLER_EXERCISE_KEY="com.linkcube.skea.ui.excise.init_game_handler";
        public static final int INIT_EXERCISE=10000001;


        public static final int PREPARE_EXERCISE=10000002;
        public static final int STOP_EXERCISE=10000003;


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


            switch ( msg.getData().getInt(this.INIT_GAME_HANDLER_EXERCISE_KEY)){
                case INIT_EXERCISE:
                    controller.init(getApplicationContext(), frontGroup, behindGroup);
                    break;
                case PREPARE_EXERCISE:
                    controller.prepare(getApplicationContext(), frontScrollView, behindScrollView);
                    controller.start();
                    //游戏开始后，手机便开始接收信号
                    EasyBluetooth.getInstance().setOnDataReceivedListener(new EasyBluetooth.OnDataReceivedListener() {
                        @Override
                        public void onDataReceived(byte[] bytes, String message) {
//                        pressDataTextView.setText(bytes.toString());
//                            Log.i("CXC", "&&&&&&&&&&&&onDataReceived：" + bytes.toString());
                            if (bytes[0] == KeyConst.GameFrame.PRESS_FRAME[0]
                                    && bytes[1] == KeyConst.GameFrame.PRESS_FRAME[1]) {
                                shrink = true;
                            }
                        }
                    });
                    break;
                case STOP_EXERCISE:
                    controller.stop();
                    break;
                default:
                    break;
            }
        }
    }


    private int countTime=0;
    private boolean is_active=false;

    /**
     * Timer定时给UI发送消息，让UI Thread 去检测是否有挤压
     */
    class TestShrinkHandler extends Handler {
        public static final String TEST_SIGNAL = "com.linkcube.skea.ui.excise.test_signal";
        public static final int TEST_SHRINK_YES_OR_NOT=2000001;
        private ImageView laser_iv;

        public TestShrinkHandler(ImageView iv) {
            super();
            this.laser_iv=iv;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //接收到Timer定时发送来的消息，检测此时是否有“挤压”
            switch ( handlerMessage_from_handler(msg,TEST_SIGNAL)){
                case TEST_SHRINK_YES_OR_NOT:
                    if (shrink) {//有挤压
                        ExerciseScoreCounter.getInstance(getApplicationContext()).receiveSignal();
                        //当前接收到了信号，显示“黄线”
                        if(is_active==false){
                            laser_iv.setImageResource(R.drawable.laser_active);
                            is_active=true;
                            countTime=0;
                        }

                    }
                    else {
//                        is_active=false;
                        countTime++;//记数
                    }
                    break;
                default:
                    break;
            }
            //重置信号标志－－－这一点很重要
            shrink = false;
            if(countTime>=5  ){//超过50msX5=250ms,即0.25s，变为“灰线”
                laser_iv.setImageResource(R.drawable.laser_inactive);
                countTime=0;
                is_active=false;
            }
        }
    }

    /**
     * 用于更新ImageView中文图片的Handler
     * 游戏中Perfect,Cool,Miss 等特效的显示
     */
    class UpdateImageViewPicHandler extends android.os.Handler {

        public static final String PERFECT_COOL_IMAGEVIEW_PIC_MESSAGE_KEY = "com.linkcube.skea.ui.exercise.UpdateImageViewPicHandler.message_key";
        private ImageView perfectCoolImageView;

        public UpdateImageViewPicHandler(ImageView iv) {
            super();
            this.perfectCoolImageView = iv;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //得到传递的参数
            int imgID =handlerMessage_from_handler(msg,PERFECT_COOL_IMAGEVIEW_PIC_MESSAGE_KEY);
            this.perfectCoolImageView.setImageResource(imgID);
        }
    }
}



