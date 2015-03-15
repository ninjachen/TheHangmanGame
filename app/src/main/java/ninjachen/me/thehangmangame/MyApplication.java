package ninjachen.me.thehangmangame;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

/**
 * Created by Ninja on 2015/3/15.
 */
public class MyApplication extends Application {
    private static MyApplication instance;

    /**
     * Create main application
     */
    public MyApplication() {
    }

    /**
     * Create main application
     *
     * @param context
     */
    public MyApplication(final Context context) {
        this();
        attachBaseContext(context);
    }

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public MyApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
