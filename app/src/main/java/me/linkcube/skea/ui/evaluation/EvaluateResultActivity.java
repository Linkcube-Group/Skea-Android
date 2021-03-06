package me.linkcube.skea.ui.evaluation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import custom.android.util.PreferenceUtils;
import me.linkcube.skea.R;
import me.linkcube.skea.base.ui.BaseActivity;
import me.linkcube.skea.core.KeyConst;
import me.linkcube.skea.core.UserManager;
import me.linkcube.skea.core.persistence.Evaluation;
import me.linkcube.skea.core.persistence.User;

public class EvaluateResultActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "EvaluateResultActivity";

    private static final int[] RISK_FACTOR_IMG_RES = new int[]{R.drawable.icon_risk_factor_low, R.drawable.icon_risk_factor_low, R.drawable.icon_risk_factor_medium, R.drawable.icon_risk_factor_high, R.drawable.icon_risk_factor_high};
    private static final int[] EXERCISE_LEVEL_TEXT_ID = new int[]{R.string.evaluate_level_one, R.string.evaluate_level_two, R.string.evaluate_level_three, R.string.evaluate_level_four, R.string.evaluate_level_five};

    private static final int[] SUGGESTIONS_TEXT_ID = new int[]{R.string.evaluate_suggestions_low, R.string.evaluate_suggestions_average, R.string.evaluate_suggestions_medium, R.string.evaluate_suggestions_high};

    public static final String KEY_EXERCISE_LEVEL = "KEY_EXERCISE_LEVEL";

    public static final String KEY_RISK_FACTOR = "KEY_RISK_FACTOR";

    private static final int REQUEST_CODE_SETTING_LEVEL = 1;

    private static final int REQUEST_CODE_EVALUATE = 2;

    private TextView exerciseLevel;
    private TextView suggestions_tv;

    private Button evaluateBtn;

    private ImageView riskFactorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.activity_evaluate_result;
    }

    /**
     * 得到相关控件，注册事件
     */
    private void initViews() {
        riskFactorView = (ImageView) findViewById(R.id.risk_factor);
        suggestions_tv =(TextView) findViewById(R.id.suggestions);
        exerciseLevel = (TextView) findViewById(R.id.level);
        evaluateBtn = (Button) findViewById(R.id.reevaluate);
        exerciseLevel.setText(EXERCISE_LEVEL_TEXT_ID[PreferenceUtils.getInt(this, KeyConst.SKEA_EXERCISE_LEVEL_KEY,3)]);
        exerciseLevel.setOnClickListener(this);

        evaluateBtn.setOnClickListener(this);

//        User user= UserManager.getInstance().getUser(this);
//        if(user !=null){
//            List<Evaluation> evaluations=Evaluation.find(Evaluation.class,"email=?",user.getEmail());
//            if(evaluations!=null && evaluations.size()>0){//找到该ID下的最后一条数据，以最新的为准
//                int riskLeve=evaluations.get(evaluations.size()-1).getLevel();
//                riskFactorView.setImageResource(RISK_FACTOR_IMG_RES[riskLeve]);
//                suggestions_tv.setText(SUGGESTIONS_TEXT_ID[riskLeve]);
//
//            }
//        }
        List<Evaluation> evaluations=Evaluation.listAll(Evaluation.class);
        if(evaluations!=null && evaluations.size()>0){
            Evaluation evaluation=evaluations.get(evaluations.size()-1);
            int riskLevel=evaluation.getLevel();

            riskFactorView.setImageResource(RISK_FACTOR_IMG_RES[riskLevel]);
            suggestions_tv.setText(SUGGESTIONS_TEXT_ID[riskLevel]);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SETTING_LEVEL:
                //得到训练强度值
                if (resultCode == RESULT_OK) {
                    exerciseLevel.setText(EXERCISE_LEVEL_TEXT_ID[data.getIntExtra(KEY_EXERCISE_LEVEL, 3)]);
//                    Log.i(TAG, "onActivityResult - exercise level = " + );
                }
                break;
            case REQUEST_CODE_EVALUATE:
                if (resultCode == RESULT_OK) {
                    int riskFactor = data.getIntExtra(KEY_RISK_FACTOR, 0);
                    riskFactorView.setImageResource(RISK_FACTOR_IMG_RES[riskFactor]);
                    suggestions_tv.setText(SUGGESTIONS_TEXT_ID[riskFactor]);

                    Log.i(TAG, "onActivityResult - risk factor = " + data.getIntExtra(KEY_RISK_FACTOR, 0));
                }
                break;
            default:
                break;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.level:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ExerciseLevelSettingActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SETTING_LEVEL);
                break;
            case R.id.reevaluate:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.evaluate_notice_title))
                        .setMessage(getResources().getString(R.string.evaluate_notice_message))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.evaluate_notice_yes), new DialogInterface.OnClickListener() {//重新评估
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), ReEvaluateActivity.class);
                                startActivityForResult(intent, REQUEST_CODE_EVALUATE);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.evaluate_notice_no), new DialogInterface.OnClickListener() {//cancel
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                            }
                        });
                //显示Dialog
                builder.create().show();

                break;
            default:
                break;
        }
    }
}
