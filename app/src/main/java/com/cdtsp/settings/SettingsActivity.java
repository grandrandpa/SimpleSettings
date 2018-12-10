package com.cdtsp.settings;

import android.app.ActivityManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.tsp.TspCarInfoManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.automotive.vehicle.V2_0.VehicleDisplayType;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.cdtsp.hmilib.util.BluetoothUtil;
import com.cdtsp.hmilib.util.SettingsUtil;
import com.cdtsp.settings.fragment.FragmentMenu;
import com.cdtsp.settings.fragment.FragmentSettingAbout;
import com.cdtsp.settings.fragment.FragmentSettingAudio;
import com.cdtsp.settings.fragment.FragmentSettingBt;
import com.cdtsp.settings.fragment.FragmentSettingDataFlow;
import com.cdtsp.settings.fragment.FragmentSettingDisplay;
import com.cdtsp.settings.fragment.FragmentSettingNetwork;
import com.cdtsp.settings.fragment.FragmentSettingPersonal;
import com.cdtsp.settings.fragment.FragmentSettingReset;
import com.cdtsp.settings.fragment.FragmentSettingSystem;
import com.cdtsp.settings.fragment.FragmentSettingUpgrade;
import com.cdtsp.settings.fragment.FragmentSettingUserFeedback;
import com.cdtsp.settings.fragment.FragmentTheme;
import com.cdtsp.hmilib.util.DPCUtils;
import com.cdtsp.hmilib.ui.activity.SkinCloseBaseActivity;
import com.cdtsp.hmilib.skin.SkinChangeHelper;
import com.cdtsp.settings.util.MyUtils;
import com.cdtsp.settings.services.VehicleService;

import one.cluster.ClusterInteractive;

import java.util.ArrayList;
import java.lang.reflect.Method; 
import java.lang.reflect.InvocationTargetException;  

