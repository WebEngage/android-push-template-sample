package com.webengage.pushtemplates.CallBacks

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.Utils.NotificationConfigurator
import com.webengage.pushtemplates.DataTypes.TimerStyle
import com.webengage.pushtemplates.R
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.actions.render.CallToAction
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.callbacks.CustomPushRender
import com.webengage.sdk.android.callbacks.CustomPushRerender
import com.webengage.sdk.android.utils.htmlspanner.WEHtmlParserInterface

class CountDownRenderer : CustomPushRender, CustomPushRerender {

    private lateinit var context: Context
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var pushData: TimerStyle
    private var notificationChannel: NotificationChannelCompat? = null
    private val SECOND = 1000;
    private val DEFAULT_SECONDS = 2;

    private var collapsedLayoutId = -1
    private var expandedLayoutId = -1
    private var collapsedTimerLayoutId = R.layout.layout_timer_collapsed
    private var expandedTimerLayoutId = R.layout.layout_timer_collapsed
    private val DEFAULT_TIME = DEFAULT_SECONDS * SECOND;
    private var whenTime: Long = 0


    override fun onRender(
        mContext: Context?,
        pushNotificationData: PushNotificationData?
    ): Boolean {

        this.context = mContext!!
        this.pushData = TimerStyle(pushNotificationData, context)
        this.whenTime = System.currentTimeMillis()
        if (pushData.timerTime < System.currentTimeMillis())
            return false
        initRender()
        return true

    }

    override fun onRerender(
        context: Context?,
        pushNotificationData: PushNotificationData?,
        extras: Bundle?
    ): Boolean {
        //TODO("Implement when the notification is supposed to me rendered with new content")
        return false
    }

    fun initRender() {
        constructNotification(context, pushData)
        show(context)
    }

    private fun constructNotification(context: Context?, pushNotificationData: TimerStyle?) {
        this.mBuilder =
            NotificationCompat.Builder(context!!, pushNotificationData!!.pushNotification.channelId)
        NotificationConfigurator().setNotificationConfiguration(
            context,
            mBuilder,
            pushNotificationData,
            whenTime
        )
        NotificationConfigurator().setDismissIntent(context, mBuilder, pushNotificationData)
        NotificationConfigurator().setClickIntent(context, mBuilder, pushNotificationData)

        this.mBuilder.setCustomContentView(
            constructCollapsedTimerPushBase(
                context,
                pushNotificationData
            )
        )
        this.mBuilder.setCustomBigContentView(
            constructExpandedTimerPushBase(
                context,
                pushNotificationData
            )
        )
    }

    private fun constructExpandedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyle?
    ): RemoteViews {

        val remoteView = RemoteViews(context.packageName, expandedTimerLayoutId)

        NotificationConfigurator().configureRemoteView(
            context,
            remoteView,
            timerNotificationData!!,
            whenTime
        )
        NotificationConfigurator().setNotificationDescription(
            context,
            timerNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(context, timerNotificationData, remoteView)
        NotificationConfigurator().setCTAList(context, remoteView, pushData)
        NotificationConfigurator().setClickIntent(context, remoteView, pushData)

        val timeDiff =
            timerNotificationData.timerTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData.timerFormat,
            true
        )
        return remoteView
    }

    private fun constructCollapsedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyle?
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, collapsedTimerLayoutId)

        NotificationConfigurator().configureRemoteView(
            context,
            remoteView,
            timerNotificationData!!,
            whenTime
        )
        NotificationConfigurator().setNotificationDescription(
            context,
            timerNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(context, timerNotificationData, remoteView)
        NotificationConfigurator().setClickIntent(context, remoteView, timerNotificationData)

        val timeDiff =
            timerNotificationData.timerTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData.timerFormat,
            true
        )

        return remoteView
    }

    private fun show(context: Context) {
        Log.d("Timer", "Showing Notification")
        var channel = notificationChannel
        if (channel == null) {
            channel = NotificationConfigurator().getDefaultNotificationChannel(
                context
            )
        }
        mBuilder.setChannelId(channel.id)
        mBuilder.setTimeoutAfter(pushData.timerTime - System.currentTimeMillis())
        Handler(Looper.getMainLooper()).postDelayed(
            {
                NotificationManagerCompat.from(context)
                    .cancel(pushData.pushNotification.experimentId.hashCode())
            },
            pushData.timerTime - System.currentTimeMillis()
        )
        with(NotificationManagerCompat.from(context)) {
            notify(pushData.pushNotification.variationId.hashCode(), mBuilder.build())
        }
    }
}