public class SettingsActivity extends SkinCloseBaseActivity
        implements FragmentMenu.Callback {
					

    private static final String TAG = "SettingsActivity";
    private FragmentManager mFragmentManager;
    private ArrayList<Fragment> mSettingFragments = new ArrayList<>();
    private int mCurSettingsPos;
    //Add-S-HMI-2018-03-20，拖拽实现主题切换功能
    private ViewGroup mRootLayout;
    //Add-E-HMI-2018-03-20，拖拽实现主题切换功能
    private TspCarInfoManager mCarInfoManager;
    private Car mCar;
    private Runnable mClusterThemeConfirm;
    private int mCurrentClusterTheme = -1;
	
    private VehicleService.VehicleBinder vehicleBinder;

	private ServiceConnection vehicleconnection=new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
			Log.d(TAG, "testchen vehicle service connected");
            vehicleBinder=(VehicleService.VehicleBinder) service;
        }
    };	
	
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                mCarInfoManager = (TspCarInfoManager) mCar.getCarManager(Car.TSP_CARINFO_SERVICE);
                mCarInfoManager.setOnCarClusterChangeListener(new TspCarInfoManager.OnCarClusterChangeListener() {
                    @Override
                    public void onCarClusterThemeChange(int id) {
                        mCurrentClusterTheme = id;
                        if (mClusterThemeConfirm != null) {
                            mClusterThemeConfirm.run();
                        }
                    }
                });
                mCarInfoManager.getClusterTheme();
                Log.d(TAG, "onServiceConnected: mCarInfoManager = " + mCarInfoManager);
            } catch (CarNotConnectedException e) {
                Log.d(TAG, "onServiceConnected: get mCarInfoManager failed ! " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCar.connect();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.activity_settings);

        mCar = Car.createCar(this, mServiceConnection);
        mCar.connect();
		
		Intent bindIntent = new Intent(this, VehicleService.class);
        bindService(bindIntent, vehicleconnection, BIND_AUTO_CREATE);
		
        mFragmentManager = getSupportFragmentManager();
        createSettingFragments();
        initUI();
        getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.SYSTEM_THEME),
                true,mThemeObserver);
				
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "SettingsActivity: onResume");
        ActivityManager manager = (ActivityManager)SettingsActivity.this.getSystemService(ACTIVITY_SERVICE);
        //manager.forceStopPackage("com.rightware.kanzi.cardemo");
        try {
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class); 

            try {
                
                method.invoke(manager, "com.rightware.kanzi.cardemo");
            }
            catch (IllegalArgumentException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            } catch (IllegalAccessException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            } catch( InvocationTargetException e) {
            }

        } catch (NoSuchMethodException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
        }  catch (ClassNotFoundException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }

        Intent intent = getIntent();
        String settingName = intent.getStringExtra(SettingsUtil.NAME_SETTING);
        Log.d(TAG, "SettingsActivity onResume fragment name: " + settingName);
        switchToSettingOnName(settingName);
        intent.removeExtra(SettingsUtil.NAME_SETTING);
    }	

    public void setVehicleSoundOn() 
    {
        if (vehicleBinder != null) {
            vehicleBinder.startVehicleSound();
            SharedPreferences prefEnginSimu = getApplicationContext().getSharedPreferences("engine_simu", Context.MODE_PRIVATE);
            prefEnginSimu.edit().putBoolean("engine_simu", true).commit();
        }
	}
	
	public void setVehicleSoundOff() 
    {
        if (vehicleBinder != null) {
            vehicleBinder.stopVehicleSound();
            SharedPreferences prefEnginSimu = getApplicationContext().getSharedPreferences("engine_simu", Context.MODE_PRIVATE);
            prefEnginSimu.edit().putBoolean("engine_simu", false).commit();
        }
	}
	
    private void createSettingFragments() {
        mSettingFragments.add(new FragmentSettingSystem());
        mSettingFragments.add(new FragmentSettingDisplay());
        mSettingFragments.add(new FragmentSettingAudio());
        mSettingFragments.add(new FragmentSettingNetwork());
        mSettingFragments.add(new FragmentSettingBt());
        mSettingFragments.add(new FragmentSettingPersonal());
        mSettingFragments.add(new FragmentSettingUpgrade());
        mSettingFragments.add(new FragmentSettingDataFlow());
        mSettingFragments.add(new FragmentSettingUserFeedback());
        mSettingFragments.add(new FragmentSettingAbout());
        mSettingFragments.add(new FragmentSettingReset());
    }

    private void initUI() {
        //Add-S-HMI-2018-03-20，拖拽实现主题切换功能
        initViews();
        //Add-E-HMI-2018-03-20，拖拽实现主题切换功能
        initMenuUI();
        initSettingContentUI();
    }

    //Add-S-HMI-2018-03-20，拖拽实现主题切换功能
    private void initViews() {
        mRootLayout = findViewById(R.id.root_layout);
    }
    private ImageView mFloatView;
    private View mFloatBgView;
    private RelativeLayout.LayoutParams mFloatViewParams;
    private RelativeLayout.LayoutParams mFloatViewBgParams;
    private final float WIDTH_FLOAT_VIEW = 1920 * 0.8f;
    private final float HEIGHT_FLOAT_VIEW = 720 * 0.8f;
    private GestureDetector mGestureDetector;
    private ClusterInteractive.CModuleCoverPosition.Builder mClusterCoverPositionBuilder;
    private ClusterInteractive.CModuleInterActive.Builder mClusterModuleInteractiveBuilder;
    private boolean mFling;
    private FragmentTheme.ClusterThemeInfo mCurClusterThemeInfo;
    /**
     * 添加FloatView
     */
    public void addFloatView(FragmentTheme.ClusterThemeInfo clusterThemeInfo) {
        mCurClusterThemeInfo = clusterThemeInfo;
        Log.d(TAG, "addFloatView: mCurClusterThemeInfo.theme=" + mCurClusterThemeInfo.theme);
        if (mFloatView == null) {
            mFloatView = new ImageView(this);
            mFloatViewParams = new RelativeLayout.LayoutParams((int)WIDTH_FLOAT_VIEW, (int)HEIGHT_FLOAT_VIEW);
            mFloatViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {}

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {}

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    MyUtils.toast(SettingsActivity.this, "velocityX = " + velocityX);
                    Log.d(TAG, "onFling: velocityX=" + velocityX);
                    if (velocityX < -500 && Math.abs(velocityX/velocityY) >= 2) {
                        MyUtils.toast(SettingsActivity.this, "velocityX = " + velocityX + ", onFlingonFlingonFling~~~");
                        //Mod-S-HMI-2018-03-22，通过DPC向仪表发送数据
                        mFling = true;
                        animateToCarBoard(velocityX, velocityY, mCurClusterThemeInfo);
                        //Mod-E-HMI-2018-03-22，通过DPC向仪表发送数据
                    }
                    return false;
                }
            });
            mFloatView.setOnTouchListener(new View.OnTouchListener() {
                float downX;
                float downY;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            downX = event.getRawX();
                            downY = event.getRawY();
                            //Add-S-HMI-2018-03-22，通过DPC向仪表发送数据
                            sendModuleStart();
                            //Add-E-HMI-2018-03-22，通过DPC向仪表发送数据
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float offsetX = event.getRawX() - downX;
                            float offsetY = event.getRawY() - downY;
                            Log.d(TAG, "onTouch: ACTION_MOVE, offsetX = " + offsetX + ", offsetY = " + offsetY);
                            mFloatView.setTranslationX(offsetX);
                            mFloatView.setTranslationY(offsetY);
                            float corX = (1920 - WIDTH_FLOAT_VIEW) / 2 + offsetX;
                            float corY = (720 - HEIGHT_FLOAT_VIEW) / 2 + offsetY;
                            if (corX <= 0) {
                                MyUtils.toast(SettingsActivity.this, "发送坐标： (" + corX + ", " + corY + ")");

                                //Add-S-HMI-2018-03-22，通过DPC向仪表发送数据
                                //设置并发送数据
                                sendModuleProgress((int) corX, (int) corY);
                                //Add-E-HMI-2018-03-22，通过DPC向仪表发送数据
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d(TAG, "onTouch: ACTION_UP");
                            mFloatView.setTranslationX(0);
                            mFloatView.setTranslationY(0);
                            if (!mFling) {
                                sendModuleEndNone();
                            } else {
                                mFling = false;
                            }
                            break;
                    }
                    return true;
                }
            });

            mFloatBgView = new View(this);
            mFloatBgView.setBackgroundColor(Color.GRAY);
            mFloatBgView.setAlpha(0.8f);
            mFloatViewBgParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            mFloatBgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFloatViewFlingAnim != null && mFloatViewFlingAnim.isStarted()) {
                        mFloatViewFlingAnim.cancel();
                    }
                    sendModuleEndNone();
                    getFloatViewExitAnim().start();
                }
            });
        }

        Bitmap coverBitmap = BitmapFactory.decodeResource(getResources(), clusterThemeInfo.largeIconResId);
        mFloatView.setImageBitmap(coverBitmap);
        mFloatView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFloatView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ValueAnimator anim = getFloatViewEnterAnim();
                anim.start();
            }
        });
        mRootLayout.addView(mFloatBgView, mFloatViewBgParams);
        mRootLayout.addView(mFloatView, mFloatViewParams);
    }

    private OnSwitchThemeCallback mOnSwitchThemeCallback;
    public void setOnSwitchThemeCallback(OnSwitchThemeCallback onSwitchThemeCallback) {
        this.mOnSwitchThemeCallback = onSwitchThemeCallback;
    }
    public interface OnSwitchThemeCallback {
        void onSwitchTheme();
    }

    /**
     * 去除FloatView
     */
    public void removeFloatView() {
        mRootLayout.removeView(mFloatView);
        mRootLayout.removeView(mFloatBgView);
        mFloatView.setTranslationX(0);
        mFloatView.setTranslationY(0);
    }

    private ValueAnimator mFloatViewEnterAnim;
    private ValueAnimator mFloatViewExitAnim;
    private ValueAnimator mFloatViewFlingAnim;
    /**
     * 获取FloatView出现动画
     * @return
     */
    public ValueAnimator getFloatViewEnterAnim() {
        if (mFloatViewEnterAnim == null) {
            mFloatViewEnterAnim = ValueAnimator.ofFloat(0.5f, 1f);
            mFloatViewEnterAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mFloatView.setScaleX(value);
                    mFloatView.setScaleY(value);
                }
            });
            mFloatViewEnterAnim.setDuration(1000);
            mFloatViewEnterAnim.setInterpolator(new BounceInterpolator());
        }
        return mFloatViewEnterAnim;
    }
    /**
     * 获取FloatView消失动画
     * @return
     */
    public ValueAnimator getFloatViewExitAnim() {
        if (mFloatViewExitAnim == null) {
            mFloatViewExitAnim = ValueAnimator.ofFloat(1f, 0.1f);
            mFloatViewExitAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mFloatView.setScaleX(value);
                    mFloatView.setScaleY(value);
                }
            });
            mFloatViewExitAnim.setDuration(150);
            mFloatViewExitAnim.setInterpolator(new DecelerateInterpolator());
            mFloatViewExitAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    removeFloatView();
                }
            });
        }
        return mFloatViewExitAnim;
    }

    private Handler mHandler = new Handler();
    private boolean mFlingAnimating;
    private int mCurCorX;
    private int mCurCorY;
    private Runnable mSendCordinationTask = new Runnable() {
        @Override
        public void run() {
            if (mFlingAnimating) {
                Log.d(TAG, "mSendCordinationTask # run: " + SystemClock.uptimeMillis());
                sendModuleProgress(mCurCorX, mCurCorY);
                mHandler.postDelayed(this, 17);
            }
        }
    };

    /**
     * 将FloatView被抛向仪表
     * @return
     * @param velocityX
     * @param velocityY
     */
    //Mod-S-HMI-2018-03-22，通过DPC向仪表发送数据
//    public void animateToCarBoard(float velocityX, float velocityY) {
    public void animateToCarBoard(float velocityX, float velocityY, FragmentTheme.ClusterThemeInfo clusterThemeInfo) {
    //Mod-E-HMI-2018-03-22，通过DPC向仪表发送数据
        float fingAngle = velocityY / velocityX;
        Point startPosition = new Point((int)mFloatView.getTranslationX(), (int)mFloatView.getTranslationY());
        int endX = - (mFloatView.getLeft() + mFloatView.getRight());
        Point endPosition = new Point(
                endX,
                (int) (endX * fingAngle)
        );
        Log.d(TAG, "animateToCarBoard: startPosition=" + startPosition.toString() + ", endPosition=" + endPosition);
        mFloatViewFlingAnim = ValueAnimator.ofObject(new PositionEvaluator(), startPosition, endPosition);
        mFloatViewFlingAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                Log.d(TAG, "animateToCarBoard, onAnimationUpdate: point=" + point);
                mFloatView.setTranslationX(point.x);
                mFloatView.setTranslationY(point.y);
//                final float corX = (1920 - WIDTH_FLOAT_VIEW) / 2 + point.x;
//                final float corY = (720 - HEIGHT_FLOAT_VIEW) / 2 + point.y;
//                if (Math.abs(corX - mLastSentCorX) >= 1 || corX == endX) {
//                    Log.d(TAG, "onFling, onAnimationUpdate: sendModuleProgress , corX = " + (int)corX);
//                    sendModuleProgress((int) corX, (int) corY);
//                }
                mCurCorX = (int) ((1920 - WIDTH_FLOAT_VIEW) / 2 + point.x);
                mCurCorY = (int) ((720 - HEIGHT_FLOAT_VIEW) / 2 + point.y);
            }
        });
        mFloatViewFlingAnim.setDuration(calculateFingTime(Math.abs(velocityX), Math.abs(startPosition.x - endPosition.x)));
        mFloatViewFlingAnim.setInterpolator(new DecelerateInterpolator());
        mFloatViewFlingAnim.addListener(new AnimatorListenerAdapter() {
            private boolean canceled;
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFlingAnimating = true;
                mHandler.post(mSendCordinationTask);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!canceled) {
                    removeFloatView();
                    //在这里用dpc发送数据
                    //Add-S-HMI-2018-03-22，通过DPC向仪表发送数据
                    Log.d(TAG, "onAnimationEnd: clusterThemeInfo.theme=" + clusterThemeInfo.theme);
                    sendTheme(clusterThemeInfo);

                    sendModuleStateEndTheme();
                    //Add-E-HMI-2018-03-22，通过DPC向仪表发送数据

                    //通过sendModuleStateEndTheme发送dpc消息给仪表后，才会真正切换主题
                    //因此这个时候才调用切换主题的回调
                    if (mOnSwitchThemeCallback != null) {
                        mOnSwitchThemeCallback.onSwitchTheme();
                    }
                } else {
                    canceled = false;
                }
                mFlingAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                canceled = true;
            }
        });
        mFloatViewFlingAnim.start();
    }

    /**
     * 通过dpc发送主题
     * @param clusterThemeInfo
     */
    private void sendTheme(FragmentTheme.ClusterThemeInfo clusterThemeInfo) {
        Log.d(TAG, "sendTheme: theme=" + clusterThemeInfo.theme);
        ClusterInteractive.CTheme.Builder themeBuilder = DPCUtils.createClusterTheme(clusterThemeInfo.theme);
        ClusterInteractive.CTheme theme = themeBuilder. build();
        byte[] inData = theme.toByteArray();
        //发送主题消息
        DPCUtils.sendRequest(
            ClusterInteractive.eMessageId.SIG_THEME_VALUE,
            inData
        );
    }

    /**
     * 通过dpc发送end_theme信息
     */
    private void sendModuleStateEndTheme() {
        ClusterInteractive.CModuleInterActive.Builder clusterModuleInteractiveBuilder = DPCUtils.createModuleInteractive(ClusterInteractive.eModuleChangeState.MODULE_STATE_END_THME);
        byte[] inDataEnd = clusterModuleInteractiveBuilder.build().toByteArray();
        //发送结束消息
        Log.d(TAG, "sendModuleStateEndTheme: send MODULE_STATE_END_THME");
        DPCUtils.sendRequest(
                ClusterInteractive.eMessageId.SIG_MODULE_INTERACTIVE_VALUE,
                inDataEnd
        );
    }

    /**
     * 通过dpc发送end_none信息
     */
    private void sendModuleEndNone() {
        ClusterInteractive.CModuleInterActive.Builder clusterModuleInteractiveBuilder = DPCUtils.createModuleInteractive(ClusterInteractive.eModuleChangeState.MODULE_STATE_END_NONE);
        byte[] inDataEnd = clusterModuleInteractiveBuilder.build().toByteArray();
        //发送结束消息
        Log.d(TAG, "sendModuleEndNone: send MODULE_STATE_END_NONE");
        DPCUtils.sendRequest(
                ClusterInteractive.eMessageId.SIG_MODULE_INTERACTIVE_VALUE,
                inDataEnd
        );
    }

    /**
     * 通过dpc发送拖拽图标时的坐标信息
     * @param corX
     * @param corY
     */
    private void sendModuleProgress(int corX, int corY) {
        if (mClusterCoverPositionBuilder != null) {
            mClusterCoverPositionBuilder.setX(corX);
            mClusterCoverPositionBuilder.setY(corY);
        } else {
            mClusterCoverPositionBuilder = DPCUtils.createCoverPosition(corX, corY);
        }
        if (mClusterModuleInteractiveBuilder == null) {
            mClusterModuleInteractiveBuilder = DPCUtils.createModuleInteractive(ClusterInteractive.eModuleChangeState.MODULE_STATE_PROCESSING);
        } else {
            mClusterModuleInteractiveBuilder.setState(ClusterInteractive.eModuleChangeState.MODULE_STATE_PROCESSING);
        }
        mClusterModuleInteractiveBuilder.setPosition(mClusterCoverPositionBuilder.build());
        byte[] inData = mClusterModuleInteractiveBuilder.build().toByteArray();
        Log.d(TAG, "sendModuleProgress: Cor= (" + mClusterCoverPositionBuilder.getX() + ", " + mClusterCoverPositionBuilder.getY() + ")" + ", " + inData.length);
        DPCUtils.sendRequest(
                ClusterInteractive.eMessageId.SIG_MODULE_INTERACTIVE_VALUE,
                inData
        );
    }

    /**
     * 通过dpc发送start信息
     */
    private void sendModuleStart() {
        //组装数据
        ClusterInteractive.CModuleCoverInfo.Builder moduleCoverInfoBuilder = DPCUtils.createCoverInfo(
                "theme" + mCurClusterThemeInfo.theme,
                "",
                (int) WIDTH_FLOAT_VIEW,
                (int) HEIGHT_FLOAT_VIEW,
                "png"
        );
        ClusterInteractive.CModuleInterActive.Builder clusterModuleInteractiveBuilder = DPCUtils.createModuleInteractive(
                ClusterInteractive.eModuleChangeState.MODULE_STATE_START
        );
        clusterModuleInteractiveBuilder.setInfo(moduleCoverInfoBuilder.build());
        byte[] inData = clusterModuleInteractiveBuilder.build().toByteArray();
        //发送数据
        Log.d(TAG, "sendModuleStart: inData : " + inData.length + ", send startTime=" + SystemClock.uptimeMillis());
        Log.d(TAG, "sendModuleStart: send MODULE_STATE_START");
        DPCUtils.sendRequest(
                ClusterInteractive.eMessageId.SIG_MODULE_INTERACTIVE_VALUE,
                inData
        );
        Log.d(TAG, "sendModuleStart: send completeTime=" + SystemClock.uptimeMillis());
    }

    /**
     * 位置计算器
     */
    private class PositionEvaluator implements TypeEvaluator<Point> {
        private Point position;

        public PositionEvaluator() {
            position = new Point();
        }

        @Override
        public Point evaluate(float fraction, Point startValue, Point endValue) {
            position.x = (int) (startValue.x * (1-fraction) + endValue.x * fraction);
            position.y = (int) (startValue.y * (1-fraction) + endValue.y * fraction);
            return position;
        }
    }

    /**
     * 计算抛向仪表盘时的动画时间，手势抛出的速度越快，返回的时间就越小，动画速度就越快
     * @param velocityX
     * @return
     */
    private long calculateFingTime(float velocityX, int distance) {
//        return (long) (600000f / velocityX);
        return (long) (2000f * distance / velocityX);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mGestureDetector != null && mFloatView != null && mFloatView.isAttachedToWindow()) {
            mGestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
    //Add-E-HMI-2018-03-20，拖拽实现主题切换功能

    private void initSettingContentUI() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment fragment = mSettingFragments.get(0);
        Fragment fragmentByTag = mFragmentManager.findFragmentByTag(fragment.getClass().getSimpleName());
        if (fragmentByTag != null) {
            transaction.replace(R.id.container_setting_content, fragment, fragment.getClass().getSimpleName());
        } else {
            transaction.add(R.id.container_setting_content, fragment, fragment.getClass().getSimpleName());
        }
        transaction.commit();
    }

    private void initMenuUI() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        FragmentMenu fragmentMenu = new FragmentMenu();
        Fragment fragmentByTag = mFragmentManager.findFragmentByTag(FragmentMenu.class.getSimpleName());
        if (fragmentByTag != null) {
            transaction.replace(R.id.container_menu, fragmentMenu, FragmentMenu.class.getSimpleName());
        } else {
            transaction.add(R.id.container_menu, fragmentMenu, FragmentMenu.class.getSimpleName());
        }
        transaction.commit();
    }

    @Override
    public void onSwitchSettingType(int pos) {
        if (pos == mCurSettingsPos) return;

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment curSettingFragment = mSettingFragments.get(mCurSettingsPos);
        transaction.hide(curSettingFragment);

        Fragment toSettingFragment = mSettingFragments.get(pos);
        String toSettingFragmentTag = toSettingFragment.getClass().getSimpleName();

        if (mFragmentManager.findFragmentByTag(toSettingFragmentTag) == null) {
            transaction.add(R.id.container_setting_content, toSettingFragment, toSettingFragmentTag);
        } else {
            transaction.show(toSettingFragment);
        }
        transaction.commit();
        mCurSettingsPos = pos;
    }

    public void switchToSettingOnName(String settingName) {
        int settingPos = findSettingPos(settingName);
        if (settingPos != -1) {
            FragmentMenu fragmentMenu = (FragmentMenu) mFragmentManager.findFragmentByTag(FragmentMenu.class.getSimpleName());
            if (fragmentMenu != null) {
                fragmentMenu.switchMediaList(settingPos);
            }
        }
    }

    private int findSettingPos(String settingName) {
        if (settingName == null) {
            return -1;
        }
        mSettingFragments.size();
        for (int i = 0; i < mSettingFragments.size(); i++) {
            if (settingName.equals(mSettingFragments.get(i).getClass().getSimpleName())) {
                return i;
            }
        }
        return -1;
    }

    public void setDisplayBrightness(int brightness) {
        if (mCarInfoManager != null) {
            try {
                mCarInfoManager.setDisplayBrightness(VehicleDisplayType.DSI1, brightness);
            } catch (CarNotConnectedException e) {
                Log.d(TAG, "setDisplayBrightness: failed, " + e.getMessage());
            }
        }
    }

    public int getDisplayBrightness() {
        if (mCarInfoManager != null) {
            try {
                return mCarInfoManager.getDisplayBrightness(VehicleDisplayType.DSI1);
            } catch (CarNotConnectedException e) {
                Log.d(TAG, "getDisplayBrightness: failed, " + e.getMessage());
            }
        }
        return 0;
    }

    /**
     * 调用了TspCarInfoManager.getClusterTheme()后，clusterTheme会异步返回
     * 若settingsActivity.getClusterTheme的时候，clusterTheme还没有返回，则会获取clusterTheme失败，
     * 因此就需要通过这里传入的Runnable来再次获取正确的clusterTheme
     * @param clusterThemeConfirm
     * @return
     */
    public int getClusterTheme(Runnable clusterThemeConfirm) {
        mClusterThemeConfirm = clusterThemeConfirm;
        return mCurrentClusterTheme;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mThemeObserver);
        BluetoothUtil.get(getApplicationContext()).release();
        mCar.disconnect();
    }

    private ContentObserver mThemeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            SkinChangeHelper.getInstance(getApplicationContext()).switchSkinMode(new SkinChangeHelper.OnSkinChangeListener(){

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                }
            });
        }
    };
}
